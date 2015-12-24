package org.tdar.filestore.personal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.MessageHelper;

import com.google.common.io.Files;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.CompleteVerifier;
import gov.loc.repository.bagit.verify.ValidVerifier;
import gov.loc.repository.bagit.verify.impl.CompleteVerifierImpl;
import gov.loc.repository.bagit.verify.impl.ParallelManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.impl.ValidVerifierImpl;
import gov.loc.repository.bagit.writer.Writer;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;

/**
 * $Id$
 * 
 * 
 * @author Jim deVos
 * @version $Rev$
 */
@Component
public class BagitPersonalFilestore implements PersonalFilestore {

    private static final Version BAGIT_VERSION = Version.V0_96;

    // FIXME: tight coupling. inject instead
    private final static TdarConfiguration cfg = TdarConfiguration.getInstance();

    // private File bagHome;
    private BagFactory bagFactory = new BagFactory();
    private Completer completer;
    private CompleteVerifier completeVerifier;
    private ValidVerifier verifier;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public BagitPersonalFilestore() {
        // FIXME: Compare with what the defaults are and remove default defaults
        DefaultCompleter defaultCompleter = new DefaultCompleter(bagFactory);
        // defaultCompleter.setGenerateBagInfoTxt(true);
        defaultCompleter.setUpdateBaggingDate(false);
        defaultCompleter.setUpdateBagSize(false);
        defaultCompleter.setUpdatePayloadOxum(false);
        defaultCompleter.setGenerateTagManifest(false);
        defaultCompleter.setTagManifestAlgorithm(Algorithm.MD5);
        defaultCompleter.setPayloadManifestAlgorithm(Algorithm.MD5);
        defaultCompleter.setNonDefaultManifestSeparator("\t");
        completer = defaultCompleter;

        completeVerifier = new CompleteVerifierImpl();
        // completeVerifier.setMissingBagItTolerant(false);
        // completeVerifier.setAdditionalDirectoriesInBagDirTolerant(false);
        verifier = new ValidVerifierImpl(completeVerifier, new ParallelManifestChecksumVerifier());
    }

    private String getPath(PersonalFilestoreTicket ticket) {
        return String.format("%s/%s/%d", getStoreLocation(ticket.getSubmitter()), ticket.getPersonalFileType().getPathname(), ticket.getId());
    }

    private String getPath(Person person, PersonalFileType type) {
        return String.format("%s/%s", getStoreLocation(person), type.getPathname());
    }

    /**
     * Attempt to instantiate a bag, retrying if first attempt unsuccessful. This can apparently happen when manifest.txt.biltemp is present
     * at beginning of {@link BagFactory#createBag()} but is deleted before the method completes (likely from a previous call
     * to {@link Completer#complete(Bag)}).
     */

    private Bag attemptCreateBag(File sourceFile) {
        // give the OS 1 second to delete the tempfile. Then give up.
        int attempt = 0;
        final int maxAttempts = 20;
        final int waitPerAttempt = 50;
        Bag bag = null;
        RuntimeException lastException = null;
        while ((attempt < maxAttempts) && (bag == null)) {
            try {
                bag = bagFactory.createBag(sourceFile, BAGIT_VERSION, LoadOption.BY_MANIFESTS);
            } catch (RuntimeException rex) {
                try {
                    lastException = rex;
                    Thread.sleep(waitPerAttempt);
                } catch (InterruptedException e) {
                    logger.warn("attempt to wait for createBag failed", e);
                }
            }
            attempt++;
        }
        if (bag == null) {
            throw new RuntimeException(MessageHelper.getMessage("bagitPersonalFilestore.could_not_create_bag"), lastException);
        }
        if (attempt > 1) {
            // FIXME: lower to WARN once we are satisfied we've worked around this bug.
            logger.error("successfully called createBag() after {} failed attempts spanning {}ms", attempt - 1, (attempt - 1) * waitPerAttempt);
        }
        return bag;
    }

    @Override
    public String getStoreLocation(Person person) {
        return cfg.getPersonalFileStoreLocation() + "/" + person.getId();
    }

    @Override
    public synchronized PersonalFilestoreFile store(PersonalFilestoreTicket personalFilestoreTicket, File file, String incomingFileName) throws IOException {
        logger.debug("ticket:{}\t file:{}\t name:{}", new Object[] { personalFilestoreTicket, file, incomingFileName });
        String pathToBag = getPath(personalFilestoreTicket);
        File pathToBagFile = new File(pathToBag);

        // need to give the file it's correct name, but not safe to do so until we move it into a unique temp directory
        File tempFileDirectory = Files.createTempDir();
        String tempFileDirectoryName = FilenameUtils.getBaseName(tempFileDirectory.getAbsolutePath());
        // the path to the actual file payload that we're going to copy to from Struts' file upload directory
        File tempFileToStore = new File(tempFileDirectory, incomingFileName);
        FileUtils.copyFile(file, tempFileToStore);
        FileUtils.forceMkdir(pathToBagFile);
        Bag bag = attemptCreateBag(pathToBagFile);
        bag.addFileToPayload(tempFileDirectory);
        Writer writer = new FileSystemWriter(bagFactory);
        Bag newBag = completer.complete(bag);
        newBag.write(writer, pathToBagFile);

        FileUtils.deleteQuietly(tempFileDirectory);
        for (BagFile storedBagFile : newBag.getPayload()) {
            String filepath = storedBagFile.getFilepath();
            if (filepath.contains(tempFileDirectoryName)) {
                Map<Algorithm, String> checksums = newBag.getChecksums(filepath);
                return new PersonalFilestoreFile(new File(pathToBag, filepath), checksums.get(Algorithm.MD5));
            }
        }
        List<String> names = new ArrayList<>();
        names.add(incomingFileName);
        throw new TdarRecoverableRuntimeException("bagitPersonalFilestore.could_not_find_file_created", names);
    }

    @Override
    public synchronized void store(PersonalFilestoreTicket ticket, List<File> files, List<String> newFileNames) throws IOException {
        // TODO: we could make this more efficient by using Bag.addFilesToPayload
        for (int i = 0; i < files.size(); i++) {
            store(ticket, files.get(i), newFileNames.get(i));
        }
    }

    @Override
    public List<PersonalFilestoreFile> retrieveAll(PersonalFilestoreTicket ticket) {
        List<PersonalFilestoreFile> filestoreFiles = new ArrayList<PersonalFilestoreFile>();
        String pathToBag = getPath(ticket);
        File bagFile = new File(pathToBag);
        if (!bagFile.exists()) {
            return Collections.emptyList();
        }

        // retrieve the files, tack on the md5, and then return
        // assert the bag is valid (baginfo.txt matches contents) then iterate over the bag contents.
        Bag bagOut = bagFactory.createBag(bagFile, BAGIT_VERSION, LoadOption.BY_MANIFESTS);

        SimpleResult result = verifier.verify(bagOut);
        logger.trace("{}", result.getMessages());
        for (BagFile bf : bagOut.getPayload()) {
            PersonalFilestoreFile pff = new PersonalFilestoreFile();
            pff.setFile(new File(pathToBag, bf.getFilepath()));
            Map<Algorithm, String> map = bagOut.getChecksums(bf.getFilepath());
            pff.setMd5(map.get(Algorithm.MD5));
            filestoreFiles.add(pff);
            logger.debug("retrieving {} ({})", bf.getFilepath(), bf.getSize());
        }
        return filestoreFiles;
    }

    @Override
    public void purge(PersonalFilestoreTicket ticket) {
        purge(ticket, false);
    }

    private void purge(PersonalFilestoreTicket ticket, boolean quiet) {
        File directory = new File(getPath(ticket));
        if (!directory.exists()) {
            return;
        }
        try {
            FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            if (quiet) {
                logger.debug("QUIET: Couldn't delete directory at " + directory, e);
            } else {
                logger.error("Couldn't delete directory at " + directory, e);
            }
        }
    }

    @Override
    public void purgeQuietly(PersonalFilestoreTicket ticket) {
        purge(ticket, true);
    }

    @Override
    public void purge(Person person, PersonalFileType personalFileType) {
        File directory = new File(getPath(person, personalFileType));
        if (!directory.exists()) {
            return;
        }
        try {
            FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            logger.error("Couldn't delete directory at " + directory, e);
        }
    }

    @Override
    public PersonalFilestoreFile retrieve(PersonalFilestoreTicket ticket, String filename) {
        String pathToBag = getPath(ticket);
        File bagFile = new File(pathToBag);
        if (!bagFile.exists()) {
            return null;
        }
        // retrieve the files, tack on the md5, and then return
        // assert the bag is valid (baginfo.txt matches contents) then iterate over the bag contents.
        Bag bagOut = bagFactory.createBag(bagFile, BAGIT_VERSION, LoadOption.BY_MANIFESTS);

        SimpleResult result = verifier.verify(bagOut);
        logger.trace("{}", result.getMessages());
        for (BagFile bf : bagOut.getPayload()) {
            File file = new File(pathToBag, bf.getFilepath());
            if (!file.getName().equals(filename)) {
                continue;
            }
            PersonalFilestoreFile pff = new PersonalFilestoreFile();
            pff.setFile(file);
            Map<Algorithm, String> map = bagOut.getChecksums(bf.getFilepath());
            pff.setMd5(map.get(Algorithm.MD5));
            logger.debug("retrieving {} ({})", bf.getFilepath(), bf.getSize());
            return pff;
        }
        return null;
    }
}
