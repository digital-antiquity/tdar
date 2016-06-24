package org.tdar.struts.result;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.views.freemarker.FreemarkerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.TextParseUtil;
import com.opensymphony.xwork2.util.ValueStack;

import freemarker.template.Template;
import freemarker.template.TemplateModel;

/**
 * This class extends the behavior of FreemarkerResult by adding the ability to set http headers
 * as well as the http status code, similar to HttpHeaderResult
 * 
 * This class is derived from GBIF Data Portal's FreemarkerHttpHeaderResult class (Apache License v2)
 * https://code.google.com/p/gbif-dataportal/
 * 
 * @author Jim deVos
 */
public class FreemarkerHttpHeaderResult extends FreemarkerResult {
    private static final long serialVersionUID = 195648957144219214L;
    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(FreemarkerHttpHeaderResult.class);
    private Map<String, String> headers;
    private int status;

    public FreemarkerHttpHeaderResult() {
        headers = new LinkedHashMap<>();
        status = HttpStatus.SC_OK;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    protected void postTemplateProcess(Template template, TemplateModel data) throws IOException {
        super.postTemplateProcess(template, data);
        HttpServletResponse response = ServletActionContext.getResponse();
        response.setStatus(status);
        if (!headers.isEmpty()) {
            // allow for
            ValueStack stack = ActionContext.getContext().getValueStack();
            for (Map.Entry<String, String> header : headers.entrySet()) {
                @SuppressWarnings("unused")
                String value = TextParseUtil.translateVariables(header.getValue(), stack);
                response.setHeader(header.getKey(), header.getValue());
            }
        }
    }
}
