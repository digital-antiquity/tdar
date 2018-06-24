package org.tdar.fileprocessing.workflows;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class RequiredOptionalPairs implements Serializable {

    private static final long serialVersionUID = -7764737864438622589L;

    List<String> required = new ArrayList<>();
    List<String> optional = new ArrayList<>();

    private Class<? extends Workflow> workflowClass;

    public RequiredOptionalPairs() {
    }

    public RequiredOptionalPairs(Class<? extends Workflow> cls) {
        this.setWorkflowClass(cls);
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }

    public List<String> getOptional() {
        return optional;
    }

    public void setOptional(List<String> optional) {
        this.optional = optional;
    }

    public Class<? extends Workflow> getWorkflowClass() {
        return workflowClass;
    }

    public void setWorkflowClass(Class<? extends Workflow> workflowClass) {
        this.workflowClass = workflowClass;
    }

}
