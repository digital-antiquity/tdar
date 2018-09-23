package org.tdar.fileprocessing.workflows;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private String primaryExtension = "";

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

    public boolean requiresSidecar() {
        int total = required.size() + optional.size();
        if (total > 1) {
            return true;
        }
        return false;
    }

    public Set<String> getAllExtensions() {
        HashSet<String> toReturn = new HashSet<>();
        toReturn.addAll(getRequired());
        toReturn.addAll(getOptional());
        return toReturn;
    }
    
    public String getPrimaryExtension() {
        if (this.getRequired().size() == 1) {
            return this.getRequired().get(0);
        }
        return this.primaryExtension;
    }

    public void setPrimaryExtension(String primaryExtension) {
        this.primaryExtension = primaryExtension;
    }
}
