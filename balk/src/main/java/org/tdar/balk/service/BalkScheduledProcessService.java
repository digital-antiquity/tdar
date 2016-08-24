package org.tdar.balk.service;

import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tdar.balk.bean.PollType;
import org.tdar.utils.dropbox.DropboxConstants;
import org.tdar.utils.dropbox.DropboxItemWrapper;
import org.tdar.utils.dropbox.StatReporter;
import org.tdar.utils.dropbox.TdarUploadListener;
import org.tdar.utils.dropbox.ToPersistListener;

import com.dropbox.core.DbxException;

@Service
public class BalkScheduledProcessService {

    private static final long ONE_MIN_MS = 60000;
    private static final long FIVE_MIN_MS = ONE_MIN_MS * 5;
    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CursorService cursorService;
    
    @Autowired
    private ItemService itemService;
    
    @Scheduled(fixedDelay = FIVE_MIN_MS)
    public void cronPollingQueue() {
        try {
            cursorService.pollAndProcess(PollType.UPLOAD, DropboxConstants.UPLOAD_PATH, new TdarUploadListener());
        } catch (URISyntaxException | IOException | DbxException e) {
            logger.error("polling error {}", e,e);
        }
        
    }

    @Scheduled(fixedDelay = FIVE_MIN_MS)
    public void cronPollingStatsQueue() {
        try {
            ToPersistListener listener = new ToPersistListener();
            cursorService.pollAndProcess(PollType.STATUS, DropboxConstants.CLIENT_DATA, listener);
            itemService.store(listener);
        } catch (URISyntaxException | IOException | DbxException e) {
            logger.error("polling error {}", e,e);
        }
        
    }

}
