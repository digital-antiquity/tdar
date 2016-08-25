package org.tdar.balk.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.balk.bean.AbstractDropboxItem;
import org.tdar.balk.bean.DropboxDirectory;
import org.tdar.balk.bean.DropboxFile;
import org.tdar.balk.dao.ItemDao;
import org.tdar.core.dao.GenericDao;
import org.tdar.utils.dropbox.DropboxItemWrapper;
import org.tdar.utils.dropbox.ToPersistListener;

@Component
public class ItemService {

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private ItemDao itemDao;

    @Transactional(readOnly = false)
    public void store(ToPersistListener listener) {
        for (DropboxItemWrapper dropboxItemWrapper : listener.getWrappers()) {
            AbstractDropboxItem item = itemDao.findByDropboxId(dropboxItemWrapper.getId());
            if (item != null) {
                continue;
            }
            if (dropboxItemWrapper.isDir()) {
                item = new DropboxDirectory();
            } else {
                item = new DropboxFile();
                ((DropboxFile) item).setExtension(dropboxItemWrapper.getExtension());
            }
            item.setPath(dropboxItemWrapper.getFullPath());
            item.setDateAdded(new Date());
            item.setDateModified(dropboxItemWrapper.getModified());
            item.setDropboxId(dropboxItemWrapper.getId());
            item.setName(dropboxItemWrapper.getName());
            item.setOwnerId(dropboxItemWrapper.getModifiedBy());
            DropboxDirectory parent = findParentByPath(dropboxItemWrapper.getFullPath(), dropboxItemWrapper.isDir());
            if (parent != null) {
                item.setParentId(parent.getDropboxId());
            }
            genericDao.saveOrUpdate(item);
        }
    }

    @Transactional(readOnly = true)
    public DropboxDirectory findParentByPath(String fullPath, boolean isDir) {
        return itemDao.findByParentPath(fullPath, isDir);
    }

    @Transactional(readOnly = true)
    public boolean hasUploaded(String id) {
        AbstractDropboxItem item = itemDao.findByDropboxId(id);
        if (item == null) {
            return false;
        }
        if (item.getTdarId() == null) {
            return false;
        }
        return true;
    }

    @Transactional(readOnly = false)
    public void markUploaded(String id, Long tdarId) {
        AbstractDropboxItem item = itemDao.findByDropboxId(id);
        item.setTdarId(tdarId);
        genericDao.saveOrUpdate(item);

    }

}
