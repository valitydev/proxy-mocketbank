package dev.vality.proxy.mocketbank.decorator;

import dev.vality.adapter.common.damsel.ProxyProviderVerification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerHandlerLogUtils {

    public static final String MESSAGE_TEMPLATE = "Class {}, message {} ";

    public static void logMessage(Exception ex, String message, Class<?> className) {
        if (ProxyProviderVerification.isUndefinedResultOrUnavailable(ex)) {
            log.warn(MESSAGE_TEMPLATE, className, message, ex);
        } else {
            log.error(MESSAGE_TEMPLATE, className, message, ex);
        }
    }

}
