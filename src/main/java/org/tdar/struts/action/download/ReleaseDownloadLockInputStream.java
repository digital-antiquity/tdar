package org.tdar.struts.action.download;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.service.download.DownloadTransferObject;

/*
 * Closes a DownloadLock when stream is closed
 */
public class ReleaseDownloadLockInputStream extends BufferedInputStream implements Serializable {
    
    private static final long serialVersionUID = 7091418668985623975L;
    private DownloadTransferObject dto;
    public ReleaseDownloadLockInputStream(InputStream in, DownloadTransferObject dto) {
        super(in);
        this.dto = dto;
    }

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void close() throws IOException {
        super.close();
        logger.debug("releasing download lock");
        dto.releaseLock();
    }
    
    


}