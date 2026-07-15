package com.codeit.hrbank.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI hrbankOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("4팀 포그라운드 HR Bank API.")
            .description("4팀의 HR Bank API 문서.")
            .version("v1.0.0")
            .contact(new Contact()
                .name("github - sb13-HRBank-team04")
                .url("https://github.com/Sprint-4team/sb13-HRBank-team04")
            ));
  }
}
