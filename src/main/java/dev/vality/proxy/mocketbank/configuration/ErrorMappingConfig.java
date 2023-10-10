package dev.vality.proxy.mocketbank.configuration;

import dev.vality.adapter.common.component.SimpleErrorMapping;
import dev.vality.adapter.common.mapper.ErrorMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class ErrorMappingConfig {

    @Value("${error-mapping.file-path}")
    private Resource errorMappingFilePath;

    @Value("${error-mapping.pattern:\"'%s' - '%s'\"}")
    private String errorMappingPattern;

    @Bean
    @Primary
    public ErrorMapping errorMapping() throws IOException {
        return new SimpleErrorMapping(errorMappingFilePath, errorMappingPattern)
                .createErrorMapping();
    }
}
