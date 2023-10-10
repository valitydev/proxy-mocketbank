package dev.vality.proxy.mocketbank.service.mpi20.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.damsel.proxy_provider.Cash;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.proxy.mocketbank.configuration.properties.Mpi20Properties;
import dev.vality.proxy.mocketbank.service.mpi20.model.AuthenticationRequest;
import dev.vality.proxy.mocketbank.service.mpi20.model.SessionState;
import dev.vality.proxy.mocketbank.utils.UrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CtxToAuthConverter implements Converter<PaymentContext, AuthenticationRequest> {

    private final CdsStorageClient cds;
    private final ObjectMapper objectMapper;
    private final Mpi20Properties mpi20Properties;

    @Override
    @SneakyThrows
    public AuthenticationRequest convert(PaymentContext context) {
        SessionState sessionState = objectMapper.readValue(context.getSession().getState(), SessionState.class);

        CardDataProxyModel cardData = cds.getCardData(context);

        Cash cost = context.getPaymentInfo().getPayment().getCost();
        return AuthenticationRequest.builder()
                .threeDSServerTransID(sessionState.getTransactionId())
                .pan(cardData.getPan())
                .cardholderName(cardData.getCardholderName())
                .expDate(cardData.getExpYear() + " " + cardData.getExpMonth())
                .notificationUrl(UrlUtils.getCallbackUrl(
                        mpi20Properties.getCallbackUrl(),
                        mpi20Properties.getAcsNotificationPath()))
                .amount(String.valueOf(cost.getAmount()))
                .currency(cost.getCurrency().getSymbolicCode())
                .build();
    }
}
