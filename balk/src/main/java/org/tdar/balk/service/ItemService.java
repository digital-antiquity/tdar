package org.tdar.balk.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.balk.bean.AbstractDropboxItem;
import org.tdar.balk.bean.DropboxDirectory;
import org.tdar.balk.bean.DropboxFile;
import org.tdar.balk.dao.ItemDao;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.UrlService;
import org.tdar.utils.APIClient;
import org.tdar.utils.ApiClientResponse;
import org.tdar.utils.dropbox.DropboxClient;
import org.tdar.utils.dropbox.DropboxConstants;
import org.tdar.utils.dropbox.DropboxItemWrapper;
import org.tdar.utils.dropbox.ToPersistListener;

@Component
public class ItemService {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private APIClient apiClient;
    boolean loggedIn = false;

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private ItemDao itemDao;

    public ItemService() throws FileNotFoundException, URISyntaxException, IOException {
        apiClient = new APIClient();
    }
    
    @Transactional(readOnly = false)
    public void store(ToPersistListener listener) {
        for (DropboxItemWrapper dropboxItemWrapper : listener.getWrappers()) {
            store(dropboxItemWrapper);
        }
    }

    @Transactional(readOnly = true)
    public DropboxDirectory findParentByPath(String fullPath, boolean isDir) {
        return itemDao.findByParentPath(fullPath, isDir);
    }

    @Transactional(readOnly = true)
    public boolean hasUploaded(String id, boolean dir) {
        AbstractDropboxItem item = itemDao.findByDropboxId(id, dir);
        if (item == null) {
            return false;
        }
        if (item.getTdarId() == null) {
            return false;
        }
        return true;
    }

    @Transactional(readOnly = false)
    public void markUploaded(String id, Long tdarId, boolean dir) {
        AbstractDropboxItem item = itemDao.findByDropboxId(id, dir);
        item.setTdarId(tdarId);
        genericDao.saveOrUpdate(item);

    }

    @Transactional(readOnly = false)
    public void store(DropboxItemWrapper dropboxItemWrapper) {
        if (dropboxItemWrapper  == null || dropboxItemWrapper.getId() == null) {
            logger.warn("id is null for path: {}", dropboxItemWrapper.getFullPath());
            return;
        }
        AbstractDropboxItem item = itemDao.findByDropboxId(dropboxItemWrapper.getId(), dropboxItemWrapper.isDir());
        if (item != null) {
            logger.trace("{} {}", dropboxItemWrapper.getPath(), item);
            return;
        }
        logger.debug("{}", dropboxItemWrapper.getFullPath());
        if (dropboxItemWrapper.isDir()) {
            item = new DropboxDirectory();
        } else {
            item = new DropboxFile();
            ((DropboxFile) item).setExtension(dropboxItemWrapper.getExtension());
        }
        item.setPath(dropboxItemWrapper.getFullPath());
        item.setDateAdded(new Date());
        item.setSize(dropboxItemWrapper.getSize());
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


    @Transactional(readOnly = false)
    public void handleUploads() {
        List<DropboxFile> files = itemDao.findToUpload();
        for (DropboxFile file : files) {
            try {
                upload(file);
            } catch (Exception e) {
                logger.error("{}",e,e);
            }
        }

    }

    private void upload(DropboxFile file) throws IllegalStateException, Exception {
        File rootDir = new File("Client Data", "Upload to tDAR");
        DropboxClient client  = new DropboxClient();
        logger.debug(file.getPath());
        File path = new File(file.getPath()).getParentFile();
        List<String> tree = new ArrayList<>();
        while (!StringUtils.equalsIgnoreCase(path.getName(), rootDir.getName()) &&
                StringUtils.contains(path.getPath(), rootDir.getPath())) {
            tree.add(0, path.getName());
            path = path.getParentFile();
            if (path == null || path.getName().equals("")) {
                break;
            }
        }
        boolean debug = false;
        if (!loggedIn && debug == false) {
            apiClient.apiLogin();
        }
        File actualFile = new File(TdarConfiguration.getInstance().getTempDirectory(), file.getName());
        FileOutputStream fos = new FileOutputStream(actualFile);
        client.getFile(file.getPath(), fos);
        String docXml = makeXml(actualFile, file.getName(), file.getExtension(), StringUtils.join(tree, "/"));
        logger.trace(docXml);
        if (docXml == null) {
            return;
        }
        if (debug == false) {
            logger.debug("uploading: {}", file);
            ApiClientResponse response = apiClient.uploadRecordWithDefaultAccount(docXml, null, actualFile);
            markUploaded(file.getDropboxId(), response.getTdarId(), false);
        }
    }

    private String makeXml(File file, String filename, String extension, String collection)
            throws JAXBException, InstantiationException, IllegalAccessException {
        Class<? extends Resource> cls = Resource.class;
        switch (extension.toLowerCase()) {
            case "doc":
            case "docx":
            case "pdf":
                cls = Document.class;
                break;
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "tif":
            case "tiff":
                cls = Image.class;
                break;
            case "xls":
            case "xlsx":
            case "mdb":
            case "accdb":
            case "tab":
            case "csv":
                cls = Dataset.class;
                break;
            default:
                return null;
        }

        JAXBContext jc = JAXBContext.newInstance(cls);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, UrlService.getProductionSchemaUrl());
        InformationResource object = (InformationResource) cls.newInstance();
        object.setTitle(filename);
        object.setDescription(filename);
        object.setStatus(Status.DRAFT);
        object.setDate(2016);
        ResourceCollection rc = new ResourceCollection(CollectionType.SHARED);
        rc.setHidden(true);
        rc.setName(collection);
        rc.setDescription("(from dropbox)");
        object.getResourceCollections().add(rc);
        StringWriter writer = new StringWriter();
        marshaller.marshal(object, writer);
        return writer.toString();

    }

    @Transactional(readOnly=true)
    public TreeMap<String, WorkflowStatusReport> itemStatusReport() {
        List<DropboxFile> findAll = genericDao.findAll(DropboxFile.class);
        TreeMap<String,WorkflowStatusReport> map = new TreeMap<>();
        for (DropboxFile file : findAll) {
            String key = file.getPath().toLowerCase();
            key = StringUtils.replace(key, "/input/", "/");
            key = StringUtils.replace(key, "/output/", "/");
            key = StringUtils.remove(key, DropboxConstants.CLIENT_DATA.toLowerCase());
            key = StringUtils.substringAfter(key, "/");
            logger.debug(key);
            key = StringUtils.replace(key, "_ocr_pdfa.pdf", ".pdf");
            map.putIfAbsent(key, new WorkflowStatusReport());
            WorkflowStatusReport status = map.get(key);
            String path1 = "/Client Data/Create PDFA/input/";
            String path2 = "/Client Data/Create PDFA/output/";
            String path3 = "/Client Data/Upload to tDAR/";
            if (StringUtils.containsIgnoreCase(file.getPath(), path1)) {
                status.setToPdf(file);
            }
            if (StringUtils.containsIgnoreCase(file.getPath(), path2)) {
                status.setDoneOcr(file);
            }
            if (StringUtils.containsIgnoreCase(file.getPath(), path3)) {
                status.setToUpload(file);
            }
        }
        return map;
    }

}
