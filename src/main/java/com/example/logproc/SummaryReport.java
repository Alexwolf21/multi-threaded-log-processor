package com.example.logproc;

import java.util.Map;

public record SummaryReport(long totalFiles,
                            long totalLines,
                            long malformedLines,
                            long processingTimeMs,
                            Map<String, ServiceReport> services) {
}
