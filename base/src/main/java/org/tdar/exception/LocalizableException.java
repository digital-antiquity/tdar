package org.tdar.exception;

import java.util.Locale;

public interface LocalizableException {

    public abstract Locale getLocale();

    public abstract String getLocalizedMessage();

    public abstract void setLocale(Locale locale);

}