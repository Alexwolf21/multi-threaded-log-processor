package com.example.logproc;

public record LogEntry(String service, int status, long latencyMs) {
}
