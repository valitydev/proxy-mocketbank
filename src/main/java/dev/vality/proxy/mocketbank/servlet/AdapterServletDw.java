package dev.vality.proxy.mocketbank.servlet;

import dev.vality.damsel.proxy_provider.ProviderProxySrv;
import dev.vality.proxy.mocketbank.decorator.DigitalWalletServerHandlerLog;
import dev.vality.woody.thrift.impl.http.THServiceBuilder;
import lombok.RequiredArgsConstructor;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@RequiredArgsConstructor
@WebServlet("/proxy/mocketbank/dw")
public class AdapterServletDw extends GenericServlet {

    private final transient DigitalWalletServerHandlerLog handler;
    private transient Servlet servlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servlet = new THServiceBuilder().build(ProviderProxySrv.Iface.class, handler);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        servlet.service(request, response);
    }

}
