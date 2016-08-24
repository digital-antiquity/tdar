package org.tdar.balk.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.balk.bean.AbstractDropboxItem;
import org.tdar.balk.bean.DropboxDirectory;
import org.tdar.balk.bean.DropboxFile;
import org.tdar.core.dao.GenericDao;
import org.tdar.utils.dropbox.DropboxItemWrapper;
import org.tdar.utils.dropbox.ToPersistListener;

@Component
public class ItemService {

    @Autowired
    private GenericDao genericDao;

    @Transactional(readOnly=false)
    public void store(ToPersistListener listener) {
        for (DropboxItemWrapper dropboxItemWrapper : listener.getWrappers()) {
            AbstractDropboxItem item = null;
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
            genericDao.saveOrUpdate(item);
        }
    }

}
