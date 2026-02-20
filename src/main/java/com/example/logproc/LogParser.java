package com.example.logproc;

import java.util.Optional;

public final class LogParser {

    private LogParser() {
    }

    public static Optional<LogEntry> parse(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }

        String[] parts = line.trim().split("\\s+");
        if (parts.length != 6) {
            return Optional.empty();
        }

        // fields: timestamp service method path status latency
        String service = parts[1];

        try {
            int status = Integer.parseInt(parts[4]);
            String latencyToken = parts[5];
            if (!latencyToken.endsWith("ms")) {
                return Optional.empty();
            }
            long latencyMs = Long.parseLong(latencyToken.substring(0, latencyToken.length() - 2));
            if (latencyMs < 0) {
                return Optional.empty();
            }
            return Optional.of(new LogEntry(service, status, latencyMs));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
