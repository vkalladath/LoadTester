package com.example.loadtester.service;

import com.example.loadtester.config.LoadTesterProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class LoadTestingService {

    private static final Logger log = LoggerFactory.getLogger(LoadTestingService.class);

    private final LoadTesterProperties properties;
    private final MetricService metricService;
    private final WebClient webClient;
    private final ConfigService configService;

    private ScheduledExecutorService scheduler;

    public LoadTestingService(LoadTesterProperties properties, MetricService metricService, WebClient.Builder webClientBuilder, ConfigService configService) {
        this.properties = properties;
        this.metricService = metricService;
        this.configService = configService;
        // Configure WebClient with a timeout
        this.webClient = webClientBuilder
                //.responseTimeout(Duration.ofSeconds(10)) // Example timeout
                .build();
    }

    @PostConstruct
    public void startLoadTesting() {
        String targetUrl = properties.getTargetUrl();
        String billShockUrl = properties.getBillShockUrl();
        int rate = properties.getRequestRatePerSecond();
        int s3ReqInterval = properties.getBillShockS3RequestIntervalInMillis();
        int s3DownloadReqInterval = properties.getBillShockS3DownloadRequestIntervalInMillis();
        int s3DeleteInterval = properties.getBillShockS3DeleteRequestIntervalInMillis();
        int dynReqInterval = properties.getBillShockDynamoDbRequestIntervalInMillis();
        int dynGetReqInterval = properties.getBillShockDynamoDBGetRequestIntervalInMillis();
        int dynDeleteInterval = properties.getBillShockDynamoDBDeleteRequestIntervalInMillis();
//        int ec2ReqInterval = properties.getBillShockEC2RequestIntervalInMillis();
//        int rdsReqInterval = properties.getBillShockRDSRequestIntervalInMillis();

        if (targetUrl == null || targetUrl.trim().isEmpty()) {
            log.warn("Target URL not configured. Load testing will not start. Set LOADTESTER_TARGET_URL environment variable.");
            return;
        }
        if (rate <= 0) {
            log.warn("Request rate per second is not configured or is invalid ({}). Load testing will not start. Set LOADTESTER_REQUEST_RATE_PER_SECOND environment variable to a positive integer.", rate);
            return;
        }

        log.info("Starting load testing on {} at {} requests/second", targetUrl, rate);

        // Use a scheduled executor to submit tasks at the desired rate
        scheduler = Executors.newScheduledThreadPool(6);
        long intervalMillis = 1000L / rate;

        scheduler.scheduleAtFixedRate(this::sendRequest, 0, intervalMillis, TimeUnit.MILLISECONDS);

        // Schedule bill shock requests
        if (billShockUrl != null && !billShockUrl.trim().isEmpty()) {
            log.info("Scheduling bill shock requests for {}", billShockUrl);

            if (s3ReqInterval > 0) {
                String s3Body = configService.getRequestBody("s3");
                scheduler.scheduleAtFixedRate(() -> performPostRequest(billShockUrl + "/s3", s3Body), 0, s3ReqInterval, TimeUnit.MILLISECONDS);
                log.info("  - S3 POST requests every {} ms", s3ReqInterval);
            }

            if (s3DownloadReqInterval > 0) {
                String s3Body = configService.getRequestBody("s3Download");
                scheduler.scheduleAtFixedRate(() -> performPostRequest(billShockUrl + "/s3-download", s3Body), 0, s3DownloadReqInterval, TimeUnit.MILLISECONDS);
                log.info("  - S3Download POST requests every {} ms", s3DownloadReqInterval);
            }

            if (s3DeleteInterval > 0) {
                String s3Body = configService.getRequestBody("s3Download");
                scheduler.scheduleAtFixedRate(() -> performPostRequest(billShockUrl + "/s3-delete-all", s3Body), 0, s3DeleteInterval, TimeUnit.MILLISECONDS);
                log.info("  - S3Delete POST requests every {} ms", s3DeleteInterval);
            }

            if (dynReqInterval > 0) {
                String dynBody = configService.getRequestBody("dynamodb");
                scheduler.scheduleAtFixedRate(() -> performPostRequest(billShockUrl + "/dynamodb", dynBody), 0, dynReqInterval, TimeUnit.MILLISECONDS);
                log.info("  - DynamoDB POST requests every {} ms", dynReqInterval);
            }

            if (dynGetReqInterval > 0) {
                String dynBody = configService.getRequestBody("dynamodbGet");
                scheduler.scheduleAtFixedRate(() -> performPostRequest(billShockUrl + "/dynamodb-get-all-items", dynBody), 0, dynGetReqInterval, TimeUnit.MILLISECONDS);
                log.info("  - DynamoDBGet POST requests every {} ms", dynGetReqInterval);
            }

            if (dynDeleteInterval > 0) {
                String dynBody = configService.getRequestBody("dynamodb");
                scheduler.scheduleAtFixedRate(() -> performPostRequest(billShockUrl + "/dynamodb-delete-all", dynBody), 0, dynDeleteInterval, TimeUnit.MILLISECONDS);
                log.info("  - DynamoDBDelete POST requests every {} ms", dynDeleteInterval);
            }

            //            if (ec2ReqInterval > 0) {
            //                scheduler.scheduleAtFixedRate(() -> performGetRequest(billShockUrl + "/ec2"), 0, ec2ReqInterval, TimeUnit.MILLISECONDS);
            //                log.info("  - EC2 GET requests every {} ms", ec2ReqInterval);
            //            }
            //            if (rdsReqInterval > 0) {
            //                scheduler.scheduleAtFixedRate(() -> performGetRequest(billShockUrl + "/rds"), 0, rdsReqInterval, TimeUnit.MILLISECONDS);
            //                log.info("  - RDS GET requests every {} ms", rdsReqInterval);
            //            }
        }
    }

    private void sendRequest() {
        performGetRequest(properties.getTargetUrl());
    }

    private void performGetRequest(String url) {
        webClient.get()
                .uri(url)
                .retrieve()
                .toEntity(String.class) // Retrieve as entity to get headers/status without body processing
                .subscribe(
                        responseEntity -> {
                            metricService.incrementRequests();
                            // Here we assume the response body is empty (Void.class)
                            // If you expect a body, you can use responseEntity.getBody() to read it
                            // and measure its size. For example, if the body is a String:
                            String body = responseEntity.getBody();
                            long bodySize = body != null ? body.length() : 0;
                            metricService.incrementBytes(bodySize);
                            long contentLength = responseEntity.getHeaders().getContentLength();
                            if (contentLength > 0) {
                                metricService.incrementBytes(contentLength);
                            } else {
                                // If Content-Length is not available, try to get body size if needed
                                // For simplicity, we'll just count header bytes or assume 0 if no body/length
                                // A more robust approach would read the body and measure its size
                                log.debug("Content-Length header not available for {}", url);
                                // If you need body size when Content-Length is missing:
                                // responseEntity.getBody() would be null for Void.class
                                // You'd need to retrieve as byte[] or String and measure
                            }
                            log.debug("Request successful to {}. Status: {}", url, responseEntity.getStatusCode());
                        },
                        error -> {
                            // Request failed (connection error, timeout, etc.)
                            metricService.incrementRequests(); // Still count as an attempted request
                            log.error("Request failed to {}: {}", url, error.getMessage());
                        }
                );
    }

    private void performPostRequest(String url, String requestBody) {
        webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .toEntity(String.class)
                .subscribe(
                        responseEntity -> {
                            metricService.incrementRequests();
                            String responseBody = responseEntity.getBody();
                            long bodySize = responseBody != null ? responseBody.length() : 0;
                            metricService.incrementBytes(bodySize);
                            log.debug("POST Request successful to {}. Status: {}", url, responseEntity.getStatusCode());
                        },
                        error -> {
                            metricService.incrementRequests();
                            log.error("POST Request failed to {}: {}", url, error.getMessage());
                        }
                );
    }

    @PreDestroy
    public void stopLoadTesting() {
        if (scheduler != null && !scheduler.isShutdown()) {
            log.info("Stopping load testing scheduler.");
            scheduler.shutdownNow(); // Attempt to stop immediately
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Scheduler did not terminate cleanly within 5 seconds.");
                }
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for scheduler termination.", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
