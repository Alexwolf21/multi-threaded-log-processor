package com.example.logproc;

public class ServiceAccumulatorTest {
    public static void run() {
        ServiceAccumulator accumulator = new ServiceAccumulator();
        for (int i = 1; i <= 100; i++) {
            accumulator.add(200, i);
        }

        ServiceReport report = accumulator.snapshot();
        TestAssertions.assertEquals(100, report.totalRequests(), "request count");
        TestAssertions.assertEquals(100, report.statusCounts().get("200"), "status count");
        TestAssertions.assertEquals(50, report.avgLatencyMs(), "avg latency");
        TestAssertions.assertEquals(95, report.p95LatencyMs(), "p95 latency");
    }
}
