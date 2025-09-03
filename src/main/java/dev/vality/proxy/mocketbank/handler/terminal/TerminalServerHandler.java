package dev.vality.proxy.mocketbank.handler.terminal;

import dev.vality.damsel.proxy_provider.*;
import dev.vality.proxy.mocketbank.handler.terminal.payment.UnsupportedTerminalHandler;
import dev.vality.proxy.mocketbank.validator.TerminalValidator;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TerminalServerHandler implements ProviderProxySrv.Iface {

    private final TerminalValidator terminalValidator;
    private final List<CommonTerminalHandler> handlers;

    @Override
    public PaymentProxyResult processPayment(PaymentContext context) throws TException {
        terminalValidator.validate(context, context.getOptions());
        return handlers.stream()
                .filter(handler -> handler.filter(
                        context.getSession().getTarget(),
                        context.getPaymentInfo().getPayment().getPaymentResource()
                ))
                .findFirst()
                .orElse(new UnsupportedTerminalHandler())
                .handler(context);
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer callback, PaymentContext context) throws TException {
        throw new TException("Method Not Supported");
    }
}
