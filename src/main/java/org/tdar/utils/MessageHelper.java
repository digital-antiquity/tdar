package org.tdar.utils;

import java.io.Serializable;
import java.util.ResourceBundle;

/*
 * A Singleton helper class for managing Localization and Messages
 */
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

    /*
     * Returns the message in the active ResourceBundle/language for the specified key; if no key is found in the bundle, the key is returned.
     */
    public static String getMessage(String key) {
        if (getInstance().getBundle().containsKey(key)) {
            return getInstance().getBundle().getString(key);
        }
        return key;
    }

    /*
     * Wraps getMessage() with String.format() to enable us to include parameterized replacement
     */
    public static String getMessage(String key, Object ... formatKeys) {
        return String.format(getMessage(key),formatKeys);
    }

    private ResourceBundle getBundle() {
        return bundle;
    }
    private void setBundle(ResourceBundle bundle) {
        MessageHelper.bundle = bundle;
    }
}
