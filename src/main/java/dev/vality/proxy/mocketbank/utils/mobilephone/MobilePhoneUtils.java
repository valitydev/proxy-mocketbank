package dev.vality.proxy.mocketbank.utils.mobilephone;

import dev.vality.mnp.PhoneNumber;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MobilePhoneUtils {

    public static Optional<MobilePhone> extractPhoneByNumber(List<MobilePhone> list, String phoneNumber) {
        return list.stream().filter(phone -> phone.getNumber().equals(phoneNumber)).findFirst();
    }

    public static String preparePhoneNumber(PhoneNumber phoneNumber) {
        return String.format("%s%s", phoneNumber.getCc(), phoneNumber.getCtn());
    }

    public static String preparePhoneNumber(dev.vality.damsel.domain.MobilePhone mobilePhone) {
        return String.format("%s%s", mobilePhone.getCc(), mobilePhone.getCtn());
    }

}
