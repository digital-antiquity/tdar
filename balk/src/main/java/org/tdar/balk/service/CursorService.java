package org.tdar.balk.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.balk.bean.PollType;
import org.tdar.utils.dropbox.MetadataListener;

import com.dropbox.core.DbxException;

public interface CursorService {

    void pollAndProcess(PollType type, String path, MetadataListener listener) throws FileNotFoundException, URISyntaxException, IOException, DbxException;

}