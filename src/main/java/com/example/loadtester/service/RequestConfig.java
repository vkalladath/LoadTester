package com.example.loadtester.service;

import java.util.Map;

public class RequestConfig {
    private Map<String, String> bodies;

    public Map<String, String> getBodies() {
        return bodies;
    }

    public void setBodies(Map<String, String> bodies) {
        this.bodies = bodies;
    }
}
