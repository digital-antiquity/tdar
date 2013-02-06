package org.tdar.core.service.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Language;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.resource.ResourceDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ServiceInterface;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.ExceptionWrapper;

/**
 * $Id: AbstractInformationResourceService.java 1466 2011-01-18 20:32:38Z abrin$
 * 
 * Provides basic InformationResource services including file management (via FileProxyS).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

public abstract class AbstractInformationResourceService<T extends InformationResource, R extends ResourceDao<T>> extends ServiceInterface.TypedDaoBase<T, R> {

    // FIXME: this should be injected
    private static final Filestore filestore = TdarConfiguration.getInstance().getFilestore();

    @Autowired
    private InformationResourceFileService informationResourceFileService;
    @Autowired
    private FileAnalyzer analyzer;
    @Autowired
    @Qualifier("genericDao")
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
        genericDao.saveOrUpdate(resource);
        irFile.setInformationResource(resource);
        genericDao.saveOrUpdate(irFile);
        resource.add(irFile);
        genericDao.saveOrUpdate(resource);
    }

    private InformationResourceFile findInformationResourceFile(FileProxy proxy) {
        InformationResourceFile irFile = genericDao.find(InformationResourceFile.class, proxy.getFileId());
        if (irFile == null) {
            logger.error("{} had no findable InformationResourceFile.id set on it", proxy);
            // FIXME: throw an exception?
        }
        return irFile;
    }

    @Transactional
    public List<ExceptionWrapper> processFileProxies(PersonalFilestore filestore, T resource, List<FileProxy> fileProxiesToProcess,
            List<InformationResourceFile> modifiedFiles, Long ticketId) {
        List<ExceptionWrapper> exceptionsAndMessages = new ArrayList<ExceptionWrapper>();
        for (FileProxy fileProxy : fileProxiesToProcess) {
            try {
                InformationResourceFile file = processFileProxy(resource, fileProxy);
                if (file != null) {
                    modifiedFiles.add(file);
                    if (file.getWorkflowContext() != null) {
                        List<ExceptionWrapper> exceptions = file.getWorkflowContext().getExceptions();
                        logger.info("EXCEPTIONS: {}", exceptions);
                        exceptionsAndMessages.addAll(exceptions);
                    }
                }
            } catch (IOException exception) {
                exceptionsAndMessages
                        .add(new ExceptionWrapper("Unable to process file " + fileProxy.getFilename(), ExceptionUtils.getFullStackTrace(exception)));
            }
        }
        if (ticketId != null) {
            filestore.purge(getDao().find(PersonalFilestoreTicket.class, ticketId));

        }
        return exceptionsAndMessages;
    }

    @Transactional
    public InformationResourceFile processFileProxy(InformationResource informationResource, FileProxy proxy) throws IOException {
        logger.debug("applying {} to {}", proxy, informationResource);
        // will be reassigned in a REPLACE or ADD_DERIVATIVE
        InformationResourceFile irFile = new InformationResourceFile();
        switch (proxy.getAction()) {
            case MODIFY_METADATA:
                irFile = findInformationResourceFile(proxy);
                if (irFile == null) {
                    return null;
                }
                // set sequence number and confidentiality
                setInformationResourceFileMetadata(irFile, proxy);
                genericDao.update(irFile);
                break;
            case REPLACE:
                irFile = findInformationResourceFile(proxy);
                if (irFile == null) {
                    return null;
                }
                // explicit fall through to ADD after loading the existing irFile to
                // be replaced.
            case ADD:
                // always set the download/version info and persist the relationships between the InformationResource and its IRFile.
                incrementVersionNumber(irFile);
                addInformationResourceFile(informationResource, irFile);
                createVersion(irFile, proxy);
                setInformationResourceFileMetadata(irFile, proxy);
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
                    return null;
                }
                createVersion(irFile, proxy);
                break;
            case DELETE:
                // handle deletion
                irFile = findInformationResourceFile(proxy);
                if (irFile == null) {
                    logger.error("FileProxy {} DELETE had no InformationResourceFile.id ({}) set on it", proxy.getFilename(), proxy.getFileId());
                    return null;
                }
                irFile.delete();
                genericDao.update(irFile);
                break;
            case NONE:
                logger.debug("Taking no action on {} with proxy {}", informationResource, proxy);
                return null;
            default:
                return null;
        }
        return irFile;
    }

    private void setInformationResourceFileMetadata(InformationResourceFile irFile, FileProxy fileProxy) {
        irFile.setRestriction(fileProxy.getRestriction());
        Integer sequenceNumber = fileProxy.getSequenceNumber();
        if (fileProxy.getRestriction() == FileAccessRestriction.EMBARGOED) {
            if (irFile.getDateMadePublic() == null) {
                Calendar calendar = Calendar.getInstance();
                // set date made public to 5 years now.
                calendar.add(Calendar.YEAR, TdarConfiguration.getInstance().getEmbargoPeriod());
                irFile.setDateMadePublic(calendar.getTime());
            }
        } else {
            irFile.setDateMadePublic(null);
        }
        if (sequenceNumber == null) {
            logger.warn("No sequence number set on file proxy {}, existing sequence number was {}", fileProxy, irFile.getSequenceNumber());
        }
        else {
            irFile.setSequenceNumber(sequenceNumber);
        }
    }

    private void incrementVersionNumber(InformationResourceFile irFile) {
        irFile.incrementVersionNumber();
        irFile.clearStatus();
        logger.info("incremented version number and reset download and status for irfile: {}", irFile, irFile.getLatestVersion());
    }

    @Transactional(readOnly = false)
    public void reprocessInformationResourceFiles(Collection<InformationResourceFile> informationResourceFiles) {
        Iterator<InformationResourceFile> fileIterator = informationResourceFiles.iterator();
        while (fileIterator.hasNext()) {
            InformationResourceFile irFile = fileIterator.next();
            InformationResourceFileVersion original = irFile.getLatestUploadedVersion();
            Iterator<InformationResourceFileVersion> iterator = irFile.getInformationResourceFileVersions().iterator();
            // List<InformationResourceFileVersion> toDelete = new ArrayList<InformationResourceFileVersion>();
            while (iterator.hasNext()) {
                InformationResourceFileVersion version = iterator.next();
                if (!version.equals(original) && !version.isUploaded() && !version.isArchival()) {
                    iterator.remove();
                    informationResourceFileService.delete(version);
                }
            }
            // this is a known case where we need to purge the session
            genericDao.synchronize();
            try {
                analyzer.processFile(original);
            } catch (Exception e) {
                logger.warn("caught exception {} while analyzing file {}", e, original.getFilename());
            }
        }

    }

    private void createVersion(InformationResourceFile irFile, FileProxy fileProxy) throws IOException {
        String filename = sanitizeFilename(fileProxy.getFilename());
        if (fileProxy.getFile() == null || !fileProxy.getFile().exists()) {
            throw new TdarRecoverableRuntimeException("something went wrong, file " + fileProxy.getFilename() + " does not exist");
        }
        InformationResourceFileVersion version = new InformationResourceFileVersion(fileProxy.getVersionType(), filename, irFile);
//        setInformationResourceFileMetadata(irFile, fileProxy);
        irFile.addFileVersion(version);
        filestore.store(fileProxy.getFile(), version);
        genericDao.save(version);
        switch (fileProxy.getVersionType()) {
            case UPLOADED:
            case UPLOADED_ARCHIVAL:
                irFile.setInformationResourceFileType(analyzer.analyzeFile(version));
                try {
                    analyzer.processFile(version);
                } catch (Exception e) {
                    logger.warn("caught exception {} while analyzing file {}", e, filename);
                }
                break;
            default:
                logger.debug("Not setting file type on irFile {} for VersionType {}", irFile, fileProxy.getVersionType());
        }
        genericDao.saveOrUpdate(irFile);
    }

    /**
     * This comes from the bad old days and was intended to make dataset filenames safe for postgres importing.
     * Dataset files are converted into tables in postgres and this method
     * was used to generate table names that were postgres-safe, e.g., starts with an alphabetic character and < 128 characters.
     * Now, DatabaseConverter should be responsible for that translation / sanitization internally,
     * and we should preserve the filename as it was originally sent in as best we can.
     * 
     * FIXME: Filestore should be responsible for sanitization of filenames instead
     * 
     * @param filename
     * @return
     */
    private String sanitizeFilename(String filename) {
        filename = filename.toLowerCase();
        String ext = FilenameUtils.getExtension(filename);
        String basename = FilenameUtils.getBaseName(filename);

        // // make sure that the total length does not exceed 128 characters
        // if (basename.length() > 122)
        // basename = basename.substring(0, 121);

        // replace all whitespace with dashes
        // basename = basename.replaceAll("\\s", "-");

        // replace all characters that are not alphanumeric, underscore "_", or
        // dash "-" with a single dash "-".
        basename = basename.replaceAll("[^\\w\\-]+", "-");

        basename = StringUtils.removeEnd(basename, "-");

        StringBuilder builder = new StringBuilder(basename);

        // ensure that the first letter of the basename is alphabetic
        // if (!StringUtils.isAlpha(String.valueOf(basename.charAt(0)))) {
        // builder.insert(0, 'a');
        // }
        builder.append('.').append(ext);

        return builder.toString();
    }

    public InformationResourceFileService getInformationResourceFileService() {
        return informationResourceFileService;
    }

    public List<Language> findAllLanguages() {
        return Arrays.asList(Language.values());
    }

}
