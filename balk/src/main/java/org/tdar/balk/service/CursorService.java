package org.tdar.balk.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.dao.GenericDao;

import com.dropbox.core.DbxException;

import org.tdar.balk.bean.DropboxState;
import org.tdar.balk.bean.PollType;
import org.tdar.balk.dao.CursorDao;
import org.tdar.utils.dropbox.DropboxClient;
import org.tdar.utils.dropbox.MetadataListener;

@Component
public class CursorService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private CursorDao cursorDao;
    
    @Autowired 
    private GenericDao genericDao;
    
    @Transactional(readOnly=false)
    public void pollAndProcess(PollType type, String path, MetadataListener listener) throws FileNotFoundException, URISyntaxException, IOException, DbxException {
        String latestCursorFor = cursorDao.getLatestCursorFor(type);
        logger.debug("running: {}", listener.getClass());
        DropboxClient client = new DropboxClient();
        listener.setDebug(client.getDebug());
        switch (type) {
            case STATUS:
                client.list(path, latestCursorFor, listener);
                break;
            case UPLOAD:
                client.list(path, latestCursorFor, listener);
                break;
            default:
                break;
            
        }
        
        DropboxState newCursor = new DropboxState(new Date(), client.getCurrentCursor(), type);
        genericDao.saveOrUpdate(newCursor);

    }
}
