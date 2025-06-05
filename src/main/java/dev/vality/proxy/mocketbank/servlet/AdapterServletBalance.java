package dev.vality.proxy.mocketbank.servlet;

import dev.vality.proxy.mocketbank.handler.balance.AccountBalanceHandler;
import dev.vality.scrooge.AccountServiceSrv;
import dev.vality.woody.thrift.impl.http.THServiceBuilder;
import lombok.RequiredArgsConstructor;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@RequiredArgsConstructor
@WebServlet("/proxy/mocketbank/p2p-credit/balance")
public class AdapterServletBalance extends GenericServlet {

    private final AccountBalanceHandler handler;
    private Servlet servlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servlet = new THServiceBuilder().build(AccountServiceSrv.Iface.class, handler);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        servlet.service(request, response);
    }

}
