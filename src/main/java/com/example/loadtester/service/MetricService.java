package com.example.loadtester.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class MetricService {
    public static final String REQUESTS_METRIC_DESCRIPTION = "Total number of GET requests sent";
    public static final String BYTES_METRIC_DESCRIPTION = "Total number of bytes received in responses";
    public static final String REQUESTS_METRIC_NAME = "loadtester_requests_total";  
    public static final String BYTES_METRIC_NAME = "loadtester_bytes_total";




    private final MeterRegistry meterRegistry;

    private Counter requestsCounter;
    private Counter bytesCounter;

    public MetricService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        requestsCounter = Counter.builder("loadtester.requests.total")
                .description("Total number of GET requests sent")
                .register(meterRegistry);

        bytesCounter = Counter.builder("loadtester.bytes.total")
                .description("Total number of bytes received in responses")
                .register(meterRegistry);
    }

    public void incrementRequests() {
        if (requestsCounter != null) {
            requestsCounter.increment();
        }
    }

    public void incrementBytes(long bytes) {
        if (bytesCounter != null) {
            bytesCounter.increment(bytes);
        }
    }

    public double getTotalRequests() {
        return requestsCounter != null ? requestsCounter.count() : 0;
    }

    public double getTotalBytes() {
        return bytesCounter != null ? bytesCounter.count() : 0;
    }
}
