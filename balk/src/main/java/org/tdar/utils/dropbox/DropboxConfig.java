package org.tdar.utils.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;

public class DropboxConfig {

    private static DropboxConfig config;
    private static final String TDAR_CLIENT = "tdar/client";
    final String APP_KEY = "dropbox.key";
    final String APP_SECRET = "dropbox.secret";
    final static String DROPBOX_TOKEN = "dropbox.token";
    private File propertiesFile;
    private Properties props;

    public DropboxConfig() throws URISyntaxException, FileNotFoundException, IOException {
        propertiesFile = new File(DropboxConfig.class.getClassLoader().getResource("dropbox.properties").toURI());
        setProperties(new Properties());
        getProperties().load(new FileInputStream(propertiesFile));
    }

    public String getBaseDropboxPath() {
        String path = getProperties().getProperty("dropbox.path", DropboxConstants.CLIENT_DATA);
        return StringUtils.appendIfMissing(path, "/");
    }

    public String finish(String code) throws DbxException {
        DbxAuthFinish authFinish = getWebAuth().finish(code);
        return authFinish.getAccessToken();
    }

    public String getAuthorizedUrl() {
        DbxWebAuthNoRedirect webAuth = getWebAuth();

        return webAuth.start();
    }

    public DbxRequestConfig buildRequest() {
        DbxAppInfo appInfo = new DbxAppInfo(props.getProperty(APP_KEY), props.getProperty(APP_SECRET));
        DbxRequestConfig config = DbxRequestConfig.newBuilder(TDAR_CLIENT).build();
        return config;
    }

    public DbxWebAuthNoRedirect getWebAuth() {
        DbxAppInfo appInfo = new DbxAppInfo(props.getProperty(APP_KEY), props.getProperty(APP_SECRET));
        DbxRequestConfig config = DbxRequestConfig.newBuilder(TDAR_CLIENT).build();
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        return webAuth;
    }

    public Properties getProperties() {
        return props;
    }

    public void setProperties(Properties props) {
        this.props = props;
    }

    public String getDefaultToken() {
        return props.getProperty(DROPBOX_TOKEN);
    }

    public static DropboxConfig getInstance() {
        if (config == null) {
            try {
                config = new DropboxConfig();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return config;
    }

    public String getUploadPath() {
        return StringUtils.appendIfMissing(getBaseDropboxPath() + DropboxConstants.UPLOAD_TO_TDAR, "/");
    }

    public String getDonePdfa() {
        return StringUtils.appendIfMissing(getBaseDropboxPath() + DropboxConstants.DONE_OCR, "/");
    }

    public String getToPdfaPath() {
        return StringUtils.appendIfMissing(getBaseDropboxPath() + DropboxConstants.TO_PDFA, "/");
    }

    public String getCreatePdfaPath() {
        return StringUtils.appendIfMissing(getBaseDropboxPath() + DropboxConstants.CREATE_PDFA, "/");
    }

    public Object getCombinePath() {
        return StringUtils.appendIfMissing(getBaseDropboxPath() + DropboxConstants.COMBINE_PDF_DIR, "/");
    }

    public String getInputPath() {
        return StringUtils.appendIfMissing(getBaseDropboxPath() + DropboxConstants.INPUT, "/");
    }

    public String getOutputPath() {
        return StringUtils.appendIfMissing(getBaseDropboxPath() + DropboxConstants.OUTPUT, "/");
    }

    public String[] getEmailAddresses() {
        String props = getProperties().getProperty("dropbox.email", "adam.brin@asu.edu , Rachel.Fernandez.1@asu.edu");
        return props.split(",");
    }

}
