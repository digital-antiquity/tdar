package org.tdar.struts.result;

import java.io.InputStream;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.tdar.core.exception.StatusCode;
import org.tdar.utils.MessageHelper;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;

public class JsonDocumentResult implements Result {

    private static final long serialVersionUID = -869094415533475014L;
    public static final String UTF_8 = "UTF-8";
    public static final String CONTENT_TYPE = "application/json";
    public static final String DEFAULT_PARAM = "jsonInputStream";

    private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
    private String stream = DEFAULT_PARAM;
    private int statusCode = StatusCode.OK.getHttpStatusCode();

    public JsonDocumentResult() {
        super();
    }

    public JsonDocumentResult(String stream) {
        this();
        this.stream = stream;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    @Override
    public void execute(ActionInvocation invocation) throws Exception {
        InputStream inputStream = (InputStream) invocation.getStack().findValue(stream);
        if (inputStream == null) {
            String msg = MessageHelper.getMessage("jsonDocumentResult.document_not_found", invocation.getInvocationContext().getLocale(),
                    Arrays.asList(stream).toArray());
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        HttpServletResponse resp = ServletActionContext.getResponse();
        resp.setCharacterEncoding(UTF_8);
        resp.setStatus(getStatusCode() );
        resp.setContentType(CONTENT_TYPE);
        IOUtils.copy(inputStream, resp.getOutputStream());
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
