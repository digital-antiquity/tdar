package org.tdar.utils.dropbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.BasicAccount;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class DropboxItemWrapper {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private DropboxClient client;
    private Metadata metadata;
    private String id;
    private boolean dir = false;
    private String name;
    private String fullPath;
    private File path;
    private Integer size;
    private Date modified;
    private String modifiedBy;
    private String modifiedId;

    private String extension;

    /*
     * path_display -> /Client Data/Upload to tDAR/srp digital library/Batch 1/1985_Effland_SaltRiver_OCR_PDFA.pdf
     * rev -> 574c829b34
     * size -> 23765842
     * server_modified -> 2016-08-13T19:44:52Z
     * path_lower -> /client data/upload to tdar/srp digital library/batch 1/1985_effland_saltriver_ocr_pdfa.pdf
     * name -> 1985_Effland_SaltRiver_OCR_PDFA.pdf
     * .tag -> file
     * sharing_info -> {read_only=false, parent_shared_folder_id=1283627828, modified_by=dbid:AADJ0bQGtwHBVqNu-oTqVKN_8XBQn3Wx8n4}
     * id -> id:0F-qZgN3hVAAAAAAAAAAFw
     * client_modified -> 2016-08-09T21:45:51Z
     * parent_shared_folder_id -> 1283627828
     * id:id:0F-qZgN3hVAAAAAAAAAAFw modifiedBy:dbid:AADJ0bQGtwHBVqNu-oTqVKN_8XBQn3Wx8n4
     * 
     */
    public DropboxItemWrapper(DropboxClient client, Metadata metadata) {
        this.client = client;
        this.setMetadata(metadata);
        try {
            ObjectReader reader = new ObjectMapper().readerFor(HashMap.class);
            HashMap<String, Object> readValue = new HashMap<>();
            readValue = reader.readValue(metadata.toString());
            logger.trace(metadata.getPathLower());
            if (logger.isTraceEnabled()) {
                readValue.entrySet().forEach(es -> {
                    logger.trace("\t {} -> {} ", es.getKey(), es.getValue());
                });
            }
            this.size = (Integer) readValue.get("size");
            this.dir = !StringUtils.equals("file", (String) readValue.get(".tag"));
            this.id = (String) readValue.get("id");
            this.name = (String) readValue.get("name");
            fullPath = (String) readValue.get("path_display");
            this.path = new File(fullPath).getParentFile();
            String mod = (String) readValue.get("server_modified");
            
            if (!dir) {
                extension = FilenameUtils.getExtension(name);
            }
            if (mod != null) {
                this.modified = DateTime.parse(mod).toDate();
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) readValue.get("sharing_info");
            if (map != null && map.get("modified_by") != null) {
                String accountId = (String) map.get("modified_by");
                setModifiedId(accountId);
                if (accountId != null) {
                    BasicAccount account = client.getAccount(accountId);
                    modifiedBy = account.getName().getDisplayName();
                }
            }
        } catch (Exception e) {
            logger.error("{}", e, e);
        }
    }

    public File getFile() throws DownloadErrorException, FileNotFoundException, DbxException, IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"),"dropbox");
        tempDir.mkdirs();
        File file = new File(tempDir, getName());
        client.getFile(fullPath, new FileOutputStream(file));
        return file;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDir() {
        return dir;
    }

    public boolean isFile() {
        return !dir;
    }

    public void setDir(boolean dir) {
        this.dir = dir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public String getModifiedId() {
        return modifiedId;
    }

    public void setModifiedId(String modifiedId) {
        this.modifiedId = modifiedId;
    }

}
