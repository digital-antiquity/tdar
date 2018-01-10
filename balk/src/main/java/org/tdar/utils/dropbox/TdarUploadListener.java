package org.tdar.utils.dropbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.UrlService;
import org.tdar.utils.APIClient;
import org.tdar.utils.ApiClientResponse;

import org.tdar.balk.service.ItemService;

public class TdarUploadListener implements MetadataListener {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    File rootDir = new File("Client Data", "Upload to tDAR");
    private APIClient apiClient;
    boolean loggedIn = false;
    private Boolean debug;
    ItemService itemService;

    public TdarUploadListener(ItemService itemService) throws FileNotFoundException, URISyntaxException, IOException {
        this.itemService = itemService;
        apiClient = new APIClient();
    }

    @Override
    public void consume(DropboxItemWrapper fileWrapper) throws Exception {
        logger.debug(fileWrapper.getFullPath());
        if (itemService.hasUploaded(fileWrapper.getId(), fileWrapper.isDir())) {
            return;
        }
        File path = fileWrapper.getPath();
        List<String> tree = new ArrayList<>();
        while (!StringUtils.equalsIgnoreCase(path.getName(), rootDir.getName()) &&
                StringUtils.contains(path.getPath(), rootDir.getPath())) {
            tree.add(0, path.getName());
            path = path.getParentFile();
            if (path == null || path.getName().equals("")) {
                break;
            }
        }
        if (!loggedIn && debug == false) {
            apiClient.apiLogin();
        }
        if (fileWrapper.isFile()) {
            File file = null;
            String docXml = makeXml(file, fileWrapper.getName(), fileWrapper.getExtension(), StringUtils.join(tree, "/"));
            logger.trace(docXml);
            if (docXml == null) {
                return;
            }
            if (debug == false) {
                logger.debug("uploading: {}", file);
                ApiClientResponse response = apiClient.uploadRecordWithDefaultAccount(docXml, null, file);
                itemService.markUploaded(fileWrapper.getId(), response.getTdarId() , fileWrapper.isDir());
            }
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
        SharedCollection rc = new SharedCollection();
        rc.setHidden(true);
        rc.setName(collection);
        rc.setDescription("(from dropbox)");
        object.getSharedResourceCollections().add(rc);
        StringWriter writer = new StringWriter();
        marshaller.marshal(object, writer);
        return writer.toString();

    }

    @Override
    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

}
