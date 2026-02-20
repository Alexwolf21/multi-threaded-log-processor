package com.example.logproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class ServiceAccumulator {
    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder totalLatency = new LongAdder();
    private final ConcurrentHashMap<Integer, LongAdder> statusCounts = new ConcurrentHashMap<>();
    private final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());

    public void add(int status, long latencyMs) {
        totalRequests.increment();
        totalLatency.add(latencyMs);
        statusCounts.computeIfAbsent(status, k -> new LongAdder()).increment();
        latencies.add(latencyMs);
    }

    public ServiceReport snapshot() {
        long requests = totalRequests.sum();
        long avg = requests == 0 ? 0 : totalLatency.sum() / requests;

        Map<String, Long> statusSnapshot = new HashMap<>();
        statusCounts.forEach((status, count) -> statusSnapshot.put(String.valueOf(status), count.sum()));

        List<Long> copy;
        synchronized (latencies) {
            copy = new ArrayList<>(latencies);
        }
        copy.sort(Long::compareTo);
        long p95 = percentile95(copy);

        return new ServiceReport(requests, statusSnapshot, avg, p95);
    }

    private long percentile95(List<Long> sortedLatencies) {
        if (sortedLatencies.isEmpty()) {
            return 0;
        }
        int index = (int) Math.ceil(sortedLatencies.size() * 0.95) - 1;
        index = Math.max(0, Math.min(index, sortedLatencies.size() - 1));
        return sortedLatencies.get(index);
    }
}
