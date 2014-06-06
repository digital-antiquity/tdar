package org.tdar.core.service.workflow;

import java.util.List;

import org.tdar.struts.ErrorListener;

public interface ActionMessageErrorSupport {

    void addActionError(String message);

    void addActionMessage(String message);

    List<String> getStackTraces();

    void setMoreInfoUrlKey(String url);
    
    void registerErrorListener(ErrorListener e);

}
