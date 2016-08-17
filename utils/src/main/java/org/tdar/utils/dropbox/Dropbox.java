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

    private static final String DEFAULT_PATH = "/client data/upload to tdar";
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private boolean all = true; 
    public static void main(String args[]) throws DbxException, FileNotFoundException, IOException, URISyntaxException {

//        final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();  
//        applicationContext.register(TdarSearchAppConfiguration.class);  
//        applicationContext.refresh();  
//        applicationContext.start();
        Dropbox db = new Dropbox();
        
//        applicationContext.getAutowireCapableBeanFactory().autowireBean(db);
        db.run();
    }
    
    public Dropbox() {
    }

    private void run() throws FileNotFoundException, IOException, URISyntaxException, DbxException {
        DropboxClient client  = new DropboxClient();

        // Get current account info
        FullAccount account = client.getUserAccount();
        logger.debug("account: {}", account.getName().getDisplayName());

        // Get files and folder metadata from Dropbox root directory

        logger.debug("-----------------  latest  --------------------");
        String cursor = client.getStoredCursor();
//        if (cursor != null) {
        if (all)  {
            cursor = null;
        }
            logger.debug("latest cursor:{}", cursor);
            client.list(DEFAULT_PATH, cursor, new TdarUploadListener(client.getDebug()));
//        }
        client.updateCursor(client.getCurrentCursor());
//        logger.debug("-----------------  all  --------------------");
//        client.list(DEFAULT_PATH, null, null);
    }        

}
