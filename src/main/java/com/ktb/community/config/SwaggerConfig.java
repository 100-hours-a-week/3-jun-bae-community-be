package com.ktb.community.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration    // 스프링 실행시 설정파일 읽어드리기 위한 어노테이션
public class SwaggerConfig {

//    @Bean
//    public OpenAPI openAPI() {
//        return new OpenAPI()
//                .components(new Components().addSecuritySchemes("basicScheme", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
//                .info(apiInfo());
//    }

    @Bean
    public OpenAPI JWTOpenAPI() {
        // Security Scheme 정의
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // Security Requirement 정의
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("BearerAuth");

        return new OpenAPI()
                .info(new Info().title("Todolist API")
                        .description("Todolist Application API Documentation")
                        .version("v1.0"))
                .addSecurityItem(securityRequirement)  // Security Requirement 추가
                .schemaRequirement("BearerAuth", securityScheme);  // Security Scheme 추가
    }

    private Info apiInfo() {
        return new Info()
                .title("KTB Community Swagger")
                .description("Community REST API")
                .version("1.0.0");
    }


}
