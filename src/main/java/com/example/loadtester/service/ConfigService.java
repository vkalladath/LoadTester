package com.example.loadtester.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class ConfigService {

    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);
    private RequestConfig requestConfig;

    public ConfigService() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = ConfigService.class.getResourceAsStream("/request-config.json");
            if (inputStream == null) {
                throw new IOException("request-config.json not found");
            }
            requestConfig = mapper.readValue(inputStream, RequestConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Exception in mapping the request body", e);
        }
    }

    public String getRequestBody(String service) {
        return requestConfig.getBodies().get(service);
    }
}
