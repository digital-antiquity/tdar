package org.tdar.utils.dropbox;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.users.FullAccount;

//@Component
public class Dropbox {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private boolean all = false;
    private boolean upload = true;

    public static void main(String args[]) throws DbxException, FileNotFoundException, IOException, URISyntaxException {

        // final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        // applicationContext.register(TdarSearchAppConfiguration.class);
        // applicationContext.refresh();
        // applicationContext.start();
        Dropbox db = new Dropbox();

        // applicationContext.getAutowireCapableBeanFactory().autowireBean(db);
        db.run();
    }

    public Dropbox() {
    }

    private void run() throws FileNotFoundException, IOException, URISyntaxException, DbxException {
        DropboxClient client = new DropboxClient();

        // Get current account info
        FullAccount account = client.getUserAccount();
        logger.debug("account: {}", account.getName().getDisplayName());

        // Get files and folder metadata from Dropbox root directory

        if (upload) {
            logger.debug("-----------------  latest  --------------------");
            String cursor = client.getStoredCursor();
            // if (cursor != null) {
            if (all) {
                cursor = null;
            }
            logger.debug("latest cursor:{}", cursor);
            TdarUploadListener listener = new TdarUploadListener(null);
            listener.setDebug(true);
            client.list(DropboxConstants.UPLOAD_PATH, cursor, listener);
            client.updateCursor(client.getCurrentCursor());
        } else {
            StatReporter reporter = new StatReporter();
            client.list("/Client Data/Upload to tDAR/test", null, reporter);
            reporter.report();
        }
    }

}
