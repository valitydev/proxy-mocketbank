package dev.vality.proxy.mocketbank.service.mpi.model;

import org.springframework.util.MultiValueMap;

public interface PrepareFieldsObject {
    MultiValueMap<String, Object> prepareFields();
}
