package org.tdar.core.exception;

import java.util.Locale;

import org.tdar.utils.MessageHelper;

public class I18nRuntimeException extends RuntimeException implements Localizable {

    private static final long serialVersionUID = 6115182705667575524L;
    private Locale locale;
    private Object[] values;
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

    public I18nRuntimeException(String message, Object ... values) {
        this.message = message;
        this.values = values;
    }


    public I18nRuntimeException(String message, Throwable cause, Object ... values) {
        super(cause);
        this.message = message;
        this.values = values;
    }


    public I18nRuntimeException(Throwable cause) {
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
        return MessageHelper.getMessage(message,locale, values);
    };

  @Override
  public String getMessage() {
      return MessageHelper.getMessage(message, values);
  }


    /* (non-Javadoc)
     * @see org.tdar.core.exception.Localizable#setLocale(java.util.Locale)
     */
    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
