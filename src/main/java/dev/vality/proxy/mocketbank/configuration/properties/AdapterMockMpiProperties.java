package dev.vality.proxy.mocketbank.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties("adapter-mock-mpi")
public class AdapterMockMpiProperties {

    private @NotEmpty String url;
    private String callbackUrl;
    private String pathCallbackUrl;
    private String pathRecurrentCallbackUrl;
    private String tagPrefix;

}
