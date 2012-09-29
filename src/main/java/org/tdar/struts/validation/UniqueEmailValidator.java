package org.tdar.struts.validation;

import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.EntityService;

import com.opensymphony.xwork2.validator.ShortCircuitableValidator;
import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class UniqueEmailValidator extends FieldValidatorSupport implements ShortCircuitableValidator {
    
    private EntityService entityService;
    
    @Override
    public void validate(Object actionBean) throws ValidationException {
        final String fieldName = getFieldName();
        Object value = getFieldValue(fieldName, actionBean);
        if (value == null) {
            return;
        }
        String email = value.toString();
        Person person = entityService.findByEmail(email);
        if (person != null && person.isRegistered()) {
            addFieldError(fieldName, actionBean);
        }
    }

    public void setEntityService(EntityService entityService) {
        this.entityService = entityService;
    }

}
