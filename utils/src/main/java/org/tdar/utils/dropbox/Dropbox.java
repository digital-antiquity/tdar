package org.tdar.utils.dropbox;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
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
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

public class Dropbox {

    private static final String DROPBOX_TOKEN = "dropbox.token";
    private static final transient Logger logger = LoggerFactory.getLogger(Dropbox.class);

    public static void main(String args[]) throws DbxException, FileNotFoundException, IOException, URISyntaxException {

        final String APP_KEY = "dropbox.key";
        final String APP_SECRET = "dropbox.secret";
        File propertiesFile = new File(Dropbox.class.getClassLoader().getResource("dropbox.properties").toURI());
        Properties props = new Properties();
        props.load(new FileInputStream(propertiesFile));
        String accessToken = props.getProperty(DROPBOX_TOKEN);
        if (accessToken == null) {
            DbxAppInfo appInfo = new DbxAppInfo(props.getProperty(APP_KEY), props.getProperty(APP_SECRET));

            DbxRequestConfig config = new DbxRequestConfig(
                    "JavaTutorial/1.0", Locale.getDefault().toString());
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
        DbxClientV2 client = new DbxClientV2(config, props.getProperty(DROPBOX_TOKEN));

        // Get current account info
        FullAccount account = client.users().getCurrentAccount();
        logger.debug("account: {}", account.getName().getDisplayName());

        // Get files and folder metadata from Dropbox root directory
        ListFolderResult result = client.files().listFolder("/client data");
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                 System.out.println(metadata.getPathLower());
            }

            if (!result.getHasMore()) {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }
    }

    private static void writePropertiesToFile(File propertiesFile, Properties props) throws FileNotFoundException, IOException {
        FileOutputStream out = new FileOutputStream(propertiesFile);
        props.store(out, "");
        IOUtils.closeQuietly(out);
    }
}
