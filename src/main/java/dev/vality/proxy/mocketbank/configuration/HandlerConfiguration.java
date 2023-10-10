package dev.vality.proxy.mocketbank.configuration;

import dev.vality.proxy.mocketbank.decorator.*;
import dev.vality.proxy.mocketbank.handler.digital.wallet.DigitalWalletServerHandler;
import dev.vality.proxy.mocketbank.handler.mobile.MobileServerHandler;
import dev.vality.proxy.mocketbank.handler.mobile.operator.MobileOperatorServerHandler;
import dev.vality.proxy.mocketbank.handler.oct.OctServerHandler;
import dev.vality.proxy.mocketbank.handler.payment.PaymentServerHandler;
import dev.vality.proxy.mocketbank.handler.terminal.TerminalServerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class HandlerConfiguration {

    @Bean
    @Primary
    public WithdrawalServerHandlerLog withdrawalServerHandlerLog(OctServerHandler octServerHandler) {
        return new WithdrawalServerHandlerLog(octServerHandler);
    }

    @Bean
    public MobileOperatorServerHandlerLog mobileOperatorServerHandlerLog(
            MobileOperatorServerHandler mobileOperatorServerHandler) {
        return new MobileOperatorServerHandlerLog(mobileOperatorServerHandler);
    }

    @Bean
    public MobileServerHandlerLog mobileServerHandlerLog(MobileServerHandler mobileServerHandler) {
        return new MobileServerHandlerLog(mobileServerHandler);
    }

    @Bean
    public TerminalServerHandlerLog terminalServerHandlerLog(TerminalServerHandler terminalServerHandler) {
        return new TerminalServerHandlerLog(terminalServerHandler);
    }

    @Bean
    public DigitalWalletServerHandlerLog digitalWalletServerHandlerLog(
            DigitalWalletServerHandler digitalWalletServerHandler) {
        return new DigitalWalletServerHandlerLog(digitalWalletServerHandler);
    }

    @Bean
    public PaymentServerHandlerMdcLog paymentServerHandlerLog(PaymentServerHandler paymentServerHandler) {
        return new PaymentServerHandlerMdcLog(new PaymentServerHandlerLog(paymentServerHandler));
    }

}
