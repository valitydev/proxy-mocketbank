package com.rbkmoney.proxy.mocketbank.servlet;

import com.rbkmoney.proxy.mocketbank.handler.balance.AccountBalanceHandler;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import dev.vality.account_balance.AccountServiceSrv;
import lombok.RequiredArgsConstructor;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@RequiredArgsConstructor
@WebServlet("/proxy/mocketbank/balance")
public class AdapterServletBalance extends GenericServlet {

    private final transient AccountBalanceHandler handler;
    private transient Servlet servlet;

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
