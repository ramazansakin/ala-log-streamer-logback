package com.daimler.rssm.alalogstreamer.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.daimler.rssm.alalogstreamer.ala.LogAnalyticsAppender;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogStreamerController {

    private final LogAnalyticsAppender logAnalyticsAppender;

    public LogStreamerController() {
        this.logAnalyticsAppender = new LogAnalyticsAppender(
                "test-ala-log-streamer",
                "secret",
                "workspaceId");
        this.logAnalyticsAppender.start();
    }

    @GetMapping("/test")
    public void testLogToALA() {
        Logger logger = (Logger) LoggerFactory.getLogger(LogStreamerController.class);
        ILoggingEvent loggingEvent = new LoggingEvent("Fqcn", logger, Level.INFO, "Test message", null, null);
        logAnalyticsAppender.doAppend(loggingEvent);
    }

}
