package com.daimler.rssm.alalogstreamer.common;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.function.Supplier;

public class HttpLogTransport {

    private static final int TIMEOUT_MILLIS = 500;
    private final String name;
    private final Supplier<String> urlProvider;

    public HttpLogTransport(final String workspaceId, final String name) {
        this(name, new LogAnalyticsUrlProvider(workspaceId));
    }
    HttpLogTransport(final String name, final Supplier<String> logAnalyticsUrlProvider) {
        this.name = name;
        this.urlProvider = logAnalyticsUrlProvider;
    }

    public void send(Signature signature) throws IOException {
        String auth = "SharedKey " + signature.getWorkspaceId() + ":" + signature.getSignatureAsString();
        HttpPost httpPost = new HttpPost(urlProvider.get());
        httpPost.setHeader("Authorization", auth);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Log-Type", this.name);
        httpPost.setHeader("x-ms-date", signature.getXmsDateAsString());
        httpPost.setHeader("time-generated-field", "");
        httpPost.setEntity(new StringEntity(signature.getPayload()));
        httpPost.setConfig(RequestConfig
                .custom()
                .setSocketTimeout(TIMEOUT_MILLIS)
                .setConnectTimeout(TIMEOUT_MILLIS)
                .setConnectionRequestTimeout(TIMEOUT_MILLIS)
                .build());
        try (CloseableHttpClient httpClient = HttpClients.createSystem()) {
            HttpResponse response = httpClient.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            // HttpEntity entity = response.getEntity();
        }
    }

    static class LogAnalyticsUrlProvider implements Supplier<String> {
        private static final String API = "/api/logs";
        private static final String API_VERSION = "2016-04-01";

        private final String workspaceId;

        public LogAnalyticsUrlProvider(String workspaceId) {
            this.workspaceId = workspaceId;
        }

        @Override
        public String get() {
            return "https://" + this.workspaceId + ".ods.opinsights.azure.com" + API + "?api-version=" + API_VERSION;
        }
    }
}
