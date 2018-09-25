package org.tdar.fileprocessing.workflows;

import org.tdar.db.conversion.converters.AbstractDatabaseConverter;

public interface HasDatabaseConverter {

    Class<? extends AbstractDatabaseConverter> getDatabaseConverterForExtension(String ext);

}
