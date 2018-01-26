package org.tdar.core.exception;

import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.utils.MessageHelper;

public class I18nRuntimeException extends RuntimeException implements LocalizableException {

    private static final long serialVersionUID = 6115182705667575524L;
    @SuppressWarnings("unused")
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Locale locale;
    private List<?> values;
    private String message;

    public I18nRuntimeException() {
        this.setLocale(Locale.getDefault());
    }

    public I18nRuntimeException(String message) {
        this.message = message;
    }

    public I18nRuntimeException(String message, Throwable cause) {
        super(cause);
        this.message = message;
    }

    public I18nRuntimeException(String message, List<?> values) {
        this.message = message;
        this.values = values;
    }

    public I18nRuntimeException(String message, Throwable cause, List<?> values) {
        super(cause);
        this.message = message;
        this.values = values;
    }

    public I18nRuntimeException(Throwable cause) {
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
        logger.debug("{} {} {}" , message, locale, values);
        if (message == null) {
//            logger.error("message is null");
            return "messgae is null";
        }
        if (CollectionUtils.isEmpty(values)) {
            return MessageHelper.getMessage(message, locale);
        }
        return MessageHelper.getMessage(message, locale, values.toArray());
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
