package com.example.logproc;

import java.nio.file.Files;
import java.nio.file.Path;

public class LogProcessorIntegrationTest {
    public static void run() throws Exception {
        Path tempDir = Files.createTempDirectory("logproc-it");
        Path file1 = tempDir.resolve("a.log");
        Path file2 = tempDir.resolve("b.log");
        Path out = tempDir.resolve("summary.json");

        Files.writeString(file1, String.join("\n",
                "2026-02-19T12:34:56.789Z service-A GET /api/v1/users 200 100ms",
                "2026-02-19T12:34:56.790Z service-A POST /api/v1/users 500 300ms",
                "malformed-line"));

        Files.writeString(file2, String.join("\n",
                "2026-02-19T12:34:57.789Z service-B GET /api/v1/orders 200 50ms",
                "2026-02-19T12:34:57.790Z service-B GET /api/v1/orders 200 150ms"));

        LogProcessorApp.main(new String[]{
                "--input", tempDir.toString(),
                "--producers", "2",
                "--consumers", "2",
                "--queue-size", "10",
                "--output", out.toString()
        });

        String json = Files.readString(out);
        TestAssertions.assertTrue(json.contains("\"totalFiles\": 2"), "totalFiles");
        TestAssertions.assertTrue(json.contains("\"totalLines\": 5"), "totalLines");
        TestAssertions.assertTrue(json.contains("\"malformedLines\": 1"), "malformedLines");
        TestAssertions.assertTrue(json.contains("\"service-A\""), "service-A present");
        TestAssertions.assertTrue(json.contains("\"avgLatencyMs\": 200"), "avgLatencyMs for service-A");
        TestAssertions.assertTrue(json.contains("\"p95LatencyMs\": 300"), "p95LatencyMs for service-A");
    }
}
