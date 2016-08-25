package org.tdar.balk.dao;

import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.balk.bean.AbstractDropboxItem;
import org.tdar.balk.bean.DropboxDirectory;
import org.tdar.balk.bean.DropboxFile;

@Component
public class ItemDao {

    @Autowired
    private transient SessionFactory sessionFactory;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public DropboxDirectory findByParentPath(String fullPath, boolean dir) {
        String path = fullPath;
        if (dir) {
            path = fullPath;
            if (StringUtils.endsWith(fullPath, "/")) {
                path = StringUtils.substringBeforeLast(fullPath, "/");
            }
        }
        path = FilenameUtils.getPathNoEndSeparator(fullPath);
        if (!StringUtils.startsWith(path, "/")) {
            path = "/" + path;
        }
        logger.trace("in: {} out: {}", fullPath, path);
        String query = "from DropboxDirectory where lower(path)=lower(:path)";
        Query query2 = getCurrentSession().createQuery(query);
        query2.setParameter("path", path);
        return (DropboxDirectory) query2.uniqueResult();
    }

    public AbstractDropboxItem findByDropboxId(String id, boolean dir) {
        String query = "from DropboxFile Item where dropbox_id=:id";
        if (dir) {
            query = "from DropboxDirectory Item where dropbox_id=:id";
        }
        Query query2 = getCurrentSession().createQuery(query);
        query2.setParameter("id", id);
        return (AbstractDropboxItem) query2.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<DropboxFile> findToUpload() {
        String query = "from DropboxFile where tdar_id is null and path like '%/Upload to tDAR/%'";
        Query query2 = getCurrentSession().createQuery(query);
        return  query2.list();
    }

}
