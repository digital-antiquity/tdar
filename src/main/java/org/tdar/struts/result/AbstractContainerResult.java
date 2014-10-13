package org.tdar.struts.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tdar.struts.action.TdarActionSupport;
import org.tdar.utils.jaxb.JaxbMapResultContainer;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;

public abstract class AbstractContainerResult  implements Result {

    private static final long serialVersionUID = -684100853873962052L;

    public static final String FIELD_ERRORS = "fieldErrors";
    public static final String ACTION_ERRORS = "actionErrors";
    public static final String ERRORS_KEY = "errors";

    @SuppressWarnings("unchecked")
    protected void processErrors(ActionInvocation invocation, Object resultObject_, boolean wrap) {
        if (resultObject_ instanceof Map && invocation.getAction() instanceof TdarActionSupport) {
            TdarActionSupport tas = (TdarActionSupport) invocation.getAction();
            Map<String, Object> result = (Map<String, Object>) resultObject_;
            Map<String, Object> errors = (Map<String, Object>) result.get(ERRORS_KEY);
            if (errors == null) {
                errors = new HashMap<>();
                if (wrap) {
                result.put(ERRORS_KEY, new JaxbMapResultContainer(errors));
                } else {
                    result.put(ERRORS_KEY, errors);
                }
            }

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
        }
    }

    
}
