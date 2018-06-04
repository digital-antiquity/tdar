package org.tdar.core.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.ImportFileStatus;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.dao.base.HibernateBase;
import org.tdar.utils.PersistableUtils;

@Component
public class FileProcessingDao extends HibernateBase<TdarFile> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public FileProcessingDao() {
        super(TdarFile.class);
    }

    public List<TdarFile> findFiles(ImportFileStatus... status) {
        List<ImportFileStatus> statuses = new ArrayList<ImportFileStatus>();
        statuses.addAll(Arrays.asList(status));
        Query<TdarFile> query = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_FILES_BY_STATUS);
        query.setParameter("status", statuses);
        List<TdarFile> resultList = query.getResultList();

        if (ArrayUtils.contains(status, ImportFileStatus.UPLOADED)) {
            Query<TdarFile> query2 = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_FILES_BY_STATUS);
            query2.setParameter("status", Arrays.asList(ImportFileStatus.VALIDATION_FAILED));
            List<TdarFile> resultList2 = query2.getResultList();
        }
        Map<TdarDir, List<TdarFile>> uploaded = new HashMap<>();
        Map<TdarDir, List<TdarFile>> invalid = new HashMap<>();
        // for (TdarFile file : )

        return resultList;
    }

    public List<AbstractFile> listFilesFor(TdarDir parent, BillingAccount account, String term, FileOrder sort, TdarUser authenticatedUser) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.LIST_FILES_FOR_DIR);
        if (parent == null && StringUtils.isNotBlank(term)) {
            query.setParameter("topLevel", true);
            query.setParameter("parentId", -1L);

        } else if (parent == null) {
            query.setParameter("topLevel", true);
            query.setParameter("parentId", -1L);
        } else {
            query.setParameter("parentId", parent.getId());
            query.setParameter("topLevel", false);
        }
        query.setParameter("account", account);
        if (StringUtils.isBlank(term)) {
            query.setParameter("term", null);
        } else {
            query.setParameter("term", "%" + term.toLowerCase() + "%");
        }
        if (account == null) {
            query.setParameter("uploader", authenticatedUser);
        } else {
            query.setParameter("uploader", null);
        }
        List<AbstractFile> list = new ArrayList<>(query.getResultList());
        if (sort != null) {
            sort.sort(list, sort);
        }
        return list;
    }

    public TdarDir findUnfiledDirByName(TdarUser authenticatedUser) {
        try {
            Query<TdarDir> query = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_DIR_BY_NAME);
            query.setParameter("name", TdarDir.UNFILED);
            query.setParameter("account", null);
            query.setParameter("uploader", authenticatedUser);
            return query.getSingleResult();
        } catch (Exception e) {
            TdarDir dir = new TdarDir();
            dir.setDateCreated(new Date());
            dir.setFilename(TdarDir.UNFILED);
            dir.setInternalName(TdarDir.UNFILED);
            dir.setUploader(authenticatedUser);
            saveOrUpdate(dir);
            return dir;
        }
    }

    public List<TdarDir> listDirectoriesFor(TdarDir parent, BillingAccount account, TdarUser authenticatedUser) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.LIST_DIR);
        if (parent == null) {
            query.setParameter("topLevel", true);
            query.setParameter("parentId", -1L);
        } else {
            query.setParameter("parentId", parent.getId());
            query.setParameter("topLevel", false);
        }
        query.setParameter("account", account);
        return query.getResultList();
    }

    public DirSummary summerizeByAccount(BillingAccount account, Date date, TdarUser authenticatedUser) {
        // Note: these methods are not recursive, and users will probably expect them to be, that is, that the parent directory summarizes all children the way
        // down...
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.SUMMARIZE_BY_ACCOUNT);
        if (date == null) {
            query.setParameter("date", null);
        } else {
            query.setParameter("date", date);
        }
        query.setParameter("accountId", account.getId());
        DirSummary summary = new DirSummary();
        List<Object[]> resultList = (List<Object[]>) query.getResultList();

        List<TdarDir> dirs = listDirectoriesFor(null, account, authenticatedUser);
        Map<Long, TdarDir> dirIdMap = PersistableUtils.createIdMap(dirs);
        Map<Long, Set<Long>> dirChildMap = new HashMap<>();
        Set<Long> topLevel = new HashSet<>();

        buildChildMapAndTopLevelNodes(dirs, dirChildMap, topLevel);

        // setup each dirSummaryPart
        Map<Long, DirSummaryPart> partMap = new HashMap<>();
        for (Object[] row : resultList) {
            DirSummaryPart part = summary.addPart(row);
            setupPart(dirIdMap, partMap, part);
        }
        // there may be parts that are mid-level directories that are empty, we don't add them, but they should be in the parts array 
        summary.getParts().addAll(partMap.values());

        summarizeChildren(dirChildMap, topLevel, partMap, dirIdMap);
        return summary;
    }

    private void setupPart(Map<Long, TdarDir> dirIdMap, Map<Long, DirSummaryPart> partMap, DirSummaryPart part) {
        part.setDir(dirIdMap.get(part.getId()));
        part.setDirPath(buildDirTree(part));
        partMap.put(part.getId(), part);
    }

    private void buildChildMapAndTopLevelNodes(List<TdarDir> dirs, Map<Long, Set<Long>> dirChildMap, Set<Long> topLevel) {
        for (TdarDir dir : dirs) {
            if (dir.getParentId() == null) {
                continue;
            }
            TdarDir dir_ = dir;
            while (dir_ != null) {
                Long parentId = dir_.getParentId();
                Set<Long> children = dirChildMap.getOrDefault(parentId, new HashSet<>());
                children.add(dir_.getId());
                dirChildMap.put(parentId, children);

                if (dir_.getParent() == null) {
                    topLevel.add(dir_.getId());
                }
                dir_ = dir_.getParent();
            }
        }
    }

    /**
     * Recursively summarize all children from top down so we don't double count
     * 
     * @param dirChildMap
     * @param topLevel
     * @param partMap
     */
    private void summarizeChildren(Map<Long, Set<Long>> dirChildMap, Set<Long> topLevel, Map<Long, DirSummaryPart> partMap, Map<Long,TdarDir> dirMap) {
        for (Long id : topLevel) {
            if (CollectionUtils.isEmpty(dirChildMap.get(id))) {
                continue;
            }
            Set<Long> working = new HashSet<>(dirChildMap.get(id));
            Set<Long> allChildren = new HashSet<>(working);
            while (!working.isEmpty()) {
                Iterator<Long> iterator = working.iterator();
                Long next = iterator.next();
                iterator.remove();

                Set<Long> nextChild = dirChildMap.get(next);
                if (CollectionUtils.isNotEmpty(nextChild)) {
                    working.addAll(nextChild);
                    allChildren.addAll(nextChild);
                }
            }
            DirSummaryPart part = partMap.get(id);
            if (part == null) {
                part = new DirSummaryPart(null);
                part.setId(id);
                setupPart(dirMap, partMap, part);
            }
            
            part.addAll(allChildren, partMap);
            if (CollectionUtils.isNotEmpty(dirChildMap.get(id))) {
                summarizeChildren(dirChildMap, dirChildMap.get(id), partMap, dirMap);
            }
        }
    }

    private String buildDirTree(DirSummaryPart part) {
        TdarDir parent = part.getDir();
        if (parent == null) {
            return "/";
        }
        StringBuilder path = new StringBuilder();
        TdarDir d = parent;
        while (d != null) {
            path.insert(0, "/" + d.getName());
            d = d.getParent();
        }
        return path.toString();
    }

    public List<TdarFile> recentByAccount(BillingAccount account, Date date, TdarDir dir, TdarUser authenticatedUser) {
        // Note: these methods are not recursive, and users will probably expect them to be, that is, that the parent directory summarizes all children the way
        // down...
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.BY_ACCOUNT_RECENT);
        if (date == null) {
            query.setParameter("date", null);
        } else {
            query.setParameter("date", date);
        }
        if (dir == null) {
            query.setParameter("dir", null);
        } else {
            query.setParameter("dir", dir);
        }
        query.setParameter("accountId", account.getId());
        return query.getResultList();
    }

}
