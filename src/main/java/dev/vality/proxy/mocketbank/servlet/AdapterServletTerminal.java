package dev.vality.proxy.mocketbank.servlet;

import dev.vality.damsel.proxy_provider.ProviderProxySrv;
import dev.vality.proxy.mocketbank.decorator.TerminalServerHandlerLog;
import dev.vality.woody.thrift.impl.http.THServiceBuilder;
import lombok.RequiredArgsConstructor;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@RequiredArgsConstructor
@WebServlet("/proxy/mocketbank/terminal")
public class AdapterServletTerminal extends GenericServlet {

    private final transient TerminalServerHandlerLog handler;
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
