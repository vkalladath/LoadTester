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
    private String billShockUrl;
    /**
     * The desired rate of requests per second.
     * Can be set via environment variable LOADTESTER_REQUEST_RATE_PER_SECOND.
     */
    private int requestRatePerSecond;
    private int billShockS3RequestIntervalInMillis;
    private int billShockDynamoDbRequestIntervalInMillis;
    private int billShockRDSRequestIntervalInMillis;
    private int billShockEC2RequestIntervalInMillis;
    private int billShockS3DownloadRequestIntervalInMillis;
    private int billShockDynamoDBGetRequestIntervalInMillis;
    private int billShockS3DeleteRequestIntervalInMillis;
    private int billShockDynamoDBDeleteRequestIntervalInMillis;

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

    public String getBillShockUrl() {
        return billShockUrl;
    }

    public void setBillShockUrl(String billShockUrl) {
        this.billShockUrl = billShockUrl;
    }

    public int getBillShockS3RequestIntervalInMillis() {
        return billShockS3RequestIntervalInMillis;
    }

    public void setBillShockS3RequestIntervalInMillis(int billShockS3RequestIntervalInMillis) {
        this.billShockS3RequestIntervalInMillis = billShockS3RequestIntervalInMillis;
    }

    public int getBillShockDynamoDbRequestIntervalInMillis() {
        return billShockDynamoDbRequestIntervalInMillis;
    }

    public void setBillShockDynamoDbRequestIntervalInMillis(int billShockDynamoDbRequestIntervalInMillis) {
        this.billShockDynamoDbRequestIntervalInMillis = billShockDynamoDbRequestIntervalInMillis;
    }

    public int getBillShockRDSRequestIntervalInMillis() {
        return billShockRDSRequestIntervalInMillis;
    }

    public void setBillShockRDSRequestIntervalInMillis(int billShockRDSRequestIntervalInMillis) {
        this.billShockRDSRequestIntervalInMillis = billShockRDSRequestIntervalInMillis;
    }

    public int getBillShockEC2RequestIntervalInMillis() {
        return billShockEC2RequestIntervalInMillis;
    }

    public void setBillShockEC2RequestIntervalInMillis(int billShockEC2RequestIntervalInMillis) {
        this.billShockEC2RequestIntervalInMillis = billShockEC2RequestIntervalInMillis;
    }

    public int getBillShockS3DownloadRequestIntervalInMillis() {
        return billShockS3DownloadRequestIntervalInMillis;
    }

    public void setBillShockS3DownloadRequestIntervalInMillis(int billShockS3DownloadRequestIntervalInMillis) {
        this.billShockS3DownloadRequestIntervalInMillis = billShockS3DownloadRequestIntervalInMillis;
    }

    public int getBillShockDynamoDBGetRequestIntervalInMillis() {
        return billShockDynamoDBGetRequestIntervalInMillis;
    }

    public void setBillShockDynamoDBGetRequestIntervalInMillis(int billShockDynamoDBGetRequestIntervalInMillis) {
        this.billShockDynamoDBGetRequestIntervalInMillis = billShockDynamoDBGetRequestIntervalInMillis;
    }

    public int getBillShockS3DeleteRequestIntervalInMillis() {
        return billShockS3DeleteRequestIntervalInMillis;
    }

    public void setBillShockS3DeleteRequestIntervalInMillis(int billShockS3DeleteRequestIntervalInMillis) {
        this.billShockS3DeleteRequestIntervalInMillis = billShockS3DeleteRequestIntervalInMillis;
    }

    public int getBillShockDynamoDBDeleteRequestIntervalInMillis() {
        return billShockDynamoDBDeleteRequestIntervalInMillis;
    }

    public void setBillShockDynamoDBDeleteRequestIntervalInMillis(int billShockDynamoDBDeleteRequestIntervalInMillis) {
        this.billShockDynamoDBDeleteRequestIntervalInMillis = billShockDynamoDBDeleteRequestIntervalInMillis;
    }
}
