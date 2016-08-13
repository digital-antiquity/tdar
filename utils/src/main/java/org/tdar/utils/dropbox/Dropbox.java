package org.tdar.utils.dropbox;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderBuilder;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

public class Dropbox {

    private static final String DEFAULT_PATH = "/client data/upload to tdar";
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String args[]) throws DbxException, FileNotFoundException, IOException, URISyntaxException {

        Dropbox db = new Dropbox();
        db.run();
    }

    private void run() throws FileNotFoundException, IOException, URISyntaxException, DbxException {
        DropboxClient client  = new DropboxClient();

        // Get current account info
        FullAccount account = client.getUserAccount();
        logger.debug("account: {}", account.getName().getDisplayName());

        // Get files and folder metadata from Dropbox root directory
        DbxClientV2 dbclient = client.getDbClient();
        ListFolderResult result = dbclient.files().listFolder("/client data");
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                logger.debug(metadata.getPathLower());
                // logger.debug("\t{}", metadata.toStringMultiline());
            }

            if (!result.getHasMore()) {
                break;
            }

            result = dbclient.files().listFolderContinue(result.getCursor());
        }

        logger.debug("-------------------------------------");
        String cursor = client.getStoredCursor();
        if (cursor != null) {
            logger.debug("latest cursor:{}", cursor);
            ListFolderResult continue1 = dbclient.files().listFolderContinue(cursor);
            logger.debug("{}", continue1);
        }

        logger.debug("-------------------------------------");
        ListFolderBuilder listFolderBuilder = dbclient.files().listFolderBuilder(DEFAULT_PATH);

        result = listFolderBuilder.withRecursive(true).withIncludeDeleted(false).start();
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                logger.debug(metadata.getPathLower());
            }

            if (!result.getHasMore()) {
                break;
            }

            result = dbclient.files().listFolderContinue(result.getCursor());
        }
        client.updateCursor(result.getCursor());
    }        

}
