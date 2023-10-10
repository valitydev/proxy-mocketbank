package dev.vality.proxy.mocketbank.configuration;

import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.cds.storage.StorageSrv;
import dev.vality.proxy.mocketbank.configuration.properties.CdsClientStorageProperties;
import dev.vality.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class CdsStorageConfig {

    @Bean
    public CdsStorageClient cdsStorageClient(CdsClientStorageProperties properties) throws IOException {
        return new CdsStorageClient(getStorageSrv(properties));
    }

    private StorageSrv.Iface getStorageSrv(CdsClientStorageProperties properties) throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(properties.getUrl().getURI())
                .withNetworkTimeout(properties.getNetworkTimeout())
                .build(StorageSrv.Iface.class);
    }
}
