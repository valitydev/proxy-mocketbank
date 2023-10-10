package dev.vality.proxy.mocketbank.servlet;

import dev.vality.mnp.MnpSrv;
import dev.vality.proxy.mocketbank.decorator.MobileOperatorServerHandlerLog;
import dev.vality.woody.thrift.impl.http.THServiceBuilder;
import lombok.RequiredArgsConstructor;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@RequiredArgsConstructor
@WebServlet("/proxy/mocketbank/mobile/operator")
public class AdapterServletMobileOperator extends GenericServlet {

    private final transient MobileOperatorServerHandlerLog handler;
    private transient Servlet servlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servlet = new THServiceBuilder().build(MnpSrv.Iface.class, handler);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        servlet.service(request, response);
    }

}
