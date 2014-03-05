package org.tdar.utils;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.util.ValueStack;

import edu.emory.mathcs.backport.java.util.Arrays;

/*
 * A Singleton helper class for managing Localization and Messages
 */
public class MessageHelper implements Serializable, TextProvider {

    private static final long serialVersionUID = 3633016404256878510L;
    private static MessageHelper instance;
    private static ResourceBundle bundle;

    private static Logger logger = LoggerFactory.getLogger(MessageHelper.class);

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
        if (checkKey(key)) {
            return getInstance().getBundle().getString(key);
        }
        return key;
    }

    /*
     * Wraps getMessage() with Message.format() to enable us to include parameterized replacement
     */
    public static String getMessage(String lookup, Object ... formatKeys) {
        return getMessage(lookup, Arrays.asList(formatKeys));
    }
    
    public static String getMessage(String lookup, List<Object> formatKeys) {
        String key = lookup;
        if (!StringUtils.contains(lookup, " ")) {
            key = getMessage(lookup);
            if (StringUtils.isBlank(key)) {
                logger.error("looked for localization key: {}, but not found",lookup);
                key = lookup;
            }
        }
        return MessageFormat.format(key,formatKeys);
    }

    private ResourceBundle getBundle() {
        return bundle;
    }
    private void setBundle(ResourceBundle bundle) {
        MessageHelper.bundle = bundle;
    }

    public static boolean checkKey(String key) {
            return getInstance().getBundle().containsKey(key);
    }

    @Override
    public boolean hasKey(String key) {
            return checkKey(key);
    }

    @Override
    public String getText(String key) {
        return getMessage(key);
    }

    @Override
    public String getText(String key, String defaultValue) {
        throw new TdarRecoverableRuntimeException("not.implemented");
    }

    @Override
    public String getText(String key, String defaultValue, String obj) {
        throw new TdarRecoverableRuntimeException("not.implemented");
    }

    @Override
    public String getText(String key, List<?> args) {
        return MessageHelper.getMessage(key, args.toArray());
    }

    @Override
    public String getText(String key, String[] args) {
        return MessageHelper.getMessage(key,args);
    }

    @Override
    public String getText(String key, String defaultValue, List<?> args) {
        throw new TdarRecoverableRuntimeException("not.implemented");
    }

    @Override
    public String getText(String key, String defaultValue, String[] args) {
        throw new TdarRecoverableRuntimeException("not.implemented");
    }

    @Override
    public String getText(String key, String defaultValue, List<?> args, ValueStack stack) {
        throw new TdarRecoverableRuntimeException("not.implemented");
    }

    @Override
    public String getText(String key, String defaultValue, String[] args, ValueStack stack) {
        throw new TdarRecoverableRuntimeException("not.implemented");
    }

    @Override
    public ResourceBundle getTexts(String bundleName) {
        throw new TdarRecoverableRuntimeException("not.implemented");
    }

    @Override
    public ResourceBundle getTexts() {
        throw new TdarRecoverableRuntimeException("not.implemented");
    }

    public static final String formatLocalizableKey(Enum<?> en) {
        return String.format("%s.%s", en.getClass().getSimpleName(), en.name());
    }

}
