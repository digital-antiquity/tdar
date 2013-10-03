package org.tdar.core.service.workflow;

import java.util.List;

public interface ActionMessageErrorSupport {

    public void addActionError(String message);

    public void addActionMessage(String message);

    public List<String> getStackTraces();

}
