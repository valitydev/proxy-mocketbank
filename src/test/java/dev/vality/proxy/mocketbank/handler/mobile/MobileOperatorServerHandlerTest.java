package dev.vality.proxy.mocketbank.handler.mobile;

import dev.vality.mnp.Operator;
import dev.vality.mnp.PhoneNumber;
import dev.vality.mnp.RequestParams;
import dev.vality.mnp.ResponseData;
import dev.vality.proxy.mocketbank.exception.MobileOperatorException;
import dev.vality.proxy.mocketbank.handler.mobile.operator.MobileOperatorServerHandler;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MobileOperatorServerHandlerTest {

    @Autowired
    private MobileOperatorServerHandler handler;

    @Test
    void lookupTest() throws TException {
        // Get operator by phone number
        RequestParams requestParams = createRequestParams("9151111111");
        ResponseData responseData = handler.lookup(requestParams);
        assertEquals(Operator.mts, responseData.getOperator());

        requestParams = createRequestParams("9001111111");
        responseData = handler.lookup(requestParams);
        assertEquals(Operator.beeline, responseData.getOperator());
    }

    private RequestParams createRequestParams(String phoneNumber) {
        RequestParams params = new RequestParams();
        params.setPhone(new PhoneNumber("7", phoneNumber));
        params.setOptions(Collections.emptyMap());
        return params;
    }

    @Test
    void lookupExceptionTest() throws TException {
        // Get exception by phone number if phone not found in lists
        RequestParams requestParams = createRequestParams("9999999999");
        assertThrows(
                MobileOperatorException.class,
                () -> handler.lookup(requestParams),
                "Expected lookup() to throw, but it didn't"
        );
    }

}
