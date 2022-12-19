package com.daimler.rssm.alalogstreamer.ala;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;

import java.util.Map;
import java.util.Objects;

public class LogAnalyticsEvent {

    private final String message;
    private final String level;
    private final long timestamp;
    private final int levelWeigth;
    private final String logger;
    private final String threadName;
    private final String exceptionMessage;
    private final String exceptionType;
    private final String exceptionStacktrace;
    private final Map<String, String> contextData;

    public LogAnalyticsEvent(ILoggingEvent event) {
        IThrowableProxy thrown = event.getThrowableProxy();

        this.message = event.getFormattedMessage();
        this.level = event.getLevel().toString();
        this.levelWeigth = event.getLevel().toInt();
        this.logger = event.getLoggerName();
        this.threadName = event.getThreadName();
        this.timestamp = event.getTimeStamp();
        this.exceptionMessage = Objects.nonNull(thrown) ? thrown.getMessage() : null;
        this.exceptionType = Objects.nonNull(thrown) ? thrown.getClassName() : null;
        this.exceptionStacktrace = Objects.nonNull(thrown)
                ? String.format("%s: %s%n%s",thrown.getClassName(), thrown.getMessage(), stringify(thrown))
                : null;
        this.contextData = event.getMDCPropertyMap();
    }

    private String stringify(IThrowableProxy thrown) {
        StringBuilder builder = new StringBuilder();
        StackTraceElementProxy[] stackElements = thrown.getStackTraceElementProxyArray();

        for (StackTraceElementProxy step : stackElements) {
            String string = step.toString();
            builder.append('\t').append(string);
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
            builder.append(CoreConstants.LINE_SEPARATOR);
        }
        return builder.toString();
    }


    public String getMessage() {
        return message;
    }

    public String getLevel() {
        return level;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getLevelWeigth() {
        return levelWeigth;
    }

    public String getLogger() {
        return logger;
    }

    public String getThreadName() {
        return threadName;
    }
    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public String getExceptionStacktrace() {
        return exceptionStacktrace;
    }

    public Map<String, String> getContextData() {
        return contextData;
    }
}
