package org.tdar.utils.dropbox;

public interface MetadataListener {

    void consume(DropboxItemWrapper fileWrapper) throws Exception;

}
