package edu.university.lab.common.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Laboratory Management System API")
                .version("1.0.0")
                .description("实验室设备与耗材管理系统后端接口文档")
                .contact(new Contact()
                    .name("Laboratory Management Team")
                    .email("lab-admin@university.edu")))
            .externalDocs(new ExternalDocumentation()
                .description("Project README")
                .url("http://localhost:8080/swagger-ui.html"));
    }
}
