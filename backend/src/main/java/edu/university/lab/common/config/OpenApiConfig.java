package edu.university.lab.common.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({OpenApiProperties.class, InitialAdminProperties.class, SampleDataProperties.class})
public class OpenApiConfig {

    private final OpenApiProperties properties;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title(properties.getTitle())
                .version(properties.getVersion())
                .description(properties.getDescription())
                .contact(new Contact()
                    .name(properties.getContactName())
                    .email(properties.getContactEmail())))
            .externalDocs(new ExternalDocumentation()
                .description(properties.getExternalDocsDescription())
                .url(properties.getExternalDocsUrl()));
    }
}
