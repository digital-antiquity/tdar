package org.tdar.balk.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dropbox.core.DbxException;

import main.java.org.tdar.balk.bean.PollType;
import main.java.org.tdar.utils.dropbox.DropboxClient;
import main.java.org.tdar.utils.dropbox.DropboxConstants;
import main.java.org.tdar.utils.dropbox.ToPersistListener;

@Service
public class BalkScheduledProcessService {

    private static final long ONE_MIN_MS = 60000;
    private static final long FIVE_MIN_MS = ONE_MIN_MS * 5;
    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CursorService cursorService;
    
    @Autowired
    private ItemService itemService;
    
    @Scheduled(fixedDelay = ONE_MIN_MS)
    public void cronPollingQueue() {
        DropboxClient client;
        try {
            client = new DropboxClient();
            if (client.getDebug() == Boolean.FALSE) {
                itemService.handleUploads();
            }
        } catch (URISyntaxException | IOException | DbxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = ONE_MIN_MS)
    @Transactional(readOnly=false)
    public void cronPollingStatsQueue() {
        try {
            ToPersistListener listener = new ToPersistListener(itemService);
            cursorService.pollAndProcess(PollType.STATUS, DropboxConstants.CLIENT_DATA, listener);
//            itemService.store(listener);
        } catch (URISyntaxException | IOException | DbxException e) {
            logger.error("polling error {}", e,e);
        }
        logger.debug("completed poling");
    }

}
