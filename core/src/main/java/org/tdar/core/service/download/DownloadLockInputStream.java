package org.tdar.core.service.download;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.configuration.TdarConfiguration;

/*
 * Closes a DownloadLock when stream is closed
 */
public class DownloadLockInputStream extends BufferedInputStream implements Serializable {

    private static final long serialVersionUID = 7091418668985623975L;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private DownloadTransferObject dto;

    public DownloadLockInputStream(InputStream in, DownloadTransferObject dto) {
        super(in, TdarConfiguration.getInstance().getDownloadBufferSize());
        this.dto = dto;
        dto.registerDownloadLock();
    }

    @Override
    public void close() throws IOException {
        super.close();
        logger.trace("releasing download lock");
        dto.releaseLock();
    }

}