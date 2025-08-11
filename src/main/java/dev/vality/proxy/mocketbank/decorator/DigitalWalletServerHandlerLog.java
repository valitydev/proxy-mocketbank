package dev.vality.proxy.mocketbank.decorator;

import dev.vality.adapter.common.damsel.PaymentResourceTypeExtractors;
import dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors;
import dev.vality.damsel.proxy_provider.PaymentCallbackResult;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.ProviderProxySrv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;

@Slf4j
@RequiredArgsConstructor
public class DigitalWalletServerHandlerLog implements ProviderProxySrv.Iface {

    private final ProviderProxySrv.Iface handler;

    @Override
    public PaymentProxyResult processPayment(PaymentContext context) throws TException {
        String invoiceId = ProxyProviderPackageExtractors.extractInvoiceId(context);
        String invoicePaymentStatus = ProxyProviderPackageExtractors.extractTargetInvoicePaymentStatus(context);
        String paymentResourceType = PaymentResourceTypeExtractors.extractPaymentResourceType(context);
        log.info("Process payment handle resource={}, status={} start with invoiceId={}",
                paymentResourceType, invoicePaymentStatus, invoiceId);
        try {
            PaymentProxyResult proxyResult = handler.processPayment(context);
            log.info("Process payment handle resource={}, status={} finished with invoiceId={} and proxyResult={}",
                    paymentResourceType, invoicePaymentStatus, invoiceId, proxyResult);
            return proxyResult;
        } catch (Exception ex) {
            String message = String.format(
                    "Failed handle resource=%s, status=%s process payment for operation with invoiceId %s",
                    paymentResourceType, invoicePaymentStatus, invoiceId);
            ServerHandlerLogUtils.logMessage(ex, message, this.getClass());
            throw ex;
        }
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(
            ByteBuffer byteBuffer,
            PaymentContext context) throws TException {
        String invoiceId = ProxyProviderPackageExtractors.extractInvoiceId(context);
        log.info("HandlePaymentCallback start with invoiceId={}", invoiceId);
        PaymentCallbackResult result = handler.handlePaymentCallback(byteBuffer, context);
        log.info("HandlePaymentCallback finish {} with invoiceId={}", result, invoiceId);
        return result;
    }

}
