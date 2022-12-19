package com.daimler.rssm.alalogstreamer.common;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Encapsulates the signature-creation that is necessary for the API-authentication scheme
 *
 * @see <a href="https://docs.microsoft.com/en-us/azure/azure-monitor/logs/data-collector-api">https://docs.microsoft.com/en-us/azure/azure-monitor/logs/data-collector-api</a>
 */
public class Signature {
    static final String RFC_1123_DATE = "EEE, dd MMM yyyy HH:mm:ss z";
    private static final String httpMethod = "POST";
    private static final String contentType = "application/json";
    private static final String xmsDate = "x-ms-date:";
    private static final String resource = "/api/logs";

    private final String payload;
    private final String sharedSecret;
    private final String workspaceId;
    private final String xmsDateAsString;
    private final String signatureAsString;
    private final String signatureAsClearString;


    public Signature(String payload, String workspaceId, String sharedSecret) throws NoSuchAlgorithmException, InvalidKeyException {
        this.payload = payload;
        this.workspaceId = workspaceId;
        this.sharedSecret = sharedSecret;

        this.xmsDateAsString = currentDateAsRfc1123String();
        this.signatureAsClearString = String.join("\n",
                httpMethod,
                String.valueOf(payload.getBytes(UTF_8).length),
                contentType,
                xmsDate + this.xmsDateAsString,
                resource);

        this.signatureAsString = hmac256(this.signatureAsClearString, this.sharedSecret);
    }

    public String getPayload() {
        return payload;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getXmsDateAsString() {
        return xmsDateAsString;
    }

    public String getSignatureAsString() {
        return signatureAsString;
    }

    public String getSignatureAsClearString() {
        return signatureAsClearString;
    }

    @Override
    public String toString() {
        return this.getSignatureAsString();
    }

    private String currentDateAsRfc1123String() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(RFC_1123_DATE, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    private String hmac256(String input, String key) throws InvalidKeyException, NoSuchAlgorithmException {
        String hash;
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        Base64.Decoder decoder = Base64.getDecoder();
        SecretKeySpec secretKey = new SecretKeySpec(decoder.decode(key.getBytes(UTF_8)), "HmacSHA256");
        sha256HMAC.init(secretKey);
        Base64.Encoder encoder = Base64.getEncoder();
        hash = new String(encoder.encode(sha256HMAC.doFinal(input.getBytes(UTF_8))));
        return hash;
    }
}
