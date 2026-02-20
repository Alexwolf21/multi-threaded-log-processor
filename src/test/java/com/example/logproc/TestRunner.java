package com.example.logproc;

public class TestRunner {
    public static void main(String[] args) throws Exception {
        LogParserTest.run();
        ServiceAccumulatorTest.run();
        LogProcessorIntegrationTest.run();
        System.out.println("All tests passed");
    }
}
