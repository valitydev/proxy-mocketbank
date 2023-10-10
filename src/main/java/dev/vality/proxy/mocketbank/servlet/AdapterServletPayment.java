package dev.vality.proxy.mocketbank.servlet;

import dev.vality.damsel.proxy_provider.ProviderProxySrv;
import dev.vality.proxy.mocketbank.decorator.PaymentServerHandlerMdcLog;
import dev.vality.woody.thrift.impl.http.THServiceBuilder;
import lombok.RequiredArgsConstructor;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@RequiredArgsConstructor
@WebServlet("/proxy/mocketbank")
public class AdapterServletPayment extends GenericServlet {

    private final transient PaymentServerHandlerMdcLog handler;
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
