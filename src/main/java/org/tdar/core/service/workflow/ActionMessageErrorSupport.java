package org.tdar.core.service.workflow;

import java.util.List;

public interface ActionMessageErrorSupport {

    void addActionError(String message);

    void addActionMessage(String message);

    List<String> getStackTraces();

    void setMoreInfoUrlKey(String url);

    boolean isShouldDetach();

    void setShouldDetach(boolean shouldDetach);

}
