package com.example.logproc;

import java.nio.file.Path;

public record CliOptions(Path inputDir, int producers, int consumers, int queueSize, Path outputFile) {

    public static CliOptions parse(String[] args) {
        Path input = null;
        Path output = null;
        int producers = 2;
        int consumers = 4;
        int queueSize = 1000;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--input" -> input = Path.of(nextArg(args, ++i, "--input"));
                case "--producers" -> producers = Integer.parseInt(nextArg(args, ++i, "--producers"));
                case "--consumers" -> consumers = Integer.parseInt(nextArg(args, ++i, "--consumers"));
                case "--queue-size" -> queueSize = Integer.parseInt(nextArg(args, ++i, "--queue-size"));
                case "--output" -> output = Path.of(nextArg(args, ++i, "--output"));
                default -> throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }

        if (input == null || output == null) {
            throw new IllegalArgumentException("Usage: --input <dir> --output <file> [--producers N] [--consumers N] [--queue-size N]");
        }
        if (producers <= 0 || consumers <= 0 || queueSize <= 0) {
            throw new IllegalArgumentException("producers, consumers, and queue-size must be > 0");
        }

        return new CliOptions(input, producers, consumers, queueSize, output);
    }

    private static String nextArg(String[] args, int idx, String flag) {
        if (idx >= args.length) {
            throw new IllegalArgumentException("Missing value for " + flag);
        }
        return args[idx];
    }
}
