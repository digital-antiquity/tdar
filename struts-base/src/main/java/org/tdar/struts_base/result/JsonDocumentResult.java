package org.tdar.struts_base.result;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.SerializationService;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.MessageHelper;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;

@Component
public class JsonDocumentResult implements Result, TdarResultHeader {

    private static final long serialVersionUID = -869094415533475014L;
    public static final String UTF_8 = "UTF-8";
    public static final String CONTENT_TYPE = "application/json";
    public static final String DEFAULT_STREAM_PARAM = "jsonInputStream";
    public static final String DEFAULT_OBJECT_PARAM = "resultObject";
    public static final String CALLBACK_PARAM = "callback";
    public static final String JSON_VIEW_PARAM = "jsonView";

    private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
    private String stream = DEFAULT_STREAM_PARAM;
    private String jsonView = JSON_VIEW_PARAM;
    private String callback = CALLBACK_PARAM;
    private String jsonObject = DEFAULT_OBJECT_PARAM;
    private int statusCode = StatusCode.OK.getHttpStatusCode();

    @Autowired
    private transient SerializationService serializationService;

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

    private ActionInvocation invocation;

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ActionInvocation invocation) throws Exception {
        this.invocation = invocation;
        InputStream inputStream = getInputStream();
        Object jsonObject_ = getJsonObjectValue();
        Map<String,Object> errorResult = constructErrorObject(invocation);
        
        if (inputStream == null && jsonObject_ == null && MapUtils.isEmpty(errorResult)) {
            String msg = MessageHelper.getMessage("jsonDocumentResult.document_not_found", invocation.getInvocationContext().getLocale(),
                    Arrays.asList(stream).toArray());
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!MapUtils.isEmpty(errorResult)) {
            if (jsonObject_ == null) {
                jsonObject_ = new HashMap<>();
            }
            ((Map<String,Object>)jsonObject_).put(ERRORS_KEY, errorResult);
            String jsonForStream = serializationService.convertFilteredJsonForStream(jsonObject_, getJsonViewValue(), getCallbackValue());
            inputStream = new ByteArrayInputStream(jsonForStream.getBytes());
            encodeAndRespond(inputStream);
            return;
        }
        
        if (jsonObject_ != null && inputStream == null) {
            String jsonForStream = serializationService.convertFilteredJsonForStream(jsonObject_, getJsonViewValue(), getCallbackValue());
            inputStream = new ByteArrayInputStream(jsonForStream.getBytes());
        }

        encodeAndRespond(inputStream);
    }

    private void encodeAndRespond(InputStream inputStream) throws IOException {
        HttpServletResponse resp = ServletActionContext.getResponse();
        resp.setCharacterEncoding(UTF_8);
        resp.setStatus(getStatusCode());
        resp.setContentType(CONTENT_TYPE);
        IOUtils.copy(inputStream, resp.getOutputStream());
    }

    private HashMap<String, Object> constructErrorObject(ActionInvocation invocation) {
        if (invocation.getAction() instanceof TdarActionSupport) {
            TdarActionSupport tas = (TdarActionSupport) invocation.getAction();
            HashMap<String, Object> errors = new HashMap<>();

            if (tas.hasActionErrors()) {
                List<String> actionErrors = new ArrayList<>();
                for (String actionError : tas.getActionErrors()) {
                    actionErrors.add(tas.getText(actionError));
                }
                errors.put(ACTION_ERRORS, actionErrors);
            }
            if (tas.hasFieldErrors()) {
                errors.put(FIELD_ERRORS, tas.getFieldErrors());
            }
            logger.debug("errors: {}", errors);
            return errors;
        }
        return null;
    }

    private String getCallbackValue() {
        String object_ = (String) invocation.getStack().findValue(callback);
        return object_;
    }

    private Class<?> getJsonViewValue() {
        Class<?> object_ = (Class<?>) invocation.getStack().findValue(jsonView);
        return object_;
    }

    private Object getJsonObjectValue() {
        Object object_ = invocation.getStack().findValue(jsonObject);
        return object_;
    }

    private InputStream getInputStream() {
        Object stream_ = invocation.getStack().findValue(stream);
        InputStream inputStream = null;
        if (stream_ != null && InputStream.class.isAssignableFrom(stream_.getClass())) {
            inputStream = (InputStream) stream_;
        }
        return inputStream;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getJsonView() {
        return jsonView;
    }

    public void setJsonView(String jsonView) {
        this.jsonView = jsonView;
    }

    public String getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(String jsonObject) {
        this.jsonObject = jsonObject;
    }
}
