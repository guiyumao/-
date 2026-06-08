package edu.university.lab.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "openapi")
public class OpenApiProperties {

    private String title;

    private String version;

    private String description;

    private String contactName;

    private String contactEmail;

    private String externalDocsDescription;

    private String externalDocsUrl;
}
