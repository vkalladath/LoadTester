package com.example.loadtester.service;

import com.example.loadtester.config.LoadTesterProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class LoadTestingService {

    private static final Logger log = LoggerFactory.getLogger(LoadTestingService.class);

    private final LoadTesterProperties properties;
    private final MetricService metricService;
    private final WebClient webClient;

    private ScheduledExecutorService scheduler;

    public LoadTestingService(LoadTesterProperties properties, MetricService metricService, WebClient.Builder webClientBuilder) {
        this.properties = properties;
        this.metricService = metricService;
        // Configure WebClient with a timeout
        this.webClient = webClientBuilder
                .baseUrl(properties.getTargetUrl())
                //.responseTimeout(Duration.ofSeconds(10)) // Example timeout
                .build();
    }

    @PostConstruct
    public void startLoadTesting() {
        String targetUrl = properties.getTargetUrl();
        int rate = properties.getRequestRatePerSecond();

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
        // This schedules tasks to start every 1000/rate milliseconds.
        // If a task takes longer than this interval, the executor will queue them up.
        // For more sophisticated rate limiting, consider libraries like Resilience4j.
        scheduler = Executors.newSingleThreadScheduledExecutor();
        long intervalMillis = 1000L / rate;

        scheduler.scheduleAtFixedRate(this::sendRequest, 0, intervalMillis, TimeUnit.MILLISECONDS);
    }

    private void sendRequest() {

            // webClient.get()
            //     .uri(properties.getTargetUrl())
            //     .retrieve() // retrieve() is preferred for simple body extraction and default error handling for 4xx/5xx
            //     .bodyToMono(String.class) // Attempt to convert the response body to String
            //     .timeout(Duration.ofSeconds(1)) // Overall timeout for the operation, including body retrieval
            //     .doOnSuccess(responseBody ->{
            //             metricService.incrementRequests();
            //             metricService.incrementBytes(responseBody.getBytes().length);
            //             log.info("Successfully received response from {}. Body length: {}",
            //                     "uri", responseBody != null ? responseBody.length() : 0);
            //     }
            //     )
            //     .doOnError(WebClientResponseException.class, e -> {
            //         // HTTP status code errors (4xx or 5xx)
            //         log.error("HTTP error for {}: Status {}, Body: {}",
            //                 "uri", e.getStatusCode(), e.getResponseBodyAsString(), e);
            //     })
            //     .doOnError(WebClientRequestException.class, e -> {
            //         // Network errors (connection timeout, unknown host, etc.)
            //         log.error("Request error for {}: {}", "uri", e.getMessage(), e);
            //     })
            //     .doOnError(e -> !(e instanceof WebClientResponseException || e instanceof WebClientRequestException), e -> {
            //         // Other unexpected errors (e.g., decoding errors, timeout from .timeout() operator)
            //         log.error("Unexpected error for {}: {}", "uri", e.getMessage(), e);
            //     })
            //     .onErrorMap(throwable -> {
            //         // Map all errors to a RuntimeException
            //         // This is optional and depends on how you want to handle errors
            //         return new RuntimeException("Error during request", throwable);
            //     });

        webClient.get()
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
                                log.debug("Content-Length header not available for {}", properties.getTargetUrl());
                                // If you need body size when Content-Length is missing:
                                // responseEntity.getBody() would be null for Void.class
                                // You'd need to retrieve as byte[] or String and measure
                            }
                            log.debug("Request successful to {}. Status: {}", properties.getTargetUrl(), responseEntity.getStatusCode());
                        },
                        error -> {
                            // Request failed (connection error, timeout, etc.)
                            metricService.incrementRequests(); // Still count as an attempted request
                            log.error("Request failed to {}: {}", properties.getTargetUrl(), error.getMessage());
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
