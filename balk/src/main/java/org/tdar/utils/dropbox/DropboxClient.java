package org.tdar.utils.dropbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxRequestConfig.Builder;
import com.dropbox.core.DbxWebAuthNoRedirect;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderBuilder;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.ListRevisionsErrorException;
import com.dropbox.core.v2.files.ListRevisionsResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.BasicAccount;
import com.dropbox.core.v2.users.FullAccount;

public class DropboxClient {

    final String DEBUG2 = "debug";
    final String DROPBOX_CURSOR = "dropbox.cursor";
    final String APP_KEY = "dropbox.key";
    final String APP_SECRET = "dropbox.secret";
    final String DROPBOX_TOKEN = "dropbox.token";
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private File propertiesFile;
    private DbxClientV2 client;
    private Properties props;
    private Boolean debug = Boolean.TRUE;
    private String currentCursor;

    public DropboxClient() throws URISyntaxException, FileNotFoundException, IOException, DbxException {
        props = loadProperties();
        this.setDebug(Boolean.parseBoolean(props.getProperty(DEBUG2, "true")));
        String accessToken = props.getProperty(DROPBOX_TOKEN);
        if (accessToken == null) {
            DbxAppInfo appInfo = new DbxAppInfo(props.getProperty(APP_KEY), props.getProperty(APP_SECRET));

            DbxRequestConfig config = DbxRequestConfig.newBuilder("tdar/client").build();
            DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);

            String authorizeUrl = webAuth.start();
            // Have the user sign in and authorize your app.
            System.out.println("1. Go to: " + authorizeUrl);
            System.out.println("2. Click \"Allow\" (you might have to log in first)");
            System.out.println("3. Copy the authorization code.");
            String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
            DbxAuthFinish authFinish = webAuth.finish(code);
            accessToken = authFinish.getAccessToken();
            props.put(DROPBOX_TOKEN, accessToken);
            writePropertiesToFile(propertiesFile, props);
        }
        Builder builder = DbxRequestConfig.newBuilder("dropbox/java-tutorial");
        DbxRequestConfig config = builder.build();
        logger.trace(props.getProperty(DROPBOX_TOKEN));
        client = new DbxClientV2(config, props.getProperty(DROPBOX_TOKEN));
    }

    public FullAccount getUserAccount() throws DbxException {
        return client.users().getCurrentAccount();
    }
    
    private Properties loadProperties() throws URISyntaxException, IOException, FileNotFoundException {
        propertiesFile = new File(Dropbox.class.getClassLoader().getResource("dropbox.properties").toURI());
        Properties props = new Properties();
        props.load(new FileInputStream(propertiesFile));
        return props;
    }
    
    private void writePropertiesToFile(File propertiesFile, Properties props) throws FileNotFoundException, IOException {
        FileOutputStream out = new FileOutputStream(propertiesFile);
        props.store(out, "");
        logger.debug("{}",props);
        IOUtils.closeQuietly(out);
    }

    public DbxClientV2 getDbClient() {
        return client;
    }

    public String getStoredCursor() {
        return (String) props.get(DROPBOX_CURSOR);
    }

    public void updateCursor(String cursor) throws FileNotFoundException, IOException {
        props.put(DROPBOX_CURSOR, cursor);
        logger.debug("new cursor:{}",cursor);
        writePropertiesToFile(propertiesFile, props);
        
    }

    public void list(String path,String cursor, MetadataListener listener) throws ListFolderErrorException, DbxException {
        ListFolderResult result = null;
        if (cursor == null) {
            ListFolderBuilder listFolderBuilder = client.files().listFolderBuilder(path);
            result = listFolderBuilder.withRecursive(true).withIncludeDeleted(false).start();
        } else {
            result = client.files().listFolderContinue(cursor);
        }
        

        while (true) {
            for (Metadata metadata : result.getEntries()) {
                if (StringUtils.containsIgnoreCase(metadata.getName(), "Hot Folder Log")) {
                    continue;
                }
                
                DropboxItemWrapper fileWrapper = new DropboxItemWrapper(this, metadata);
                if (listener != null) {
                    try {
                        listener.consume(fileWrapper);
                    } catch (Exception e) {
                        logger.error("{}",e,e);
                    }
                }
            }

            if (!result.getHasMore()) {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }
//        String cursor2 = client.files().listFolderGetLatestCursor(path).getCursor();
        logger.debug("old cursor:{}", cursor);
//        logger.debug("new cursor:{}", cursor2);
        setCurrentCursor(cursor);
    }

    private Map<String,BasicAccount> cachedUsers = new HashMap<>();

    public BasicAccount getAccount(String accountId) {
        if (accountId != null) {
            if (cachedUsers.containsKey(accountId)) {
                return cachedUsers.get(accountId);
            }
            try {
                BasicAccount account = client.users().getAccount(accountId);
                cachedUsers.put(accountId, account);
                return account;
            } catch (DbxException e) {
                logger.error("{}",e,e);
            }
        }
        return null;
    }

    public FileMetadata getFile(String path, OutputStream os) throws DownloadErrorException, DbxException, IOException {
        DbxDownloader<FileMetadata> download = client.files().download(path);
        FileMetadata fileMetadata = download.download(os);
        return fileMetadata;
        
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

}
