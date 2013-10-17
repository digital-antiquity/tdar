package org.tdar.utils;

import java.io.Serializable;
import java.util.ResourceBundle;

public class MessageHelper implements Serializable {

    private static final long serialVersionUID = 3633016404256878510L;
    private static MessageHelper instance;
    private static ResourceBundle bundle;
    protected MessageHelper() {
       // Exists only to defeat instantiation.
    }
    public static MessageHelper getInstance() {
       if(instance == null) {
          instance = new MessageHelper();
       }
       instance.setBundle(ResourceBundle.getBundle("Locales/tdar-messages"));
       return instance;
    }

    public static String getMessage(String key) {
        if (getInstance().getBundle().containsKey(key)) {
            return getInstance().getBundle().getString(key);
        }
        return key;
    }
    
    private ResourceBundle getBundle() {
        return bundle;
    }
    private void setBundle(ResourceBundle bundle) {
        MessageHelper.bundle = bundle;
    }
}
