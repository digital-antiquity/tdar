package org.tdar.core.service.workflow;

import java.util.List;

import org.tdar.struts.ErrorListener;

//FIXME: these methods overlap with ValidationAware.  Consider adding "extends ValidationAware" in this interface or using more-granular method declarations (e.g.  public <T extends ValidationAware & ActionMessageErrorSupport> void  doSomething(T action) ) when working with objects that implement these interfaces
public interface ActionMessageErrorSupport {

    void addActionError(String message);

    void addActionMessage(String message);

    List<String> getStackTraces();

    void setMoreInfoUrlKey(String url);
    
    void registerErrorListener(ErrorListener e);

}
