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
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.EmailService;
import org.tdar.utils.APIClient;
import org.tdar.utils.ApiClientResponse;
import org.tdar.utils.PersistableUtils;

import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.BasicAccount;

import org.tdar.balk.bean.AbstractDropboxItem;
import org.tdar.balk.bean.DropboxDirectory;
import org.tdar.balk.bean.DropboxFile;
import org.tdar.balk.bean.DropboxUserMapping;
import org.tdar.balk.dao.ItemDao;
import org.tdar.balk.dao.UserDao;
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
    EmailService emailService;
    @Autowired
    UserDao userDao;

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private JavaMailSender mailSender;

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
        if (dropboxItemWrapper == null || dropboxItemWrapper.getId() == null) {
            logger.warn("id is null for path: {}", dropboxItemWrapper.getFullPath());
            return;
        }
        AbstractDropboxItem item = itemDao.findByDropboxId(dropboxItemWrapper.getId(), dropboxItemWrapper.isDir());
        if (item != null) {
            logger.debug("{} {}", dropboxItemWrapper.getPath(), item);
            // fixme: better handling of "move/delete"
            item.setDropboxId("deleted" + item);
            genericDao.saveOrUpdate(item);
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
        item.setOwnerName(dropboxItemWrapper.getModifiedByName());
        DropboxDirectory parent = findParentByPath(dropboxItemWrapper.getFullPath(), dropboxItemWrapper.isDir());
        if (parent != null) {
            item.setParentId(parent.getDropboxId());
        }
        genericDao.saveOrUpdate(item);

    }

    private void sendEmail(String from, String[] to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        // Message message = new MimeMessage(session);
        message.setFrom(from);
        message.setSubject(subject);
        message.setTo(to);
        message.setText(text);
        try {
            mailSender.send(message);
        } catch (MailException me) {
            logger.error("email error: {} {}", message, me);
        }

    }

    @Transactional(readOnly = false)
    public void handleUploads() {
        List<DropboxFile> files = itemDao.findToUpload();
        StringBuilder msg = new StringBuilder();

        for (DropboxFile file : files) {
            try {
                upload(file);
                if (PersistableUtils.isNotNullOrTransient(file.getTdarId())) {
                    msg.append(" - ").append(file.getName()).append(" (").append(file.getTdarId()).append(")\n");
                }
            } catch (Exception e) {
                logger.error("{}", e, e);
            }
        }
        if (CollectionUtils.isNotEmpty(files) && StringUtils.isNotBlank(msg.toString())) {
            msg.insert(0, "the following files were uploaded to tDAR:\n");
            sendEmail("balk@tdar.org", new String[]{"adam.brin@asu.edu","Rachel.Fernandez.1@asu.edu"}, "Uploaded files to tDAR", msg.toString());
        }

    }

    private void upload(DropboxFile file) throws IllegalStateException, Exception {
        File rootDir = new File(DropboxConstants.UPLOAD_PATH);
        DropboxClient client = new DropboxClient();
        logger.debug(file.getPath());
        File path = new File(file.getPath()).getParentFile();
        List<String> tree = new ArrayList<>();
        while (!StringUtils.equalsIgnoreCase(path.getName(), rootDir.getName()) &&
                StringUtils.containsIgnoreCase(path.getPath(), rootDir.getPath())) {
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
        BasicAccount account = client.getAccount(file.getOwnerId());
        DropboxUserMapping mapping = userDao.getUserForDropboxAccount(account);
        String username = null;
        if (mapping != null) {
            username = mapping.getUsername();
        }
        String docXml = makeXml(actualFile, file.getName(), file.getExtension(), StringUtils.join(tree, "/"), username);
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

    private String makeXml(File file, String filename, String extension, String collection, String username)
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
        SharedCollection rc = new SharedCollection();
        rc.setHidden(true);
        if (StringUtils.isNotBlank(username)) {
            rc.getAuthorizedUsers().add(new AuthorizedUser(new TdarUser(null, null, null, username), GeneralPermissions.ADMINISTER_GROUP));
        }
        rc.setName(collection);
        rc.setDescription("(from dropbox)");
        object.getSharedCollections().add(rc);
        StringWriter writer = new StringWriter();
        marshaller.marshal(object, writer);
        return writer.toString();

    }

    @Transactional(readOnly = true)
    public int itemStatusReport(String path, int page, int size, TreeMap<String, WorkflowStatusReport> map, boolean managed) {
        List<DropboxFile> findAll = new ArrayList<>();
        int total = itemDao.findAllWithPath(path, findAll, page, size, managed);
        for (DropboxFile file : findAll) {
            String key = Phases.createKey(file);
            map.putIfAbsent(key, new WorkflowStatusReport());
            WorkflowStatusReport status = map.get(key);
            if (status.getFirst() == null) {
                status.setFirst(file);
            }

            for (Phases phase : Phases.values()) {
                phase.updateStatus(status, file);
            }
        }
        return total;
    }

    public AbstractDropboxItem findByDropboxId(String id, boolean dir) {
        return itemDao.findByDropboxId(id, dir);
    }

    @Transactional(readOnly = false)
    public void move(AbstractDropboxItem item, Phases phase, DropboxUserMapping userMapping, TdarUser tdarUser)
            throws Exception {
        DropboxClient client = new DropboxClient(userMapping);
        Metadata move = client.move(item.getPath(), phase.mutatePath(item.getPath()));
        client = new DropboxClient();
        ToPersistListener listener = new ToPersistListener(this);
        client.processMetadataItem(listener, move);
        logger.debug("storing: {} {}", listener, listener.getWrappers());
        store(listener);

    }

    @Transactional(readOnly = false)
    public void copy(AbstractDropboxItem item, String newPath, DropboxUserMapping userMapping, TdarUser tdarUser)
            throws Exception {
        DropboxClient client = new DropboxClient(userMapping);
        // FIGURE OUT WHAT PHASE, FIGURE OUT WHAT PATH
        Metadata move = client.copy(item.getPath(), newPath);
        // client = new DropboxClient();
        // ToPersistListener listener = new ToPersistListener(this);
        // client.processMetadataItem(listener, move);
        // logger.debug("storing: {} {}", listener,listener.getWrappers());
        // store(listener);
    }

    
    @Transactional(readOnly=false)
    public Set<String> listChildPaths(String path) {
        return itemDao.findTopLevelPaths(path);
    }

    @Transactional(readOnly = true)
    public Set<String> listTopLevelPaths() {
        return itemDao.findTopLevelPaths(DropboxConstants.CLIENT_DATA.replace("/", ""));
    }

    @Transactional(readOnly = true)
    public Set<String> listTopLevelManagedPaths() {
        return itemDao.findTopLevelManagedPaths();
    }

}
