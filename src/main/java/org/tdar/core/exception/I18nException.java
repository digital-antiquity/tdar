package org.tdar.core.exception;

import java.util.Locale;

import org.tdar.utils.MessageHelper;

public class I18nException extends Exception implements Localizable {

    private static final long serialVersionUID = 6115182705667575524L;
    private Locale locale;
    
    public I18nException() {
        this.setLocale(Locale.getDefault());
    }


    public I18nException(String message) {
        super(message);
    }


    public I18nException(String message, Throwable cause) {
        super(message, cause);
    }


    public I18nException(Throwable cause) {
        super(cause);
    }


    /* (non-Javadoc)
     * @see org.tdar.core.exception.Localizable#getLocale()
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.exception.Localizable#getLocalizedMessage()
     */
    @Override
    public String getLocalizedMessage() {
        return MessageHelper.getMessage(getMessage(),locale);
    };

    /* (non-Javadoc)
     * @see org.tdar.core.exception.Localizable#setLocale(java.util.Locale)
     */
    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
