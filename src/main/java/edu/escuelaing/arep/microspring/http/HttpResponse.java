package edu.escuelaing.arep.microspring.http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private int statusCode;
    private String statusMessage;
    private Map<String, String> headers;
    private String body;

    public HttpResponse(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.headers = new HashMap<>();
        this.body = "";
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setBody(String body, String contentType) {
        this.body = body;
        addHeader("Content-Type", contentType);
        addHeader("Content-Length", String.valueOf(body.length()));
    }

    public String buildResponse() {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusMessage).append("\r\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            response.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        response.append("\r\n").append(body);
        return response.toString();
    }
}

