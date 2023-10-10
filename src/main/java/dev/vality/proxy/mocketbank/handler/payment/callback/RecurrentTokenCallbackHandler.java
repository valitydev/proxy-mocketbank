package dev.vality.proxy.mocketbank.handler.payment.callback;

import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.adapter.common.damsel.model.Error;
import dev.vality.adapter.common.exception.CdsStorageExpDateException;
import dev.vality.adapter.common.mapper.ErrorMapping;
import dev.vality.damsel.proxy_provider.RecurrentTokenCallbackResult;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import dev.vality.damsel.proxy_provider.RecurrentTokenIntent;
import dev.vality.damsel.proxy_provider.RecurrentTokenProxyResult;
import dev.vality.proxy.mocketbank.constant.PaymentState;
import dev.vality.proxy.mocketbank.service.mpi.MpiApi;
import dev.vality.proxy.mocketbank.service.mpi.constant.TransactionStatus;
import dev.vality.proxy.mocketbank.service.mpi.model.ValidatePaResResponse;
import dev.vality.proxy.mocketbank.utils.Converter;
import dev.vality.proxy.mocketbank.utils.ErrorBuilder;
import dev.vality.proxy.mocketbank.utils.model.Card;
import dev.vality.proxy.mocketbank.utils.model.CardAction;
import dev.vality.proxy.mocketbank.utils.model.CardUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.*;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors.extractRecurrentId;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurrentTokenCallbackHandler {

    private final CdsStorageClient cds;
    private final MpiApi mpiApi;
    private final ErrorMapping errorMapping;
    private final List<Card> cardList;

    public RecurrentTokenCallbackResult handler(ByteBuffer byteBuffer, RecurrentTokenContext context) {
        String recurrentId = extractRecurrentId(context);
        HashMap<String, String> parameters = Converter.mergeParams(byteBuffer, context.getSession().getState());

        CardDataProxyModel cardData;
        try {
            cardData = cds.getCardData(context);
        } catch (CdsStorageExpDateException ex) {
            return ErrorBuilder.prepareRecurrentCallbackError(errorMapping, Error.DEFAULT_ERROR_CODE, ex.getMessage());
        }

        ValidatePaResResponse validatePaResResponse = mpiApi.validatePaRes(cardData, parameters);
        if (TransactionStatus.isAuthenticationSuccessful(validatePaResResponse.getTransactionStatus())) {
            RecurrentTokenIntent intent = createRecurrentTokenFinishIntentSuccess(recurrentId);
            RecurrentTokenProxyResult proxyResult =
                    createRecurrentTokenProxyResult(intent, PaymentState.PENDING.getBytes());
            return createRecurrentTokenCallbackResult("".getBytes(), proxyResult);
        }

        CardAction action = CardUtils.extractActionFromCard(cardList, cardData);
        return ErrorBuilder.prepareRecurrentCallbackError(errorMapping, Error.DEFAULT_ERROR_CODE, action);
    }
}
