package com.rbkmoney.proxy.test.servlet;

import com.rbkmoney.damsel.proxy_provider.ProviderProxySrv;
import com.rbkmoney.proxy.test.handler.TestServerHandler;
import com.rbkmoney.woody.api.event.CompositeServiceEventListener;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import com.rbkmoney.woody.thrift.impl.http.event.HttpServiceEventLogListener;
import com.rbkmoney.woody.thrift.impl.http.event.ServiceEventLogListener;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/proxy/test")
public class ProxyServlet extends GenericServlet {

    @Autowired
    private TestServerHandler handler;

    private Servlet servlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        THServiceBuilder builder = new THServiceBuilder();
        builder.withEventListener(new CompositeServiceEventListener<>(
                new ServiceEventLogListener(),
                new HttpServiceEventLogListener()
        ));
        servlet = builder.build(ProviderProxySrv.Iface.class, handler);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        servlet.service(request, response);
    }

}