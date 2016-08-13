package org.tdar.utils.dropbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxRequestConfig.Builder;
import com.dropbox.core.DbxWebAuthNoRedirect;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

public class DropboxClient {

    final String APP_KEY = "dropbox.key";
    final String APP_SECRET = "dropbox.secret";
    final String DROPBOX_TOKEN = "dropbox.token";
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private File propertiesFile;
    private DbxClientV2 client;
    private Properties props;

    public DropboxClient() throws URISyntaxException, FileNotFoundException, IOException, DbxException {
        props = loadProperties();
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
        logger.debug(props.getProperty(DROPBOX_TOKEN));
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
        return (String) props.get("dropbox.cursor");
    }

    public void updateCursor(String cursor) throws FileNotFoundException, IOException {
        props.put("dropbox.cursor", cursor);
        logger.debug("cursor:{}",cursor);
        writePropertiesToFile(propertiesFile, props);
        
    }

}
