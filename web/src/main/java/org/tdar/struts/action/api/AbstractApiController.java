package org.tdar.struts.action.api;

import java.io.InputStream;
import java.util.List;

import org.tdar.core.exception.StatusCode;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.utils.jaxb.JaxbResultContainer;

public abstract class AbstractApiController extends AbstractAuthenticatableAction {

    private static final long serialVersionUID = -4598504825709978763L;
    private String record;
    private String msg;
    private StatusCode status;
    private String errorMessage;
    public final static String msg_ = "%s is %s %s (%s): %s";
    private Long id;
    private InputStream inputStream;
    private JaxbResultContainer xmlResultObject = new JaxbResultContainer();

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public StatusCode getStatus() {
        return status;
    }

    public void setStatus(StatusCode status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public JaxbResultContainer getXmlResultObject() {
        return xmlResultObject;
    }

    public void setXmlResultObject(JaxbResultContainer xmlResultObject) {
        this.xmlResultObject = xmlResultObject;
    }

    protected String errorResponse(StatusCode statusCode, List<String> errors, String message2, List<String> stackTraces) {
        status = statusCode;
        xmlResultObject.setStatus(statusCode.toString());
        xmlResultObject.setStatusCode(statusCode.getHttpStatusCode());
        xmlResultObject.setMessage(errorMessage);
        xmlResultObject.setStackTraces(stackTraces);
        xmlResultObject.setErrors(errors);
        return ERROR;
    }

    protected void logMessage(String action_, Class<?> cls, Long id_, String name_) {
        getLogger().info(String.format(msg_, getAuthenticatedUser().getEmail(), action_, cls.getSimpleName().toUpperCase(), id_, name_));
    }

    protected void setStatusMessage(StatusCode updated, String string) {
        this.status = updated;
        this.errorMessage = string;
    }
}
