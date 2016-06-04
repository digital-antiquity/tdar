package org.tdar.struts.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.interceptor.annotation.PostOnly;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/mock-nelnet")
public class MockNelnetController extends AbstractAuthenticatableAction implements ParameterAware, Serializable {

    private static final long serialVersionUID = -973297044126882831L;

    private Map<String, String[]> params;
    private Map<String, String[]> responseParams = new HashMap<String, String[]>();

    @Autowired
    private transient NelNetPaymentDao nelnet;

    @Override
    public void setParameters(Map<String, String[]> arg0) {
        this.params = arg0;
        getLogger().info("{}", arg0);
    }

    private String ccnum = "";

    @Action(value = "setup-payment", results = {
            @Result(name = SUCCESS, location = "setup-payment.ftl")
    })
    public String setupPayment() {

        return SUCCESS;
    }

    private String getParamValue(NelnetTransactionItem item) {
        if (params.containsKey(item.getKey())) {
            return params.get(item.getKey())[0];
        }
        return null;
    }

    @Action("process-payment")
    @Override
    @PostOnly
    public String execute() throws ClientProtocolException, IOException, TdarActionException {

        processFakeResponse(getCcType(getCcnum()));
        sendResponse();
        return SUCCESS;
    }

    private void sendResponse() throws TdarActionException {
        String url = String.format("https://%s:%s%s/cart/process-external-payment-response", getHostName(), getHttpsPort(),getContextPath());

        HttpPost postReq = new HttpPost(url);
        getLogger().info(url);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (NelnetTransactionItemResponse item : NelnetTransactionItemResponse.values()) {
            if (!getResponseParams().containsKey(item.getKey()) || (item == NelnetTransactionItemResponse.KEY)) {
                continue;
            }
            pairs.add(new BasicNameValuePair(item.getKey(), responseParams.get(item.getKey())[0]));
        }
        postReq.setEntity(new UrlEncodedFormEntity(pairs, Consts.UTF_8));
        try {
            SSLContextBuilder sslBuilder = new SSLContextBuilder();
            sslBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslBuilder.build());
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

            CloseableHttpResponse response = httpclient.execute(postReq);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            boolean seen = false;
            for (String line : IOUtils.readLines(rd)) {
                if (line.contains(SUCCESS)) {
                    seen = true;
                }
            }
            if (seen == false) {
                getLogger().warn("WE SHOULD SEE 'SUCCESS' IN THE RESPONSE");
                throw new TdarRecoverableRuntimeException("mockNelnetController.did_not_see_success");
            }
            getLogger().info("response: {} ", response);
        } catch (Exception e) {
            getLogger().error("exception: {}", e);
            throw new TdarActionException(StatusCode.BAD_REQUEST, "cannot make http connection");
        }

    }

    private void processFakeResponse(String cctype) {
        for (NelnetTransactionItemResponse item : NelnetTransactionItemResponse.values()) {
            String key = item.getKey();
            if (params.containsKey(key)) {
                responseParams.put(key, params.get(key));
            }
        }
        String total = getParamValue(NelnetTransactionItem.AMOUNT);
        responseParams.put(NelnetTransactionItemResponse.TIMESTAMP.getKey(), new String[] { Long.toString(System.currentTimeMillis()) });
        responseParams.put(NelnetTransactionItemResponse.TRANSACTION_ACCOUNT_TYPE.getKey(), new String[] { cctype });
        responseParams.put(NelnetTransactionItemResponse.EVENING_PHONE.getKey(), new String[] { "4809651369" });
        responseParams.put(NelnetTransactionItemResponse.STREET_ONE.getKey(), new String[] { "PO Box 872402" });
        responseParams.put(NelnetTransactionItemResponse.STREET_TWO.getKey(), new String[] { "Arizona State University" });
        responseParams.put(NelnetTransactionItemResponse.CITY.getKey(), new String[] { "Tempe" });
        responseParams.put(NelnetTransactionItemResponse.STATE.getKey(), new String[] { "AZ" });
        responseParams.put(NelnetTransactionItemResponse.ZIP.getKey(), new String[] { "85287" });
        responseParams.put(NelnetTransactionItemResponse.COUNTRY.getKey(), new String[] { "USA" });
        responseParams.put(NelnetTransactionItemResponse.TRANSACTION_TOTAL.getKey(), new String[] { total });
        responseParams.put(NelnetTransactionItemResponse.TRANSACTION_ID.getKey(), new String[] { String.valueOf(System.currentTimeMillis()) });
        responseParams.put(NelnetTransactionItemResponse.TRANSACTION_TYPE.getKey(), new String[] { "1" });
        String responseCode = "1";
        if (total.endsWith("11")) {
            responseCode = "2";
        }
        if (total.endsWith("21")) {
            responseCode = "3";
        }
        if (total.endsWith("31")) {
            responseCode = "4";
        }
        responseParams.put(NelnetTransactionItemResponse.TRANSACTION_STATUS.getKey(), new String[] { responseCode });
        NelNetTransactionResponseTemplate resp = new NelNetTransactionResponseTemplate(nelnet.getSecretResponseWord(), responseParams);
        getLogger().info(resp.generateHashKey());
        responseParams.put(NelnetTransactionItemResponse.HASH.getKey(), new String[] { resp.generateHashKey() });

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
        getLogger().info("cctype: {}", cctype);
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

    public Map<String, String[]> getResponseParams() {
        return responseParams;
    }

    public void setResponseParams(Map<String, String[]> responseParams) {
        this.responseParams = responseParams;
    }

}
