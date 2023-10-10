package dev.vality.proxy.mocketbank.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentState {

    public static final String INIT = "init";
    public static final String PAY = "block";
    public static final String SLEEP = "sleep";
    public static final String CAPTURED = "captured";
    public static final String REDIRECT = "redirect";
    public static final String PENDING = "pending";
    public static final String CANCELLED = "cancelled";
    public static final String REFUNDED = "refunded";
    public static final String CONFIRM = "confirm";
    public static final String RECURRENT_INIT = "recurrent_init";

}