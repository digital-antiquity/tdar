package org.tdar.core.exception;

import java.util.Locale;

public interface Localizable {

    public abstract Locale getLocale();

    public abstract String getLocalizedMessage();

    public abstract void setLocale(Locale locale);

}