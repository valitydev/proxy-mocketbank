package dev.vality.proxy.mocketbank.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "cds.client.storage")
@Validated
@Getter
@Setter
public class CdsClientStorageProperties {

    @NotNull
    private Resource url;
    @NotNull
    private int networkTimeout = 5000;

}
