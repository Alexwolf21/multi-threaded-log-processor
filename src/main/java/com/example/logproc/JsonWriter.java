package com.example.logproc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public final class JsonWriter {
    private JsonWriter() {
    }

    public static void writeSummary(Path out, SummaryReport summary) throws IOException {
        if (out.getParent() != null) {
            Files.createDirectories(out.getParent());
        }
        Files.writeString(out, toJson(summary));
    }

    public static String toJson(SummaryReport s) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"totalFiles\": ").append(s.totalFiles()).append(",\n");
        sb.append("  \"totalLines\": ").append(s.totalLines()).append(",\n");
        sb.append("  \"malformedLines\": ").append(s.malformedLines()).append(",\n");
        sb.append("  \"processingTimeMs\": ").append(s.processingTimeMs()).append(",\n");
        sb.append("  \"services\": {");

        var sortedServices = new TreeMap<>(s.services());
        if (!sortedServices.isEmpty()) {
            sb.append("\n");
            int i = 0;
            for (Map.Entry<String, ServiceReport> e : sortedServices.entrySet()) {
                sb.append("    \"").append(escape(e.getKey())).append("\": ");
                appendService(sb, e.getValue(), 4);
                if (i++ < sortedServices.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  }");
        } else {
            sb.append("}");
        }

        sb.append("\n}");
        return sb.toString();
    }

    private static void appendService(StringBuilder sb, ServiceReport s, int indent) {
        String pad = " ".repeat(indent);
        sb.append("{\n");
        sb.append(pad).append("  \"totalRequests\": ").append(s.totalRequests()).append(",\n");
        sb.append(pad).append("  \"statusCounts\": {");

        var sorted = s.statusCounts().entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .toList();
        if (!sorted.isEmpty()) {
            sb.append(" ");
            for (int i = 0; i < sorted.size(); i++) {
                var e = sorted.get(i);
                sb.append("\"").append(escape(e.getKey())).append("\": ").append(e.getValue());
                if (i < sorted.size() - 1) sb.append(", ");
            }
            sb.append(" },\n");
        } else {
            sb.append("},\n");
        }

        sb.append(pad).append("  \"avgLatencyMs\": ").append(s.avgLatencyMs()).append(",\n");
        sb.append(pad).append("  \"p95LatencyMs\": ").append(s.p95LatencyMs()).append("\n");
        sb.append(pad).append("}");
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
