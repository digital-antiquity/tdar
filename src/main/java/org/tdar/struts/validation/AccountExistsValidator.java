package org.tdar.struts.validation;

import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.EntityService;

import com.opensymphony.xwork2.validator.ShortCircuitableValidator;
import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * $Id$
 * 
 * Validates successfully if the given account email exists and is registered.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class AccountExistsValidator extends FieldValidatorSupport implements ShortCircuitableValidator {

    private EntityService entityService;

    @Override
    public void validate(Object actionBean) throws ValidationException {
        String fieldName = getFieldName();
        Object value = getFieldValue(fieldName, actionBean);
        if (value == null) {
            return;
        }
        String email = value.toString();
        Person person = entityService.findByEmail(email);
        if ((person == null) || !person.isRegistered()) {
            addFieldError(fieldName, actionBean);
        }
    }

    public void setEntityService(EntityService entityService) {
        this.entityService = entityService;
    }

}
