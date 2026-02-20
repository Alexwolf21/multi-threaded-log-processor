package com.example.logproc;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogProcessorApp {
    public static void main(String[] args) {
        Logger logger = AppLogger.get();
        try {
            CliOptions options = CliOptions.parse(args);
            logger.info("Starting log processing");

            LogProcessingService service = new LogProcessingService();
            SummaryReport report = service.process(options.inputDir(), options.producers(), options.consumers(), options.queueSize());
            JsonWriter.writeSummary(options.outputFile(), report);

            logger.info("Processing complete. Summary written to: " + options.outputFile());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Application failure: " + ex.getMessage());
            System.exit(1);
        }
    }
}
