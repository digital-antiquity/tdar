package org.tdar.core.service.integration.dto;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

public class IntegrationDeserializationException extends Exception {

    private static final long serialVersionUID = -388002332387954296L;
    private List<String> errors;
    private Map<String, List<String>> fieldErrors;

    public IntegrationDeserializationException(List<String> errors, Map<String, List<String>> fieldErrors) {
        this.fieldErrors = fieldErrors;
        this.errors = errors;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, List<String>> fieldError) {
        this.fieldErrors = fieldError;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (CollectionUtils.isNotEmpty(errors)) {
            sb.append("errors: ").append(errors).append("\n");
        }
        // fixme -- cleanup
        if (MapUtils.isNotEmpty(getFieldErrors())) {
            sb.append("Field Errors: ").append(fieldErrors).append("\n");
        }
        return sb.toString();
    }

}
