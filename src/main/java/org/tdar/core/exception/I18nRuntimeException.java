package org.tdar.core.exception;

import java.util.Locale;

import org.tdar.utils.MessageHelper;

public class I18nRuntimeException extends RuntimeException implements Localizable {

    private static final long serialVersionUID = -5281745740521596786L;
    private Locale locale;
    
    public I18nRuntimeException() {
        this.setLocale(Locale.getDefault());
    }


    public I18nRuntimeException(String message) {
        super(message);
    }


    public I18nRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }


    public I18nRuntimeException(Throwable cause) {
        super(cause);
    }


    public Locale getLocale() {
        return locale;
    }

    @Override
    public String getLocalizedMessage() {
        return MessageHelper.getMessage(getMessage(),locale);
    };

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
