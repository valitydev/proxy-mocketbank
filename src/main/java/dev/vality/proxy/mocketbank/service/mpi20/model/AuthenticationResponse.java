package dev.vality.proxy.mocketbank.service.mpi20.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    private String threeDSServerTransID;

    @JsonUnwrapped
    private Error error;

    private String transStatus;

    private String acsUrl;

    private String creq;

}
