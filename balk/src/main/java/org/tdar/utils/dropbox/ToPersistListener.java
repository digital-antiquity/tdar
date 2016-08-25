package org.tdar.utils.dropbox;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToPersistListener implements MetadataListener {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private List<DropboxItemWrapper> wrappers = new ArrayList<>();
    private Boolean debug;

    @Override
    public void consume(DropboxItemWrapper fileWrapper) throws Exception {
        logger.debug(fileWrapper.getFullPath());
        getWrappers().add(fileWrapper);
    }

    @Override
    public void setDebug(Boolean debug) {
            this.debug = debug;        
    }

    public List<DropboxItemWrapper> getWrappers() {
        return wrappers;
    }

    public void setWrappers(List<DropboxItemWrapper> wrappers) {
        this.wrappers = wrappers;
    }
}
