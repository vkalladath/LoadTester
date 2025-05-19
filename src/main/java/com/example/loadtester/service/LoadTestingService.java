package com.example.loadtester.service;

import com.example.loadtester.config.LoadTesterProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
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
                .responseTimeout(Duration.ofSeconds(10)) // Example timeout
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
        webClient.get()
                .retrieve()
                .toEntity(Void.class) // Retrieve as entity to get headers/status without body processing
                .subscribe(
                        responseEntity -> {
                            metricService.incrementRequests();
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
