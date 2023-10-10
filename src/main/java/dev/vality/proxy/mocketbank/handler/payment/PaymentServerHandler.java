package dev.vality.proxy.mocketbank.handler.payment;

import dev.vality.damsel.proxy_provider.*;
import dev.vality.proxy.mocketbank.handler.payment.callback.PaymentCallbackHandler;
import dev.vality.proxy.mocketbank.handler.payment.callback.RecurrentTokenCallbackHandler;
import dev.vality.proxy.mocketbank.handler.payment.recurrent.GenerateTokenHandler;
import dev.vality.proxy.mocketbank.validator.PaymentValidator;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentServerHandler implements ProviderProxySrv.Iface {

    private final List<CommonPaymentHandler> handlers;
    private final PaymentCallbackHandler paymentCallbackHandler;
    private final GenerateTokenHandler generateTokenHandler;
    private final RecurrentTokenCallbackHandler recurrentTokenCallbackHandler;
    private final PaymentValidator paymentValidator;

    @Override
    public RecurrentTokenProxyResult generateToken(RecurrentTokenContext context) {
        return generateTokenHandler.handler(context);
    }

    @Override
    public RecurrentTokenCallbackResult handleRecurrentTokenCallback(
            ByteBuffer callback,
            RecurrentTokenContext context) {
        return recurrentTokenCallbackHandler.handler(callback, context);
    }

    @Override
    public PaymentProxyResult processPayment(PaymentContext context) throws TException {
        paymentValidator.validate(context, context.getOptions());
        return handlers.stream()
                .filter(handler -> handler.filter(
                        context.getSession().getTarget(),
                        context.getPaymentInfo().getPayment().getPaymentResource()
                ))
                .findFirst()
                .orElse(new FallbackPaymentHandler())
                .handler(context);
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer callback, PaymentContext context) {
        return paymentCallbackHandler.handler(callback, context);
    }
}
