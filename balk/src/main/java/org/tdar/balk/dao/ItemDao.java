package org.tdar.balk.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.tdar.balk.service.Phases;
import org.tdar.utils.dropbox.DropboxConstants;

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
        try {
            // FIXME: FIgure out deleted paths/directories
            String query = "from DropboxDirectory where lower(path)=lower(:path) order by id desc";
            Query query2 = getCurrentSession().createQuery(query);
            query2.setParameter("path", path);
            query2.setFirstResult(0);
            query2.setMaxResults(1);
            return (DropboxDirectory) query2.uniqueResult();
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
            return (AbstractDropboxItem) query2.uniqueResult();
        } catch (Exception e) {
            logger.error("{} ({}) -- {}", query, id, e, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<DropboxFile> findToUpload() {
        String query = "from DropboxFile where tdar_id is null and lower(path) like lower('%/"+DropboxConstants.UPLOAD_TO_TDAR+"/%')";
        Query query2 = getCurrentSession().createQuery(query);
        return query2.list();
    }

    public int findAllWithPath(String path, List<DropboxFile> findAll, int page, int size, boolean managed) {
        String query = "from DropboxFile";
        if (StringUtils.isNotBlank(path) && path != "/") {
            query = "from DropboxFile where lower(path) like lower('%/" + path + "/%')";
        }
        
        if (managed) {
            query += " AND (";
            boolean first = false;
            for (Phases phase : Phases.values() ) {
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
        int total = query2.list().size();
        query2 = getCurrentSession().createQuery(query);
        query2.setMaxResults(size);
        query2.setFirstResult(size * page);
        findAll.addAll(query2.list());
        return total;
    }

    public Set<String> findTopLevelPaths(String path) {
        Query query = getCurrentSession().createQuery("select name from DropboxDirectory where parentId in (select dropboxId from DropboxDirectory where lower(name)=lower(:path) )");
        query.setParameter("path", path);
        Set<String> toReturn = new HashSet<>(query.list());
        toReturn.remove(DropboxConstants.CREATE_PDFA);
        toReturn.remove(DropboxConstants.COMBINE_PDF_DIR);
        toReturn.remove(DropboxConstants.UPLOAD_TO_TDAR);
        return toReturn;
    }

    public Set<String> findTopLevelManagedPaths() {
        Set<String> paths = new HashSet<>();
        paths.addAll(findTopLevelPaths(DropboxConstants.UPLOAD_TO_TDAR));
        paths.addAll(findTopLevelPaths(DropboxConstants.INPUT));
        paths.addAll(findTopLevelPaths(DropboxConstants.OUTPUT));
        return paths;
    }

}
