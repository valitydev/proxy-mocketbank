package dev.vality.proxy.mocketbank.servlet;

import dev.vality.damsel.withdrawals.provider_adapter.AdapterSrv;
import dev.vality.proxy.mocketbank.decorator.WithdrawalServerHandlerLog;
import dev.vality.woody.thrift.impl.http.THServiceBuilder;
import lombok.RequiredArgsConstructor;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@RequiredArgsConstructor
@WebServlet("/proxy/mocketbank/p2p-credit")
public class AdapterServletOct extends GenericServlet {

    private final transient WithdrawalServerHandlerLog handler;
    private transient Servlet servlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servlet = new THServiceBuilder().build(AdapterSrv.Iface.class, handler);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        servlet.service(request, response);
    }

}
