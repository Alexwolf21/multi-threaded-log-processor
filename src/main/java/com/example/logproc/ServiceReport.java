package com.example.logproc;

import java.util.Map;

public record ServiceReport(long totalRequests,
                            Map<String, Long> statusCounts,
                            long avgLatencyMs,
                            long p95LatencyMs) {
}
