package org.tdar.core.bean.resource.file;

public enum FileAction {
    NONE, ADD, REPLACE, DELETE, MODIFY_METADATA, ADD_DERIVATIVE;

    public boolean shouldExpectFileHandle() {
        switch (this) {
            case ADD:
            case ADD_DERIVATIVE:
            case REPLACE:
                // user added a file but changed mind and clicked delete. NONE instructs the system to ignore the pending file
            case NONE:
                return true;
            default:
                return false;
        }
    }

    public boolean requiresWorkflowProcessing() {
        switch (this) {
            case ADD:
            case REPLACE:
                return true;
            default:
                return false;
        }
    }

    public boolean requiresExistingIrFile() {
        switch (this) {
            case ADD:
                return false;
            case NONE:
                return false;
            default:
                return true;
        }
    }

    public boolean updatesMetadata() {
        switch (this) {
            case ADD:
            case REPLACE:
            case MODIFY_METADATA:
                return true;
            default:
                return false;
        }
    }
}
