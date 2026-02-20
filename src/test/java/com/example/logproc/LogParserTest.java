package com.example.logproc;

public class LogParserTest {
    public static void run() {
        String valid = "2026-02-19T12:34:56.789Z service-A GET /api/v1/users 200 123ms";
        var parsed = LogParser.parse(valid);
        TestAssertions.assertTrue(parsed.isPresent(), "valid line should parse");
        TestAssertions.assertEquals("service-A", parsed.get().service(), "service should match");
        TestAssertions.assertEquals(200, parsed.get().status(), "status should match");
        TestAssertions.assertEquals(123, parsed.get().latencyMs(), "latency should match");

        TestAssertions.assertTrue(LogParser.parse("bad line").isEmpty(), "malformed should be empty");
    }
}
