package com.github.lihaans.esimporter.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public final class ConfigLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ConfigLoader() {
    }

    public static JobConfig load(String path) throws IOException {
        JobConfig config = MAPPER.readValue(new File(path), JobConfig.class);
        validate(config);
        return config;
    }

    private static void validate(JobConfig config) {
        require(config.getJobName(), "jobName");
        require(config.getInputDir(), "inputDir");
        require(config.getIndexName(), "indexName");
        if (config.getEsHosts() == null || config.getEsHosts().isEmpty()) {
            throw new IllegalArgumentException("esHosts must not be empty");
        }
    }

    private static void require(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
