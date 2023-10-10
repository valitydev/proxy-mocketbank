package dev.vality.proxy.mocketbank.configuration;

import dev.vality.adapter.common.hellgate.HellgateClient;
import dev.vality.damsel.proxy_provider.ProviderProxyHostSrv;
import dev.vality.proxy.mocketbank.configuration.properties.HellgateClientStorageProperties;
import dev.vality.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class HellgateClientConfig {

    @Bean
    public HellgateClient hellgateClient(HellgateClientStorageProperties properties) throws IOException {
        return new HellgateClient(getProviderProxyHostSrv(properties));
    }

    private ProviderProxyHostSrv.Iface getProviderProxyHostSrv(HellgateClientStorageProperties properties)
            throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(properties.getUrl().getURI())
                .withNetworkTimeout(properties.getNetworkTimeout())
                .build(ProviderProxyHostSrv.Iface.class);
    }
}
