package org.tdar.balk.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.balk.bean.PollType;
import org.tdar.utils.dropbox.DropboxClient;
import org.tdar.utils.dropbox.DropboxConfig;
import org.tdar.utils.dropbox.ToPersistListener;

import com.dropbox.core.DbxException;

@Service
public class BalkScheduledProcessServiceImpl implements BalkScheduledProcessService {

    private static final long ONE_MIN_MS = 60000;
    private static final long FIVE_MIN_MS = ONE_MIN_MS * 5;
    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CursorService cursorService;
    
    @Autowired
    private ItemService itemService;
    
    /* (non-Javadoc)
     * @see org.tdar.balk.service.BalkScheduledProcessService#cronUploadTdar()
     */
    @Override
    @Scheduled(fixedDelay = FIVE_MIN_MS)
    @Transactional(readOnly=false)
    public void cronUploadTdar() {
        DropboxClient client;
        try {
            client = new DropboxClient();
            if (client.getDebug() == Boolean.FALSE) {
                itemService.handleUploads();
            }
        } catch (URISyntaxException | IOException | DbxException e) {
            logger.error("{}", e,e);
        }
    }

    /* (non-Javadoc)
     * @see org.tdar.balk.service.BalkScheduledProcessService#cronPollingStatsQueue()
     */
    @Override
    @Scheduled(fixedDelay = ONE_MIN_MS)
    @Transactional(readOnly=false)
    public void cronPollingStatsQueue() {
        try {
            ToPersistListener listener = new ToPersistListener(itemService);
            cursorService.pollAndProcess(PollType.STATUS, DropboxConfig.getInstance().getBaseDropboxPath(), listener);
//            itemService.store(listener);
        } catch (URISyntaxException | IOException | DbxException e) {
            logger.error("polling error {}", e,e);
        }
        logger.debug("completed poling");
    }

}
