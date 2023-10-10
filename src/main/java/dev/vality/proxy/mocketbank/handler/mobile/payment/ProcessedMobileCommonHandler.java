package dev.vality.proxy.mocketbank.handler.mobile.payment;

import dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors;
import dev.vality.adapter.common.mapper.ErrorMapping;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.PaymentResource;
import dev.vality.proxy.mocketbank.constant.PaymentState;
import dev.vality.proxy.mocketbank.handler.mobile.CommonMobileHandler;
import dev.vality.proxy.mocketbank.utils.CreatorUtils;
import dev.vality.proxy.mocketbank.utils.mobilephone.MobilePhone;
import dev.vality.proxy.mocketbank.utils.mobilephone.MobilePhoneAction;
import dev.vality.proxy.mocketbank.utils.mobilephone.MobilePhoneUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedMobileCommonHandler implements CommonMobileHandler {

    private final List<MobilePhone> mobilePhones;
    private final ErrorMapping errorMapping;

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetProcessed();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        PaymentResource paymentResource =
                dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors
                        .extractPaymentResource(context);
        PaymentTool paymentTool = ProxyProviderPackageExtractors.extractPaymentTool(paymentResource);
        String phoneNumber = MobilePhoneUtils.preparePhoneNumber(paymentTool.getMobileCommerce().getPhone());
        Optional<MobilePhone> mobilePhone = MobilePhoneUtils.extractPhoneByNumber(mobilePhones, phoneNumber);

        if (!mobilePhone.isPresent()) {
            String error = MobilePhoneAction.UNSUPPORTED_PHONE.getAction();
            return createProxyResultFailure(errorMapping.mapFailure(error, error));
        }

        MobilePhoneAction mobilePhoneAction = MobilePhoneAction.findByValue(mobilePhone.get().getAction());
        if (MobilePhoneAction.isFailedAction(mobilePhoneAction.getAction())) {
            String error = mobilePhoneAction.getAction();
            return createProxyResultFailure(errorMapping.mapFailure(error, error));
        }

        TransactionInfo transactionInfo = CreatorUtils.createDefaultTransactionInfo(context);
        return createPaymentProxyResult(createFinishIntentSuccess(), PaymentState.CAPTURED.getBytes(), transactionInfo);
    }

}
