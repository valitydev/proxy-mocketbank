package dev.vality.proxy.mocketbank.service.mpi20.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.proxy.mocketbank.service.mpi20.model.ResultRequest;
import dev.vality.proxy.mocketbank.service.mpi20.model.SessionState;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CtxToResultConverter implements Converter<PaymentContext, ResultRequest> {

    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public ResultRequest convert(PaymentContext context) {
        SessionState sessionState = objectMapper.readValue(context.getSession().getState(), SessionState.class);

        return ResultRequest.builder()
                .threeDSServerTransID(sessionState.getTransactionId())
                .build();
    }
}
