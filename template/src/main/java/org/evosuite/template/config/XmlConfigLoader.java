/*
 * XStream 加载 XML 配置
 */
package org.evosuite.template.config;

import java.io.File;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Loads test data configuration from XML files using XStream.
 * XStream is already in EvoSuite's dependency tree (BSD license).
 */
public class XmlConfigLoader implements ConfigLoader {

    private final XStream xstream;

    public XmlConfigLoader() {
        this.xstream = new XStream(new DomDriver());
        // Configure XStream aliases for cleaner XML output
        XStream.setupDefaultSecurity(xstream);
        xstream.allowTypesByWildcard(new String[] { "org.evosuite.template.**" });
        xstream.alias("testDataConfig", TestDataConfig.class);
        xstream.alias("testCase", TestCaseConfig.class);
        xstream.alias("methodCall", TestMethodConfig.class);
    }

    @Override
    public TestDataConfig load(File configFile) throws ConfigLoadException {
        if (configFile == null || !configFile.exists()) {
            throw new ConfigLoadException("Config file not found: " + configFile);
        }
        try {
            return (TestDataConfig) xstream.fromXML(configFile);
        } catch (Exception e) {
            throw new ConfigLoadException(
                    "Failed to load XML config from: " + configFile.getAbsolutePath(), e);
        }
    }

    @Override
    public boolean supports(File configFile) {
        if (configFile == null) {
            return false;
        }
        String name = configFile.getName().toLowerCase();
        return name.endsWith(".xml");
    }
}