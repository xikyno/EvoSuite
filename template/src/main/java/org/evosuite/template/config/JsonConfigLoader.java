/*
 *  Jackson 一行搞定：mapper.readValue(file, TestDataConfig.class)
 */
package org.evosuite.template.config;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Loads test data configuration from JSON files using Jackson.
 * Pure Java implementation, no native dependencies, works offline.
 */
public class JsonConfigLoader implements ConfigLoader {

    private final ObjectMapper mapper;

    public JsonConfigLoader() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public TestDataConfig load(File configFile) throws ConfigLoadException {
        if (configFile == null || !configFile.exists()) {
            throw new ConfigLoadException("Config file not found: " + configFile);
        }
        try {
            return mapper.readValue(configFile, TestDataConfig.class);
        } catch (IOException e) {
            throw new ConfigLoadException(
                    "Failed to load JSON config from: " + configFile.getAbsolutePath(), e);
        }
    }

    @Override
    public boolean supports(File configFile) {
        if (configFile == null) {
            return false;
        }
        String name = configFile.getName().toLowerCase();
        return name.endsWith(".json");
    }
}