package org.tdar.core.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.LanguageEnum;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.resource.ResourceDao;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.Filestore;
import org.tdar.struts.data.FileProxy;

/**
 * $Id: AbstractInformationResourceService.java 1466 2011-01-18 20:32:38Z abrin$
 * 
 * Provides basic InformationResource services including file management (via FileProxyS).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Service
public abstract class AbstractInformationResourceService<T extends InformationResource, R extends ResourceDao<T>> extends ServiceInterface.TypedDaoBase<T, R> {

    // FIXME: this should be injected
    private static final Filestore filestore = TdarConfiguration.getInstance().getFilestore();

    @Autowired
    private InformationResourceFileService informationResourceFileService;
    @Autowired
    private FileAnalyzer analyzer;
    @Autowired
    private GenericDao genericDao;

    @Transactional(readOnly = true)
    public List<T> findBySubmitter(Person person) {
        if (person == null) {
            getLogger().warn("Trying to find resources for null submitter");
            return Collections.emptyList();
        }
        return getDao().findBySubmitter(person);
    }

    @Transactional(readOnly = true)
    public List<T> findSparseBySubmitter(Person person) {
        if (person == null) {
            getLogger().warn("Trying to find resources for null submitter");
            return Collections.emptyList();
        }
        return getDao().findSparseResourceBySubmitterType(person, ResourceType.fromClass(getDao().getPersistentClass()));
    }

    @Transactional
    public void deleteInformationResourceFile(InformationResource resource, InformationResourceFile irFile) {
        resource.getInformationResourceFiles().remove(irFile);
        informationResourceFileService.delete(irFile);
    }

    private void addInformationResourceFile(InformationResource resource, InformationResourceFile irFile) {
        irFile.setInformationResource(resource);
        genericDao.saveOrUpdate(irFile);
        resource.add(irFile);
        genericDao.saveOrUpdate(resource);
    }
    
    private InformationResourceFile findInformationResourceFile(FileProxy proxy) {
        InformationResourceFile irFile = informationResourceFileService.find(proxy.getFileId());
        if (irFile == null) {
            logger.error("{} had no findable InformationResourceFile.id set on it", proxy);
            // FIXME: throw an exception?
        }
        return irFile;
    }

    @Transactional
    public void processFileProxy(InformationResource informationResource, FileProxy proxy) throws IOException {
        logger.debug("processing {} proxy {}", informationResource, proxy);
        // will be reassigned in a REPLACE or ADD_DERIVATIVE
        InformationResourceFile irFile = new InformationResourceFile();
        switch (proxy.getAction()) {
            case MODIFY_METADATA:
                irFile = findInformationResourceFile(proxy);
                if (irFile == null) {
                    return;
                }
                // set sequence number and confidentiality
                setInformationResourceFileMetadata(irFile, proxy);
                genericDao.update(irFile);
                break;
            case REPLACE:
                irFile = findInformationResourceFile(proxy);
                if (irFile == null) {
                    return;
                }
                // explicit fall through to ADD after loading the existing irFile to
                // be replaced.
            case ADD:
                // always set the download/version info and persist the relationships between the InformationResource and its IRFile.
                incrementVersionNumber(irFile);
                addInformationResourceFile(informationResource, irFile);
                createVersion(irFile, proxy);
                for (FileProxy additionalVersion : proxy.getAdditionalVersions()) {
                    logger.debug("Creating new version {}", additionalVersion);
                    createVersion(irFile, additionalVersion);
                }
                genericDao.saveOrUpdate(irFile);
                logger.debug("all versions for {}", irFile);
                break;
            case ADD_DERIVATIVE:
                irFile = findInformationResourceFile(proxy);
                if (irFile == null) {
                    return;
                }
                createVersion(irFile, proxy);
                break;
            case DELETE:
                // handle deletion
                irFile = findInformationResourceFile(proxy);
                if (irFile == null) {
                    logger.error("FileProxy {} DELETE had no InformationResourceFile.id ({}) set on it", proxy.getFilename(), proxy.getFileId());
                    return;
                }
                irFile.delete();
                genericDao.update(irFile);
                break;
            case NONE:
                logger.debug("Taking no action on {} with proxy {}", informationResource, proxy);
                return;
            default:
                return;
        }
    }
    
    private void setInformationResourceFileMetadata(InformationResourceFile irFile, FileProxy fileProxy) {
        irFile.setConfidential(fileProxy.isConfidential());
        Integer sequenceNumber = fileProxy.getSequenceNumber();
        if (sequenceNumber == null) {
            logger.warn("No sequence number set on file proxy {}, existing sequence number was {}", fileProxy, irFile.getSequenceNumber());
        }
        else {
            irFile.setSequenceNumber(sequenceNumber);
        }
    }

    private void incrementVersionNumber(InformationResourceFile irFile) {
        irFile.incrementVersionNumber();
        irFile.setDownloadCount(0);
        irFile.clearStatus();
        logger.info("incremented version number and reset download and status for irfile: {}", irFile, irFile.getLatestVersion());
    }

    private void createVersion(InformationResourceFile irFile, FileProxy fileProxy) throws IOException {
        String filename = sanitizeFilename(fileProxy.getFilename());
        InformationResourceFileVersion version = new InformationResourceFileVersion(fileProxy.getVersionType(), filename, irFile);
        setInformationResourceFileMetadata(irFile, fileProxy);
        irFile.addFileVersion(version);
        filestore.store(fileProxy.getInputStream(), version);
        genericDao.save(version);
        switch (fileProxy.getVersionType()) {
            case UPLOADED:
            case UPLOADED_ARCHIVAL:
                irFile.setInformationResourceFileType(analyzer.analyzeFile(version));
                try {
                    analyzer.processFile(version);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warn("caught exception {} while analyzing file {}", e, filename);
                }
                break;
            default:
                logger.debug("Not setting file type on irFile {} for VersionType {}", irFile, fileProxy.getVersionType());
        }
        genericDao.saveOrUpdate(irFile);
    }

    private String sanitizeFilename(String filename) {
        filename = filename.toLowerCase();
        String ext = FilenameUtils.getExtension(filename);
        String basename = FilenameUtils.getBaseName(filename);

        // make sure that the total length does not exceed 128 characters
        if (basename.length() > 122)
            basename = basename.substring(0, 121);

        // replace all whitespace with dashes
        // basename = basename.replaceAll("\\s", "-");

        // replace all characters that are not alphanumeric, underscore "_", or
        // dash "-" with a single dash "-".
        basename = basename.replaceAll("[^\\w\\-]+", "-");

        basename = StringUtils.removeEnd(basename, "-");

        StringBuilder builder = new StringBuilder(basename);
        // ensure that the first letter of the basename is alphabetic
        if (!StringUtils.isAlpha(String.valueOf(basename.charAt(0)))) {
            builder.insert(0, 'a');
        }
        builder.append('.').append(ext);

        return builder.toString();
    }

    public InformationResourceFileService getInformationResourceFileService() {
        return informationResourceFileService;
    }

    public List<LanguageEnum> findAllLanguages() {
        return Arrays.asList(LanguageEnum.values());
    }

}
