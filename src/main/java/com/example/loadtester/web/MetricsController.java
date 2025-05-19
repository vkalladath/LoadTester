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
}
