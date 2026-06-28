package edu.university.lab.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.sample-data")
public class SampleDataProperties {

    private boolean enabled;

    private String resource = "classpath:sample-data/demo-data.json";
}
