package com.example.loadtester.web;

import com.example.loadtester.service.MetricService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MetricsController {

    private final MetricService metricService;

    public MetricsController(MetricService metricService) {
        this.metricService = metricService;
    }

    @GetMapping("/metrics/current")
    public Map<String, Double> getCurrentMetrics() {
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("requests.total", metricService.getTotalRequests());
        metrics.put("bytes.total", metricService.getTotalBytes());
        metrics.put("timestamp", (double) System.currentTimeMillis()); // Add timestamp for rate calculation
        return metrics;
    }

    @GetMapping(value = "/metrics", produces = "text/plain; version=0.0.4; charset=utf-8")
    // This endpoint returns metrics for prometheus to scrape
    // The content type is set to "text/plain" with Prometheus format
    public String getMetrics() {
        StringBuilder sb = new StringBuilder();

        // Format for requestsCounter
        sb.append("# HELP ").append(MetricService.REQUESTS_METRIC_NAME).append(" ").append(MetricService.REQUESTS_METRIC_DESCRIPTION).append("\n");
        sb.append("# TYPE ").append(MetricService.REQUESTS_METRIC_NAME).append(" counter\n");
        sb.append(MetricService.REQUESTS_METRIC_NAME).append(" ").append(metricService.getTotalRequests()).append("\n\n");

        // Format for bytesCounter
        sb.append("# HELP ").append(MetricService.BYTES_METRIC_NAME).append(" ").append(MetricService.BYTES_METRIC_DESCRIPTION).append("\n");
        sb.append("# TYPE ").append(MetricService.BYTES_METRIC_NAME).append(" counter\n");
        sb.append(MetricService.BYTES_METRIC_NAME).append(" ").append(metricService.getTotalBytes()).append("\n");
        return sb.toString();
    }
}
