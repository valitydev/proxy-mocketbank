package dev.vality.proxy.mocketbank.handler.digital.wallet.callback;

import dev.vality.damsel.proxy_provider.PaymentCallbackResult;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.proxy.mocketbank.constant.PaymentState;
import dev.vality.proxy.mocketbank.utils.CreatorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DigitalWalletCallbackHandler {

    public PaymentCallbackResult handler(ByteBuffer byteBuffer, PaymentContext context) {
        return createCallbackResult("".getBytes(), createCallbackProxyResult(
                createFinishIntentSuccess(), PaymentState.CAPTURED.getBytes(),
                CreatorUtils.createDefaultTransactionInfo(context)
        ));
    }

}
