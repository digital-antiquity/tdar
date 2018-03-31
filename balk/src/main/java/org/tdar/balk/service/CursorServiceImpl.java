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
import org.tdar.balk.bean.DropboxState;
import org.tdar.balk.bean.PollType;
import org.tdar.balk.dao.CursorDao;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.utils.dropbox.DropboxClient;
import org.tdar.utils.dropbox.MetadataListener;

import com.dropbox.core.DbxException;

@Component
public class CursorServiceImpl implements CursorService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CursorDao cursorDao;

    @Autowired
    private GenericDao genericDao;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.balk.service.CursorService#pollAndProcess(org.tdar.balk.bean.PollType, java.lang.String, org.tdar.utils.dropbox.MetadataListener)
     */
    @Override
    @Transactional(readOnly = false)
    public void pollAndProcess(PollType type, String path, MetadataListener listener)
            throws FileNotFoundException, URISyntaxException, IOException, DbxException {
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
