package dev.vality.proxy.mocketbank.handler.mobile.operator;

import dev.vality.mnp.MnpSrv;
import dev.vality.mnp.Operator;
import dev.vality.mnp.RequestParams;
import dev.vality.mnp.ResponseData;
import dev.vality.proxy.mocketbank.exception.MobileOperatorException;
import dev.vality.proxy.mocketbank.utils.mobilephone.MobilePhone;
import dev.vality.proxy.mocketbank.utils.mobilephone.MobilePhoneUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MobileOperatorServerHandler implements MnpSrv.Iface {

    private final List<MobilePhone> mobilePhones;

    @Override
    public ResponseData lookup(RequestParams requestParams) {
        String phoneNumber = MobilePhoneUtils.preparePhoneNumber(requestParams.getPhone());
        Optional<MobilePhone> mobilePhone = MobilePhoneUtils.extractPhoneByNumber(mobilePhones, phoneNumber);
        if (!mobilePhone.isPresent()) {
            throw new MobileOperatorException(
                    String.format("Phone number %s not correct or not supported", phoneNumber));
        }
        return new ResponseData(Operator.valueOf(mobilePhone.get().getOperator()));
    }

}
