package org.tdar.utils.jaxb;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.TextProvider;

public interface ActionErrorWrapper extends TextProvider {

    boolean hasActionErrors();
    
    Collection<String> getErrorMessages();
    
    boolean hasFieldErrors();
    
    Map<String, List<String>> getFieldErrors();
}
