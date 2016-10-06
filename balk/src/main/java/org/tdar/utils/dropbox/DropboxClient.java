package org.tdar.utils.dropbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.balk.bean.DropboxUserMapping;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderBuilder;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.ListRevisionsErrorException;
import com.dropbox.core.v2.files.ListRevisionsResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.RelocationErrorException;
import com.dropbox.core.v2.users.BasicAccount;
import com.dropbox.core.v2.users.FullAccount;

public class DropboxClient {

    private static final String DbxRequestConfig = null;
    final String DEBUG2 = "debug";
    final String DROPBOX_CURSOR = "dropbox.cursor";
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private File propertiesFile;
    private DbxClientV2 client;
    private DropboxConfig config;
    private Boolean debug = Boolean.TRUE;
    private String currentCursor;

    public DropboxClient() throws URISyntaxException, FileNotFoundException, IOException, DbxException {
        config = new DropboxConfig();
        this.setDebug(Boolean.parseBoolean(config.getProperties().getProperty(DEBUG2, "true")));
        String accessToken = config.getDefaultToken();
        if (accessToken == null) {
            String authorizeUrl = config.getAuthorizedUrl();
            // Have the user sign in and authorize your app.
            System.out.println("1. Go to: " + authorizeUrl);
            System.out.println("2. Click \"Allow\" (you might have to log in first)");
            System.out.println("3. Copy the authorization code.");
            String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
            accessToken = config.finish(code);
            config.getProperties().put(DropboxConfig.DROPBOX_TOKEN, accessToken);
            writePropertiesToFile(propertiesFile, config.getProperties());
        }
        client = new DbxClientV2(config.buildRequest(), config.getDefaultToken());
    }

    public DropboxClient(DropboxUserMapping userMapping) throws FileNotFoundException, URISyntaxException, IOException {
        config = new DropboxConfig();
        logger.trace(userMapping.getToken());
        client = new DbxClientV2(config.buildRequest(), config.getDefaultToken());
    }

    public FullAccount getUserAccount() throws DbxException {
        return client.users().getCurrentAccount();
    }

    private void writePropertiesToFile(File propertiesFile, Properties props) throws FileNotFoundException, IOException {
        FileOutputStream out = new FileOutputStream(propertiesFile);
        props.store(out, "");
        logger.debug("{}", props);
        IOUtils.closeQuietly(out);
    }

    public DbxClientV2 getDbClient() {
        return client;
    }

    public void list(String path, String cursor, MetadataListener listener) throws ListFolderErrorException, DbxException {
        ListFolderResult result = null;
        if (cursor == null) {
            ListFolderBuilder listFolderBuilder = client.files().listFolderBuilder(path);
            result = listFolderBuilder.withRecursive(true).withIncludeDeleted(false).start();
        } else {
            result = client.files().listFolderContinue(cursor);
        }

        while (true) {
            for (Metadata metadata : result.getEntries()) {
                processMetadataItem(listener, metadata);
            }

            if (!result.getHasMore()) {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }
        logger.debug("old cursor:{}", cursor);
        String cursor2 = result.getCursor();
        logger.debug("new cursor:{}", cursor2);
        setCurrentCursor(cursor2);
    }

    public DropboxItemWrapper processMetadataItem(MetadataListener listener, Metadata metadata) {
        logger.trace("{}", metadata);
        if (StringUtils.containsIgnoreCase(metadata.getName(), "Hot Folder Log")) {
            return null;
        }

        DropboxItemWrapper fileWrapper = new DropboxItemWrapper(this, metadata);
        if (listener != null) {
            try {
                logger.debug("consume: {}", fileWrapper);
                listener.consume(fileWrapper);
            } catch (Exception e) {
                logger.error("{}", e, e);
            }
        }
        return fileWrapper;
    }

    private Map<String, BasicAccount> cachedUsers = new HashMap<>();

    public BasicAccount getAccount(String accountId) {
        if (accountId != null) {
            if (cachedUsers.containsKey(accountId)) {
                return cachedUsers.get(accountId);
            }
            try {
                BasicAccount account = client.users().getAccount(accountId);
                cachedUsers.put(accountId, account);
                return account;
            } catch (Exception e) {
                logger.error("{}", e, e);
            }
        }
        return null;
    }

    public FileMetadata getFile(String path, OutputStream os) throws DownloadErrorException, DbxException, IOException {
        DbxDownloader<FileMetadata> download = client.files().download(path);
        FileMetadata fileMetadata = download.download(os);
        return fileMetadata;

    }

    public Metadata move(String from, String to) throws RelocationErrorException, DbxException {
        logger.debug("Moving {} -> {}",from, to);
        Metadata move = client.files().move(from, to);
        logger.trace("\t{}",move);
        return move;
    }
    
    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public String getCurrentCursor() {
        return currentCursor;
    }

    public void setCurrentCursor(String currentCursor) {
        this.currentCursor = currentCursor;
    }

    public List<FileMetadata> getRevisions(String path) throws ListRevisionsErrorException, DbxException {
        ListRevisionsResult listRevisions = client.files().listRevisions(path);
        return listRevisions.getEntries();

    }

    public Metadata copy(String from, String to) throws RelocationErrorException, DbxException {
        logger.debug("Moving {} -> {}",from, to);
        Metadata move = client.files().copy(from, to);
        logger.trace("\t{}",move);
        return move;
    }

}
