package com.example.logproc;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LogProcessingService {
    private final Logger logger = AppLogger.get();

    public SummaryReport process(Path inputDir, int producerCount, int consumerCount, int queueSize) throws IOException, InterruptedException {
        long startNanos = System.nanoTime();
        List<Path> files;
        try (var stream = Files.list(inputDir)) {
            files = stream.filter(Files::isRegularFile).collect(Collectors.toList());
        }

        BlockingQueue<LineTask> queue = new ArrayBlockingQueue<>(queueSize);
        AtomicInteger nextFileIndex = new AtomicInteger(0);

        LongAdder totalLines = new LongAdder();
        LongAdder malformedLines = new LongAdder();
        ConcurrentHashMap<String, ServiceAccumulator> services = new ConcurrentHashMap<>();

        Thread[] producers = new Thread[producerCount];
        Thread[] consumers = new Thread[consumerCount];

        for (int i = 0; i < producerCount; i++) {
            int id = i;
            producers[i] = new Thread(() -> runProducer(files, nextFileIndex, queue, totalLines), "producer-" + id);
            producers[i].start();
        }

        for (int i = 0; i < consumerCount; i++) {
            int id = i;
            consumers[i] = new Thread(() -> runConsumer(queue, malformedLines, services), "consumer-" + id);
            consumers[i].start();
        }

        for (Thread producer : producers) {
            producer.join();
        }

        for (int i = 0; i < consumerCount; i++) {
            queue.put(LineTask.poison());
        }

        for (Thread consumer : consumers) {
            consumer.join();
        }

        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
        Map<String, ServiceReport> serviceReports = services.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().snapshot()));

        return new SummaryReport(files.size(), totalLines.sum(), malformedLines.sum(), elapsedMs, serviceReports);
    }

    private void runProducer(List<Path> files,
                             AtomicInteger nextFileIndex,
                             BlockingQueue<LineTask> queue,
                             LongAdder totalLines) {
        while (true) {
            int idx = nextFileIndex.getAndIncrement();
            if (idx >= files.size()) {
                return;
            }

            Path file = files.get(idx);
            logger.info("Reading file: " + file);
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    totalLines.increment();
                    queue.put(LineTask.data(line));
                }
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, "Producer interrupted: " + ex.getMessage());
                Thread.currentThread().interrupt();
                return;
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Failed processing file: " + file + " - " + ex.getMessage());
            }
        }
    }

    private void runConsumer(BlockingQueue<LineTask> queue,
                             LongAdder malformedLines,
                             ConcurrentHashMap<String, ServiceAccumulator> services) {
        while (true) {
            try {
                LineTask task = queue.take();
                if (task.poisonPill()) {
                    return;
                }

                var parsed = LogParser.parse(task.line());
                if (parsed.isEmpty()) {
                    malformedLines.increment();
                    logger.warning("Malformed line skipped: " + task.line());
                    continue;
                }

                LogEntry entry = parsed.get();
                services.computeIfAbsent(entry.service(), s -> new ServiceAccumulator())
                        .add(entry.status(), entry.latencyMs());
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, "Consumer interrupted: " + ex.getMessage());
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Unexpected consumer error: " + ex.getMessage());
            }
        }
    }
}
