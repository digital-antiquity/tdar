package org.tdar.exception;

import java.util.Locale;

import org.tdar.utils.MessageHelper;

public class I18nException extends Exception implements LocalizableException {

    private static final long serialVersionUID = -6989995919647215504L;
    private Locale locale;
    private Object[] values;
    private String message;

    public I18nException() {
        this.setLocale(Locale.getDefault());
    }

    public I18nException(String message) {
        this.message = message;
    }

    public I18nException(String message, Throwable cause) {
        super(cause);
        this.message = message;
    }

    public I18nException(String message, Object... values) {
        this.message = message;
        this.values = values;
    }

    public I18nException(String message, Throwable cause, Object... values) {
        super(cause);
        this.message = message;
        this.values = values;
    }

    public I18nException(Throwable cause) {
        super(cause);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.exception.Localizable#getLocale()
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.exception.Localizable#getLocalizedMessage()
     */
    @Override
    public String getLocalizedMessage() {
        try {
            return MessageHelper.getMessage(message, locale, values);
        } catch (Exception e) {
            return message;
        }
    };

    @Override
    public String getMessage() {
        return getLocalizedMessage();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.exception.Localizable#setLocale(java.util.Locale)
     */
    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
