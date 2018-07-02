package org.tdar.utils;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.base.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.util.ValueStack;

/**
 * A Singleton helper class for managing Localization and Messages
 */
public class MessageHelper implements Serializable, TextProvider {

    private static final String BUNDLE_NAME = "Locales/tdar-messages";
    private static final long serialVersionUID = 3633016404256878510L;
    private final static MessageHelper INSTANCE = new MessageHelper();
    private final static Logger logger = LoggerFactory.getLogger(MessageHelper.class);
    private ResourceBundle bundle;

    protected MessageHelper() {
        this(BUNDLE_NAME);
    }

    protected MessageHelper(String name) {
        try {
            bundle = ResourceBundle.getBundle(name);
        } catch (NoClassDefFoundError | MissingResourceException cnf) {
        }
        if (bundle == null) {
            loadFalback(name);
        }
    }

    private void loadFalback(String name) {
        Logger log = LoggerFactory.getLogger(MessageHelper.class);
        log.warn("loading fallback locale");
        for (String fallback : Arrays.asList("../locales/src/main/resources/", "../../locales/src/main/resources/", "locales/src/main/resources/",
                "../../../locales/src/main/resources/")) {
            if (new File(fallback).exists()) {
                try {
                    List<URL> paths = new ArrayList<>();
                    paths.add(new File(fallback).toURI().toURL());
                    ClassLoader loader = new URLClassLoader(paths.toArray(new URL[0]));
                    log.debug("{}", paths);
                    loadBundle(name, loader);
                    if (bundle != null) {
                        return;
                    }
                } catch (MalformedURLException e) {// TODO Auto-generated catch block
                    log.warn("loading fallback locale");
                    log.error("{}", e, e);
                }
            }
        }
    }

    private void loadBundle(String path, ClassLoader loader) {
        bundle = ResourceBundle.getBundle(path, Locale.getDefault(), loader);
    }

    public MessageHelper(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public Enumeration<String> getKeys() {
        return bundle.getKeys();
    }

    public static MessageHelper getInstance() {
        return INSTANCE;
    }

    /*
     * Returns the message in the active ResourceBundle/language for the specified key; if no key is found in the bundle, the key is returned.
     */
    public static String getMessage(String key) {
        String msg = getString(key);
        return MessageFormat.format(msg, Collections.emptyList());
    }

    public static String getMessage(String lookup, List<?> formatKeys) {
        return getMessage(lookup, formatKeys.toArray());
    }

    /*
     * Wraps getMessage() with Message.format() to enable us to include parameterized replacement
     */
    public static String getMessage(String lookup, Object[] formatKeys) {
        if (logger.isTraceEnabled()) {
            logger.trace("Calling getMessage: {}, {}", lookup, formatKeys);
        }
        String fmt = getString(lookup);
        return MessageFormat.format(fmt, formatKeys);
    }

    /*
     * Wraps getMessage() with Message.format() to enable us to include parameterized replacement
     */
    public static String getMessage(String lookup, Locale locale, Object[] formatKeys) {
        if (logger.isTraceEnabled()) {
            logger.trace("Calling getMessage: {}, {}", lookup, formatKeys);
        }
        String fmt = getString(lookup);
        return MessageFormat.format(fmt, formatKeys);
    }

    /*
     * Wraps getMessage() with Message.format() to enable us to include parameterized replacement
     */
    public static String getMessage(String lookup, Locale locale) {
        return getMessage(lookup);
    }

    /**
     * Return the format string specified by the provided lookup key, or return the lookup key if no string exists with that key
     * 
     * @param key
     * @return
     */
    private static String getString(String key) {
        String str = key;
        if (checkKey(key)) {
            str = getInstance().getBundle().getString(key);
        } else {
            if (!StringUtils.contains(key, " ")) {
                logger.error("localization key not found: {}", key);
            }
        }
        return str;
    }

    private ResourceBundle getBundle() {
        return bundle;
    }

    public static boolean checkKey(String key) {
        return getInstance().getBundle().containsKey(key);
    }

    public boolean containsKey(String key) {
        return getBundle().containsKey(key);
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
        return MessageHelper.getMessage(key, args);
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

    public static final String formatPluralLocalizableKey(Enum<?> en) {
        return String.format("%s.%s_PLURAL", en.getClass().getSimpleName(), en.name());
    }

}
