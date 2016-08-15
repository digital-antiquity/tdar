package org.tdar.utils.dropbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.UrlService;
import org.tdar.utils.APIClient;
import org.tdar.utils.dropbox.container.AbstractContainer;
import org.tdar.utils.dropbox.container.FileContainer;
import org.tdar.utils.dropbox.container.FolderContainer;

public class TdarUploadListener implements MetadataListener {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    File rootDir = new File("Client Data", "Upload to tDAR");
    FolderContainer root = null;
    private Map<String, AbstractContainer> map = new HashMap<>();
    private APIClient apiClient;
    boolean loggedIn = false;
    private Boolean debug;

    public TdarUploadListener(Boolean debug) throws FileNotFoundException, URISyntaxException, IOException {
        this.debug = debug;
        apiClient = new APIClient();
    }

    @Override
    public void consume(DropboxItemWrapper fileWrapper) throws Exception {
        logger.debug(fileWrapper.getFullPath());
        // TODO Auto-generated method stub
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
        if (fileWrapper.isDir()) {
            getMap().put(fileWrapper.getId(), new FolderContainer(fileWrapper));
        } else {
            File file = fileWrapper.getFile();

            String docXml = makeXml(file, fileWrapper.getExtension());
            logger.debug(docXml);
            if (debug == false) {
                apiClient.uploadRecordWithDefaultAccount(docXml, null, file);
            }
            getMap().put(fileWrapper.getId(), new FileContainer(fileWrapper));
        }

    }

    private String makeXml(File file, String extension) throws JAXBException, InstantiationException, IllegalAccessException {
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
        }

        JAXBContext jc = JAXBContext.newInstance(cls);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, UrlService.getProductionSchemaUrl());
        InformationResource object = (InformationResource) cls.newInstance();
        object.setTitle(file.getName());
        object.setDescription(file.getName());
        object.setStatus(Status.DRAFT);
        object.setDate(2016);
        StringWriter writer = new StringWriter();
        marshaller.marshal(object, writer);
        return writer.toString();

    }

    public Map<String, AbstractContainer> getMap() {
        return map;
    }

    public void setMap(Map<String, AbstractContainer> map) {
        this.map = map;
    }

}
