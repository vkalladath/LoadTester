package com.example.loadtester.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "loadtester")
public class LoadTesterProperties {

    /**
     * The target URL to send GET requests to.
     * Can be set via environment variable LOADTESTER_TARGET_URL.
     */
    private String targetUrl;

    /**
     * The desired rate of requests per second.
     * Can be set via environment variable LOADTESTER_REQUEST_RATE_PER_SECOND.
     */
    private int requestRatePerSecond;

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public int getRequestRatePerSecond() {
        return requestRatePerSecond;
    }

    public void setRequestRatePerSecond(int requestRatePerSecond) {
        this.requestRatePerSecond = requestRatePerSecond;
    }
}
