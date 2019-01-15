package com.rbkmoney.wb.list.manager.resource;

import com.rbkmoney.damsel.wb_list.WbListServiceSrv;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import lombok.RequiredArgsConstructor;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/v1/wb_list")
@RequiredArgsConstructor
public class FraudInspectorServlet extends GenericServlet {

    private Servlet thriftServlet;

    private final WbListServiceSrv.Iface fraudInspectorHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        thriftServlet = new THServiceBuilder()
                .build(WbListServiceSrv.Iface.class, fraudInspectorHandler);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        thriftServlet.service(req, res);
    }
}
