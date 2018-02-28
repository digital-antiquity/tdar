package org.tdar.balk.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.balk.bean.AbstractDropboxItem;
import org.tdar.balk.bean.DropboxDirectory;
import org.tdar.balk.bean.DropboxFile;
import org.tdar.balk.bean.ItemType;
import org.tdar.balk.service.Phases;
import org.tdar.utils.dropbox.DropboxConfig;

@Component
public class ItemDao {

    @Autowired
    private transient SessionFactory sessionFactory;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public DropboxDirectory findByParentPath(String fullPath, boolean dir, boolean archived) {
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
        try {
            // FIXME: FIgure out deleted paths/directories
            Query query2 = getCurrentSession().createQuery(
                    "from DropboxDirectory where dropboxId not like 'deleted%' and  lower(path)=lower(:path) and archived is :archived order by id desc");
            query2.setParameter("path", path);
            query2.setParameter("archived", archived);
            query2.setFirstResult(0);
            query2.setMaxResults(1);
            return (DropboxDirectory) query2.getSingleResult();
        } catch (Exception e) {
            logger.error("in: {}, out: {}, {}", fullPath, path, e, e);
            throw e;
        }
    }

    public AbstractDropboxItem findByDropboxId(String id, boolean dir) {
        String query = "from DropboxFile Item where dropbox_id=:id";
        if (dir) {
            query = "from DropboxDirectory Item where dropbox_id=:id";
        }
        try {
            Query query2 = getCurrentSession().createQuery(query);
            query2.setParameter("id", id);
            return (AbstractDropboxItem) query2.getSingleResult();
        } catch (Exception e) {
            logger.error("{} ({}) -- {}", query, id, e, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<DropboxFile> findToUpload() {
        Query query2 = getCurrentSession().createQuery(
                "from DropboxFile df where lower(path) like lower('%/Upload to tDAR/%') and not exists (select tr from TdarReference tr where df.dropboxId=tr.dropboxId and df.dropboxId not like 'deleted%')");
        return query2.getResultList();
    }

    public int findAllWithPath(String path, List<DropboxFile> findAll, int page, int size, boolean managed, boolean archived) {
        String query = "from DropboxFile where dropboxId not like 'deleted%' and archived is :archived ";
        if (StringUtils.isNotBlank(path) && path != "/") {
            query += " and lower(path) like lower('%/" + path + "/%') )";
        }

        if (managed) {
            query += " AND (";
            boolean first = false;
            for (Phases phase : Phases.values()) {
                if (!first) {
                    first = true;
                } else {
                    query += " OR ";
                }
                query += " lower(path) like '" + phase.getPath().toLowerCase() + "%' ";
            }
            query += " ) ";
        }
        logger.debug(query);
        Query query2 = getCurrentSession().createQuery(query);
        query2.setParameter("archived", archived);
        int total = query2.getResultList().size();
        query2 = getCurrentSession().createQuery(query);
        query2.setParameter("archived", archived);
        query2.setMaxResults(size);
        query2.setFirstResult(size * page);
        findAll.addAll(query2.getResultList());
        return total;
    }

    public Set<String> findTopLevelPaths(String path, boolean archived) {
        Query query = getCurrentSession().createQuery(
                "select name from DropboxDirectory where parentId in (select dropboxId from DropboxDirectory where lower(name)=lower(:path) and archived is :archived) and dropboxId not like 'deleted%' and archived is :archived");
        query.setParameter("path", path);
        query.setParameter("archived", archived);
        Set<String> toReturn = new HashSet<>(query.getResultList());
        toReturn.remove(CONFIG.getCreatePdfaPath());
        toReturn.remove(CONFIG.getCombinePath());
        toReturn.remove(CONFIG.getUploadPath());
        return toReturn;
    }

    private DropboxConfig CONFIG = DropboxConfig.getInstance();

    public Set<String> findTopLevelManagedPaths(boolean archived) {
        Set<String> paths = new HashSet<>();
        paths.addAll(findTopLevelPaths(CONFIG.getUploadPath(), archived));
        paths.addAll(findTopLevelPaths(CONFIG.getInputPath(), archived));
        paths.addAll(findTopLevelPaths(CONFIG.getOutputPath(), archived));
        return paths;
    }

    public DropboxFile findByPath(String fullPath, boolean archived) {
        Query query = getCurrentSession().createQuery("from DropboxFile where lower(path) like :path and archived is :archived");
        query.setParameter("path", fullPath);
        query.setParameter("archived", archived);
        List<DropboxFile> list = query.getResultList();
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public void archive(AbstractDropboxItem item) {
        if (item.getType() == ItemType.DIR) {
            String intialQuery = "update DropboxDirectory set archived=true where dropboxId=:dropboxId";
            Query query = getCurrentSession().createQuery(intialQuery);
            query.setParameter("dropboxId", item.getDropboxId());
            query.executeUpdate();
        } else {
            String intialQuery = "update DropboxFile set archived=true where dropboxId=:dropboxId";
            Query query = getCurrentSession().createQuery(intialQuery);
            query.setParameter("dropboxId", item.getDropboxId());
            query.executeUpdate();
        }
        int numResults = 1;
        int count = 10;
        while (numResults > 0) {
            String repeat = "update dropbox_items set archived=true where parent_id in (select dropbox_id from dropbox_item where archived is true)";
            Query query2 = getCurrentSession().createNativeQuery(repeat);
            numResults = query2.executeUpdate();
            count--;
            if (count == 0) {
                break;
            }
        }
    }

}
