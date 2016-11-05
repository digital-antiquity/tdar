package org.tdar.utils.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;

public class DropboxConfig {

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


}
