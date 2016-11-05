package org.tdar.core.service.processes;

import org.tdar.core.configuration.TdarConfiguration;

public interface BatchProcess {

    public static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();
    int DEFAULT_PROCESS_END_ID = 1_000_000;

    int getProcessStartId();

    void setProcessStartId(int processStartId);

    int getProcessEndId();

    void setProcessEndId(int processEndId);


    default boolean shouldRunAll() {
            if (CONFIG.getScheduledProcessStartId() != -1) {
                setProcessStartId(CONFIG.getScheduledProcessStartId());
            }
            if (CONFIG.getScheduledProcessEndId() != -1) {
                setProcessEndId(CONFIG.getScheduledProcessEndId());
            }

            if (getProcessStartId() == -1 && getProcessEndId() == -1) {
                return true;
            } else {
                if (getProcessEndId() == -1) {
                    setProcessEndId(DEFAULT_PROCESS_END_ID);
                }
                return false;
            }
    }
    
}
