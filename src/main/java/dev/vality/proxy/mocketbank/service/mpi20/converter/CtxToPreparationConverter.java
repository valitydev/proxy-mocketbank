package dev.vality.proxy.mocketbank.service.mpi20.converter;

import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.proxy.mocketbank.configuration.properties.Mpi20Properties;
import dev.vality.proxy.mocketbank.service.mpi20.constant.CallbackResponseFields;
import dev.vality.proxy.mocketbank.service.mpi20.model.PreparationRequest;
import dev.vality.proxy.mocketbank.utils.UrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CtxToPreparationConverter implements Converter<PaymentContext, PreparationRequest> {

    private final CdsStorageClient cds;
    private final Mpi20Properties mpi20Properties;

    @Override
    @SneakyThrows
    public PreparationRequest convert(PaymentContext context) {

        CardDataProxyModel cardData = cds.getCardData(context);

        return PreparationRequest.builder()
                .pan(cardData.getPan())
                .notificationUrl(URLEncoder.encode(getNotificationUrl(context), StandardCharsets.UTF_8))
                .build();
    }

    private String getNotificationUrl(PaymentContext context) {
        String terminationUri = UrlUtils.hasRedirectUrl(context.getPaymentInfo().getPayment())
                ? context.getPaymentInfo().getPayment().getPayerSessionInfo().getRedirectUrl()
                : mpi20Properties.getReturnUrl();

        return UrlUtils.getCallbackUrl(
                mpi20Properties.getCallbackUrl(),
                mpi20Properties.getThreeDsMethodNotificationPath(),
                CallbackResponseFields.TERMINATION_URI,
                terminationUri
        );
    }
}
