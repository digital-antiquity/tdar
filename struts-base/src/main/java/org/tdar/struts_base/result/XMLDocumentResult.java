package org.tdar.struts_base.result;

import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.SerializationService;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.jaxb.JaxbResultContainer;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;

@Component
public class XMLDocumentResult implements Result {

    private static final long serialVersionUID = 7102433466724795537L;
    public static final String UTF_8 = "UTF-8";
    public static final String CONTENT_TYPE = "application/xml";
    public static final String DEFAULT_PARAM = "resultObject";

    private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
    private String object = DEFAULT_PARAM;
    private int statusCode = StatusCode.OK.getHttpStatusCode();

    @Autowired
    SerializationService serializationService;

    public XMLDocumentResult() {
        super();
    }

    public XMLDocumentResult(String object) {
        this();
        this.object = object;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String stream) {
        this.object = stream;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ActionInvocation invocation) throws Exception {
        Object object_ = invocation.getStack().findValue(object);
        if (object_ == null) {
            String msg = MessageHelper.getMessage("xmlDocumentResult.object_not_found", invocation.getInvocationContext().getLocale(),
                    Arrays.asList(object).toArray());
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        HttpServletResponse resp = ServletActionContext.getResponse();
        resp.setCharacterEncoding(UTF_8);
        resp.setContentType(CONTENT_TYPE);
        JaxbResultContainer container = new JaxbResultContainer();
        if (object_ instanceof JaxbResultContainer) {
            container = (JaxbResultContainer) object_;
            if (container.getStatusCode() != StatusCode.OK.getHttpStatusCode()) {
                setStatusCode(container.getStatusCode());
            }
        }

        if (object_ instanceof Map) {
            container.convert((Map<String, Object>) object_, invocation);
            object_ = container;
            if (TdarActionSupport.INPUT.equals(invocation.getResultCode())) {
                setStatusCode(StatusCode.BAD_REQUEST.getHttpStatusCode());
                container.setStatusCode(getStatusCode());
            }
        }

        logger.trace("StatusCode: {}", getStatusCode());
        resp.setStatus(getStatusCode());
        serializationService.convertToXML(container, new OutputStreamWriter(resp.getOutputStream()));
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
