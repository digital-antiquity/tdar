package org.tdar.utils.dropbox.container;

import java.util.ArrayList;
import java.util.List;

import org.tdar.utils.dropbox.DropboxItemWrapper;

public class FolderContainer extends AbstractContainer {

    private List<AbstractContainer> children = new ArrayList<>();
    
    public FolderContainer(DropboxItemWrapper wrapper) {
        this.setWrapper(wrapper);
    }

    public List<AbstractContainer> getChildren() {
        return children;
    }

    public void setChildren(List<AbstractContainer> children) {
        this.children = children;
    }
    
    
}
