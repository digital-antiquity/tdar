package org.tdar.fileprocessing.workflows;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.tdar.db.conversion.converters.DatasetConverter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class RequiredOptionalPairs implements Serializable {

    private static final long serialVersionUID = -7764737864438622589L;

    List<String> required = new ArrayList<>();
    List<String> optional = new ArrayList<>();

    private Class<? extends Workflow> workflowClass;

    private boolean hasDimensions;
    private Class<? extends DatasetConverter> datasetConverter;

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

    public void setHasDimensions(boolean hasDimensions) {
        this.hasDimensions = hasDimensions;
    }

    public boolean isHasDimensions() {
        return hasDimensions;
    }
    
    public Class<? extends DatasetConverter> getDatasetConverter() {
        return datasetConverter;
    }

    public void setDatasetConverter(Class<? extends DatasetConverter> datasetConverter) {
        this.datasetConverter = datasetConverter;
    }

}
