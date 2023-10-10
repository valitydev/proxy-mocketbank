package dev.vality.proxy.mocketbank.decorator;

import dev.vality.mnp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;

@Slf4j
@RequiredArgsConstructor
public class MobileOperatorServerHandlerLog implements MnpSrv.Iface {

    private final MnpSrv.Iface handler;

    @Override
    public ResponseData lookup(RequestParams requestParams) throws BadPhoneFormat, OperatorNotFound, TException {
        log.info("Lookup: start with requestParams={}", requestParams);
        try {
            ResponseData responseData = handler.lookup(requestParams);
            log.info("Lookup: finish {} with requestParams={}", responseData, requestParams);
            return responseData;
        } catch (Exception ex) {
            String message = String.format("Failed Lookup with requestParams=%s", requestParams);
            ServerHandlerLogUtils.logMessage(ex, message, this.getClass());
            throw ex;
        }
    }


}
