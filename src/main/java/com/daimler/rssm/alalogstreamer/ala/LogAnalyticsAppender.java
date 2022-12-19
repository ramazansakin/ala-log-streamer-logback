package com.daimler.rssm.alalogstreamer.ala;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.status.WarnStatus;
import com.daimler.rssm.alalogstreamer.common.HttpLogTransport;
import com.daimler.rssm.alalogstreamer.common.Signature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

public class LogAnalyticsAppender extends AppenderBase<ILoggingEvent> {

    private String sharedSecret;
    private String sharedSecretEnvVariable;
    private String workspaceId;
    private String secret;
    ObjectMapper objectMapper;
    HttpLogTransport httpTransport;

    public LogAnalyticsAppender(String name, String sharedSecret, String workspaceId) {
        this.name = name;
        this.sharedSecret = sharedSecret;
        this.workspaceId = workspaceId;
    }

    public String getSharedSecretEnvVariable() {
        return sharedSecretEnvVariable;
    }

    public void setSharedSecretEnvVariable(String sharedSecretEnvVariable) {
        this.sharedSecretEnvVariable = sharedSecretEnvVariable;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    @Override
    public void start() {
        this.secret = Objects.requireNonNull(
                determineSecret(this.sharedSecretEnvVariable, this.sharedSecret),
                "Missconfiguration of LogAnalyticsAppender - either 'sharedSecret' or 'sharedSecretEnvironmentVariableName' has to be defined");
        this.httpTransport = new HttpLogTransport(workspaceId, name);
        this.objectMapper = new ObjectMapper();

        super.start();
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        if (!this.isStarted()) {
            this.addStatus(new WarnStatus("Appender not initialized yet ...", this));
            return;
        }

        try {
            String logMessage = this.objectMapper.writeValueAsString(new LogAnalyticsEvent(iLoggingEvent));
            this.addInfo(logMessage);
            httpTransport.send(new Signature(logMessage, this.workspaceId, this.secret));
        } catch (Exception e) {
            this.addError("Couldn't send log-message to Log-Analytics", e);
        }
    }

    private String determineSecret(String sharedSecretEnvironmentVariableName, String sharedSecret) {
        return Objects.nonNull(sharedSecretEnvironmentVariableName)
                ? System.getenv(sharedSecretEnvironmentVariableName)
                : sharedSecret;
    }
}
