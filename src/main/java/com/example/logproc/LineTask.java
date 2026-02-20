package com.example.logproc;

public record LineTask(String line, boolean poisonPill) {
    public static LineTask data(String line) {
        return new LineTask(line, false);
    }

    public static LineTask poison() {
        return new LineTask("", true);
    }
}
