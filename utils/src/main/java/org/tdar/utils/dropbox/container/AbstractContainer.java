package org.tdar.utils.dropbox.container;

import org.tdar.utils.dropbox.DropboxItemWrapper;

public abstract class AbstractContainer {

    private DropboxItemWrapper wrapper;

    public DropboxItemWrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(DropboxItemWrapper wrapper) {
        this.wrapper = wrapper;
    }
}
