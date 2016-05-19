package org.tdar.utils.jaxb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.exception.StatusCode;

import com.opensymphony.xwork2.ActionInvocation;

@XmlRootElement(name = "resultContainer")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resultContainer")
public class JaxbResultContainer implements Serializable, APIParameters {

    private static final long serialVersionUID = -8255546475402578411L;
    private List<String> errors = new ArrayList<>();
    @XmlAttribute
    private String username;
    @XmlAttribute
    private String sessionKeyName;
    @XmlAttribute
    private String apiToken;
    private Resource result;
    private List<String> stackTraces;
    private String message;
    private Long id;
    private int statusCode = StatusCode.OK.getHttpStatusCode();
    private String status;
    private Long recordId;

    public void convert(Map<String, Object> object_, ActionInvocation invocation) {

        String username_ = (String) object_.get(USERNAME);
        if (StringUtils.isNotBlank(username_)) {
            username = username_;
        }

        String message_ = (String) object_.get(MESSAGE);
        if (StringUtils.isNotBlank(message_)) {
            setMessage(message_);
        }
        
        String id_ = (String)object_.get(ID);
        if (id_ != null && NumberUtils.isNumber(id_)) {
            id = Long.parseLong(id_);
        }
        
        String apiKey_ = (String) object_.get(API_TOKEN);
        if (StringUtils.isNotBlank(apiKey_)) {
            apiToken = apiKey_;
        }

        String sessionKeyName_ = (String) object_.get(API_TOKEN_KEY_NAME);
        if (StringUtils.isNotBlank(sessionKeyName_)) {
            sessionKeyName = sessionKeyName_;
        }

            ActionErrorWrapper tas = (ActionErrorWrapper) invocation.getAction();
            if (tas.hasActionErrors()) {
                for (String actionError : tas.getErrorMessages()) {
                    errors.add(tas.getText(actionError));
                }
            }

            if (tas.hasFieldErrors()) {
                for (Entry<String, List<String>> entry : tas.getFieldErrors().entrySet()) {
                    errors.add(String.format("%s %s: %s", tas.getText("JaxbMapResultContainer.fieldError"),
                            entry.getKey(), StringUtils.join(entry.getValue(), ";")));
                }
            }
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionKeyName() {
        return sessionKeyName;
    }

    public void setSessionKeyName(String sessionKeyName) {
        this.sessionKeyName = sessionKeyName;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String token) {
        this.apiToken = token;
    }

    public Resource getResult() {
        return result;
    }

    public void setResult(Resource result) {
        this.result = result;
    }

    public List<String> getStackTraces() {
        return stackTraces;
    }

    public void setStackTraces(List<String> stackTraces) {
        this.stackTraces = stackTraces;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
