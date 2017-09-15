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
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.balk.bean.AbstractDropboxItem;
import org.tdar.balk.bean.DropboxDirectory;
import org.tdar.balk.bean.DropboxFile;
import org.tdar.balk.bean.DropboxUserMapping;
import org.tdar.balk.bean.TdarReference;
import org.tdar.balk.dao.ItemDao;
import org.tdar.balk.dao.UserDao;
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
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.external.EmailService;
import org.tdar.utils.APIClient;
import org.tdar.utils.ApiClientResponse;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.dropbox.DropboxClient;
import org.tdar.utils.dropbox.DropboxConfig;
import org.tdar.utils.dropbox.DropboxItemWrapper;
import org.tdar.utils.dropbox.ToPersistListener;

import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.BasicAccount;

@Component
public class ItemServiceImpl implements ItemService {

    private static final String DELETED = "deleted:";
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private APIClient apiClient;
    boolean loggedIn = false;
    private DropboxConfig config = DropboxConfig.getInstance();

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

    public ItemServiceImpl() throws FileNotFoundException, URISyntaxException, IOException {
        apiClient = new APIClient();
    }

    /* (non-Javadoc)
     * @see org.tdar.balk.service.ItemService#store(org.tdar.utils.dropbox.ToPersistListener)
     */
    @Override
    @Transactional(readOnly = false)
    public void store(ToPersistListener listener) {
        for (DropboxItemWrapper dropboxItemWrapper : listener.getWrappers()) {
            store(dropboxItemWrapper);
        }
    }

    /* (non-Javadoc)
     * @see org.tdar.balk.service.ItemService#findParentByPath(java.lang.String, boolean)
     */
    @Override
    @Transactional(readOnly = true)
    public DropboxDirectory findParentByPath(String fullPath, boolean isDir) {
        return itemDao.findByParentPath(fullPath, isDir);
    }

    /* (non-Javadoc)
     * @see org.tdar.balk.service.ItemService#hasUploaded(java.lang.String, boolean)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasUploaded(String id, boolean dir) {
        AbstractDropboxItem item = itemDao.findByDropboxId(id, dir);
        if (item == null) {
            return false;
        }
        if (CollectionUtils.isEmpty(item.getTdarReferences())) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.tdar.balk.service.ItemService#markUploaded(java.lang.String, java.lang.Long, boolean)
     */
    @Override
    @Transactional(readOnly = false)
    public void markUploaded(String id, Long tdarId, boolean dir) {
        if (PersistableUtils.isNotNullOrTransient(tdarId)) {
            AbstractDropboxItem item = itemDao.findByDropboxId(id, dir);
            item.getTdarReferences().add(new TdarReference(id, tdarId));
            genericDao.saveOrUpdate(item);
            genericDao.saveOrUpdate(item.getTdarReferences());
        }

    }

    /* (non-Javadoc)
     * @see org.tdar.balk.service.ItemService#store(org.tdar.utils.dropbox.DropboxItemWrapper)
     */
    @Override
    @Transactional(readOnly = false)
    public void store(DropboxItemWrapper dropboxItemWrapper) {
        if (dropboxItemWrapper == null || dropboxItemWrapper.getId() == null) {
            // when something is deleted, the first step is that an event for the file with  a .tag:deleted is passed
            logger.warn("id is null for path: {} (deleted: {})", dropboxItemWrapper.getFullPath(), dropboxItemWrapper.isDeleted());
            DropboxFile file = itemDao.findByPath(dropboxItemWrapper.getFullPath());
            if (file != null && dropboxItemWrapper.isDeleted()) {
                file.setDropboxId(DELETED + file.getDropboxId());
                genericDao.saveOrUpdate(file);
            }
            logger.debug("{}", dropboxItemWrapper.getMetadata());
            return;
        }
        AbstractDropboxItem item = itemDao.findByDropboxId(dropboxItemWrapper.getId(), dropboxItemWrapper.isDir());
        if (item != null) {
            // second time... i shows up separately
            logger.debug("move/delete {} | {}", dropboxItemWrapper.getPath(), item.getDropboxId());
            logger.debug("{}", dropboxItemWrapper.getMetadata());
            // fixme: better handling of "move/delete"
            if (dropboxItemWrapper.isDeleted()) {
                item.setDropboxId(DELETED + item.getDropboxId());
            } else if (StringUtils.contains(item.getDropboxId(), DELETED)) {
                item.setDropboxId(item.getDropboxId().replace(DELETED, ""));
            }
            updatePathInfo(dropboxItemWrapper, item);
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
        item.setDateAdded(new Date());
        item.setSize(dropboxItemWrapper.getSize());
        item.setDateModified(dropboxItemWrapper.getModified());
        item.setDropboxId(dropboxItemWrapper.getId());
        item.setOwnerId(dropboxItemWrapper.getModifiedBy());
        item.setOwnerName(dropboxItemWrapper.getModifiedByName());
        updatePathInfo(dropboxItemWrapper, item);
        genericDao.saveOrUpdate(item);

    }

    private void updatePathInfo(DropboxItemWrapper dropboxItemWrapper, AbstractDropboxItem item) {
        item.setPath(dropboxItemWrapper.getFullPath());
        item.setName(dropboxItemWrapper.getName());
        DropboxDirectory parent = findParentByPath(dropboxItemWrapper.getFullPath(), dropboxItemWrapper.isDir());
        if (parent != null) {
            item.setParentId(parent.getDropboxId());
        }
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

    /* (non-Javadoc)
     * @see org.tdar.balk.service.ItemService#handleUploads()
     */
    @Override
    @Transactional(readOnly = false)
    public void handleUploads() {
        List<DropboxFile> files = itemDao.findToUpload();
        StringBuilder msg = new StringBuilder();

        for (DropboxFile file : files) {
            if (file.getDropboxId().startsWith("deleted")) {
                continue;
            }
            try {
                logger.debug("{} --> {} (should be none)", file.getDropboxId(), file.getTdarReferences());
                upload(file);
                TdarReference ref = file.getTdarReference();
                if (ref != null && PersistableUtils.isNotNullOrTransient(ref.getTdarId())) {
                    msg.append(String.format(" - %s (%s)\n",file.getName(), ref.getTdarId()));
                }
            } catch (Exception e) {
                logger.error("{}", e, e);
            }
        }
        if (CollectionUtils.isNotEmpty(files) && StringUtils.isNotBlank(msg.toString())) {
            msg.insert(0, "the following files were uploaded to tDAR:\n");
            sendEmail("balk@tdar.org", config.getEmailAddresses(), "Uploaded files to tDAR", msg.toString());
        }

    }

    private void upload(DropboxFile file) throws IllegalStateException, Exception {
        File rootDir = new File(config.getUploadPath());
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
            ApiClientResponse response = uploadFile(file, actualFile, docXml);
            markUploaded(file.getDropboxId(), response.getTdarId(), false);
        }
    }

    private ApiClientResponse uploadFile(DropboxFile file, File actualFile, String docXml) throws ClientProtocolException, IOException {
        Long accountId = apiClient.getDefaultAccount();
        if (file.getAccountId() != null) {
            accountId = file.getAccountId();
        } else {
            String parentId = file.getParentId();
            while (parentId != null) {
                AbstractDropboxItem parent = findByDropboxId(parentId, true);
                if (parent.getAccountId() != null) {
                    accountId = parent.getAccountId();
                    break;
                }
                parentId = parent.getParentId();
            }
        }
        ApiClientResponse response = apiClient.uploadRecord(docXml, null, accountId, actualFile);
        return response;
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
        if (StringUtils.isNotBlank(collection)) {
            SharedCollection rc = new SharedCollection();
            rc.setHidden(true);
            if (StringUtils.isNotBlank(username)) {
                rc.getAuthorizedUsers().add(new AuthorizedUser(null, new TdarUser(null, null, null, username), GeneralPermissions.ADMINISTER_GROUP));
            }
            rc.setName(collection);
            rc.setDescription("(from dropbox)");
            object.getSharedCollections().add(rc);
        }
        StringWriter writer = new StringWriter();
        marshaller.marshal(object, writer);
        return writer.toString();

    }

    /* (non-Javadoc)
     * @see org.tdar.balk.service.ItemService#itemStatusReport(java.lang.String, int, int, java.util.TreeMap, boolean)
     */
    @Override
    @Transactional(readOnly = true)
    public int itemStatusReport(String path, int page, int size, TreeMap<String, WorkflowStatusReport> map, boolean managed) {
        List<DropboxFile> findAll = new ArrayList<>();
        int total = itemDao.findAllWithPath(path, findAll, page, size, managed);
        for (DropboxFile file : findAll) {
            String key = Phases.createKey(file);
            logger.trace("{} --> {}", file.getPath(), key);
            map.putIfAbsent(key, new WorkflowStatusReport());
            WorkflowStatusReport status = map.get(key);
            if (status.getFirst() == null) {
                status.setFirst(file);
            }

            for (Phases phase : Phases.values()) {
                phase.updateStatus(status, file);
            }
            logger.trace(" {} [{}]", status.getCurrentPhase(), file.getTdarReferences());
        }
        return total;
    }

    /* (non-Javadoc)
     * @see org.tdar.balk.service.ItemService#findByDropboxId(java.lang.String, boolean)
     */
    @Override
    public AbstractDropboxItem findByDropboxId(String id, boolean dir) {
        return itemDao.findByDropboxId(id, dir);
    }

    /* (non-Javadoc)
     * @see org.tdar.balk.service.ItemService#move(org.tdar.balk.bean.AbstractDropboxItem, org.tdar.balk.service.Phases, org.tdar.balk.bean.DropboxUserMapping, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
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

    /* (non-Javadoc)
     * @see org.tdar.balk.service.ItemService#copy(org.tdar.balk.bean.AbstractDropboxItem, java.lang.String, org.tdar.balk.bean.DropboxUserMapping, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
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

    /* (non-Javadoc)
     * @see org.tdar.balk.service.ItemService#listChildPaths(java.lang.String)
     */
    @Override
    @Transactional(readOnly = false)
    public Set<String> listChildPaths(String path) {
        return itemDao.findTopLevelPaths(path);
    }

    /* (non-Javadoc)
     * @see org.tdar.balk.service.ItemService#listTopLevelPaths()
     */
    @Override
    @Transactional(readOnly = true)
    public Set<String> listTopLevelPaths() {
        return itemDao.findTopLevelPaths(config.getBaseDropboxPath().replace("/", ""));
    }

    /* (non-Javadoc)
     * @see org.tdar.balk.service.ItemService#listTopLevelManagedPaths()
     */
    @Override
    @Transactional(readOnly = true)
    public Set<String> listTopLevelManagedPaths() {
        return itemDao.findTopLevelManagedPaths();
    }

}
