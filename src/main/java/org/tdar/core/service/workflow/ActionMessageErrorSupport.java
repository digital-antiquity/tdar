package org.tdar.core.service.workflow;

import java.util.List;

public interface ActionMessageErrorSupport {

    void addActionError(String message);

    void addActionMessage(String message);

    public List<String> getStackTraces();

}
