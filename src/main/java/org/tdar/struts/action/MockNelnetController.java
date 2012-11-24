package org.tdar.struts.action;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ParameterAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.dao.external.payment.nelnet.NelNetPaymentDao;
import org.tdar.core.dao.external.payment.nelnet.NelNetTransactionRequestTemplate.NelnetTransactionItem;
import org.tdar.core.dao.external.payment.nelnet.NelNetTransactionResponseTemplate;
import org.tdar.core.dao.external.payment.nelnet.NelNetTransactionResponseTemplate.NelnetTransactionItemResponse;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/mock-nelnet")
public class MockNelnetController extends AuthenticationAware.Base implements ParameterAware, Serializable {

    private static final long serialVersionUID = -973297044126882831L;

    private Map<String, String[]> params;

    @Autowired
    private NelNetPaymentDao nelnet;

    @Override
    public void setParameters(Map<String, String[]> arg0) {
        this.params = arg0;
        logger.info("{}", arg0);
    }

    private String ccnum = "";

    @Action(value = "setup-payment", results = {
            @Result(name = "success", location = "setup-payment.ftl")
    })
    public String setupPayment() {

        return "success";
    }

    private String getParamValue(NelnetTransactionItem item) {
        if (params.containsKey(item.getKey())) {
            return params.get(item.getKey())[0];
        }
        return null;
    }

    @Action("process-payment")
    public String execute() throws ClientProtocolException, IOException {

        Map<String, String[]> toreturn = new HashMap<String, String[]>();
        processFakeResponse(getCcType(getCcnum()), toreturn);
        sendResponse(toreturn);
        return "success";
    }

    private void sendResponse(Map<String, String[]> toreturn) throws IOException, ClientProtocolException {
        String url = String.format("http://%s:%s/cart/process-external-payment-response", getHostName(), getHostPort());
        HttpPost postReq = new HttpPost(url);
        logger.info(url);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (NelnetTransactionItemResponse item : NelnetTransactionItemResponse.values()) {
            if (!toreturn.containsKey(item.getKey()) || item == NelnetTransactionItemResponse.KEY)
                continue;
            pairs.add(new BasicNameValuePair(item.getKey(), toreturn.get(item.getKey())[0]));
        }
        postReq.setEntity(new UrlEncodedFormEntity(pairs, Consts.UTF_8));

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpResponse httpresponse = httpclient.execute(postReq);
        logger.info("response: {} ", httpresponse);

    }

    private void processFakeResponse(String cctype, Map<String, String[]> toreturn) {
        for (NelnetTransactionItemResponse item : NelnetTransactionItemResponse.values()) {
            String key = item.getKey();
            if (params.containsKey(key)) {
                toreturn.put(key, params.get(key));
            }
        }
        String total = getParamValue(NelnetTransactionItem.AMOUNT);
        toreturn.put(NelnetTransactionItemResponse.TIMESTAMP.getKey(), new String[] { Long.toString(System.currentTimeMillis()) });
        toreturn.put(NelnetTransactionItemResponse.TRANSACTION_ACCOUNT_TYPE.getKey(), new String[] { cctype });
        toreturn.put(NelnetTransactionItemResponse.TRANSACTION_TOTAL.getKey(), new String[] { total });
        toreturn.put(NelnetTransactionItemResponse.TRANSACTION_TYPE.getKey(), new String[] { "1" });
        String responseCode = "1";
        if (total.endsWith(".11")) {
            responseCode = "2";
        }
        if (total.endsWith(".21")) {
            responseCode = "3";
        }
        if (total.endsWith(".31")) {
            responseCode = "4";
        }
        toreturn.put(NelnetTransactionItemResponse.TRANSACTION_STATUS.getKey(), new String[] { responseCode });
        NelNetTransactionResponseTemplate resp = new NelNetTransactionResponseTemplate(nelnet.getSecretWord());
        resp.setValues(toreturn);
        logger.info(resp.generateHashKey());
        toreturn.put(NelnetTransactionItemResponse.HASH.getKey(), new String[] { resp.generateHashKey() });

    }

    private String getCcType(String cc) {
        String cctype = "";
        if (cc.startsWith("4111")) {
            cctype = "VISA";
        } else if (cc.startsWith("5454")) {
            cctype = "MasterCard";
        } else if (cc.startsWith("3782")) {
            cctype = "American Express";
        } else if (cc.startsWith("6011")) {
            cctype = "DISCOVER";
        }
        logger.info("cctype: {}", cctype);
        return cctype;
    }

    public Map<String, String[]> getParams() {
        return params;
    }

    public String getCcnum() {
        return ccnum;
    }

    public void setCcnum(String ccnum) {
        this.ccnum = ccnum;
    }

}
