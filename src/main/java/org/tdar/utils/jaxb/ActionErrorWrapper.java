package org.tdar.utils.jaxb;

import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.TextProvider;

public interface ActionErrorWrapper extends TextProvider {

    boolean hasActionErrors();
    
    List<String> getActionErrors();
    
    boolean hasFieldErrors();
    
    Map<String, List<String>> getFieldErrors();
}
