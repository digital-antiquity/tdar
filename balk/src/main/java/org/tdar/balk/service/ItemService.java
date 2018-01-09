package org.tdar.balk.service;

import java.util.Set;
import java.util.TreeMap;

import org.tdar.balk.bean.AbstractDropboxItem;
import org.tdar.balk.bean.DropboxDirectory;
import org.tdar.balk.bean.DropboxUserMapping;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.utils.dropbox.DropboxItemWrapper;
import org.tdar.utils.dropbox.ToPersistListener;

public interface ItemService {


    void store(ToPersistListener listener);

    DropboxDirectory findParentByPath(String fullPath, boolean isDir);

    boolean hasUploaded(String id, boolean dir);

    void markUploaded(String id, Long tdarId, boolean dir);

    void store(DropboxItemWrapper dropboxItemWrapper);

    void handleUploads();

    int itemStatusReport(String path, int page, int size, TreeMap<String, WorkflowStatusReport> map, boolean managed);

    AbstractDropboxItem findByDropboxId(String id, boolean dir);

    void move(AbstractDropboxItem item, Phases phase, DropboxUserMapping userMapping, TdarUser tdarUser)
            throws Exception;

    void copy(AbstractDropboxItem item, String newPath, DropboxUserMapping userMapping, TdarUser tdarUser)
            throws Exception;

    Set<String> listChildPaths(String path);

    Set<String> listTopLevelPaths();

    Set<String> listTopLevelManagedPaths();

}