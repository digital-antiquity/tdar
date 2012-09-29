package org.tdar.filestore;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

@Component
public class BagitPersonalFilestore implements PersonalFilestore {

    // FIXME: tight coupling. inject instead
    private final static TdarConfiguration cfg = TdarConfiguration.getInstance();

    // private File bagHome;
    private BagFactory bagFactory = new BagFactory();
    private Completer completer;
    private CompleteVerifier completeVerifier;
    private ValidVerifier verifier;

    private Logger logger = Logger.getLogger(getClass());

    public BagitPersonalFilestore() {
        // bagHome = new File(cfg.getPersonalFileStoreLocation());

        // FIXME: Compare with what the defaults are and remove default defaults
        DefaultCompleter defaultCompleter = new DefaultCompleter(bagFactory);
        defaultCompleter.setGenerateBagInfoTxt(true);
        defaultCompleter.setUpdateBaggingDate(false);
        defaultCompleter.setUpdateBagSize(false);
        defaultCompleter.setUpdatePayloadOxum(false);
        defaultCompleter.setGenerateTagManifest(false);
        defaultCompleter.setTagManifestAlgorithm(Algorithm.MD5);
        defaultCompleter.setPayloadManifestAlgorithm(Algorithm.MD5);
        defaultCompleter.setNonDefaultManifestSeparator("\t");
        completer = defaultCompleter;

        completeVerifier = new CompleteVerifierImpl();
        completeVerifier.setMissingBagItTolerant(false);
        completeVerifier.setAdditionalDirectoriesInBagDirTolerant(false);
        verifier = new ValidVerifierImpl(completeVerifier, new ParallelManifestChecksumVerifier());
    }

    private String getPath(PersonalFilestoreTicket tfg) {
        return String.format("%s/%s/%d", getStoreLocation(tfg.getSubmitter()), tfg.getPersonalFileType().getPathname(), tfg.getId());
    }

    private String getPath(Person person, PersonalFileType type) {
        return String.format("%s/%s", getStoreLocation(person), type.getPathname());
    }

    private Bag getBag(File sourceFile, Version version, LoadOption loadOption) {
        if (version != null) {
            if (sourceFile != null) {
                return bagFactory.createBag(sourceFile, version, loadOption);
            } else {
                return bagFactory.createBag(version);
            }
        } else {
            if (sourceFile != null) {
                return bagFactory.createBag(sourceFile, loadOption);
            } else {
                return bagFactory.createBag();
            }
        }
    }

    // return a bag appropriate for writing
    private Bag getBag(File sourceFile) {
        return getBag(sourceFile, Version.V0_96, null);
    }

    @Override
    public String getStoreLocation(Person person) {
        return cfg.getPersonalFileStoreLocation() + "/" + person.getId();
    }

    @Override
    public synchronized File store(PersonalFilestoreTicket personalFilestoreTicket, File file, String incomingFileName) throws IOException {
        String pathToBag = getPath(personalFilestoreTicket);
        File pathToBagFile = new File(pathToBag);

        // need to give the file it's correct name, but not safe to do so until we move it out of the temp directory
        File tempFileToStore = new File(pathToBagFile.getParentFile(), incomingFileName);
        FileUtils.copyFile(file, tempFileToStore);

        FileUtils.forceMkdir(pathToBagFile);
        Bag bag = getBag(pathToBagFile);
        bag.addFileToPayload(tempFileToStore);

        Writer writer = new FileSystemWriter(bagFactory);
        Bag newBag = completer.complete(bag);
        newBag.write(writer, pathToBagFile);
        FileUtils.deleteQuietly(tempFileToStore);
        for (BagFile storedBagFile : newBag.getPayload()) {
            // BagFile makes the assumption of unix filesystem paths
            if (storedBagFile.getFilepath().endsWith("/" + incomingFileName)) {
                return new File(pathToBag, storedBagFile.getFilepath());
            }
        }
        throw new TdarRecoverableRuntimeException("could not find the file we just filed:" + incomingFileName);
    }

    @Override
    public void store(PersonalFilestoreTicket ticket, List<File> files, List<String> newFileNames) throws IOException {
        // TODO: we could make this more effiient by using Bag.addFilesToPayload
        for (int i = 0; i < files.size(); i++) {
            store(ticket, files.get(i), newFileNames.get(i));
        }
    }

    @Override
    public List<PersonalFilestoreFile> retrieveAll(PersonalFilestoreTicket tempFileGroup) {
        logger.trace("retrieveAll Called");
        List<PersonalFilestoreFile> filestoreFiles = new ArrayList<PersonalFilestoreFile>();
        String pathToBag = getPath(tempFileGroup);
        File bagFile = new File(pathToBag);

        // retrieve the files, tack on the md5, and then return
        // assert the bag is valid (baginfo.txt matches contents) then iterate over the bag contents.
        Bag bagOut = this.getBag(bagFile, Version.V0_96, LoadOption.BY_PAYLOAD_MANIFESTS);

        SimpleResult result = verifier.verify(bagOut);
        logger.trace(result.getMessages());
        for (BagFile bf : bagOut.getPayload()) {
            PersonalFilestoreFile pff = new PersonalFilestoreFile();
            pff.setFile(new File(pathToBag, bf.getFilepath()));
            Map<Algorithm, String> map = bagOut.getChecksums(bf.getFilepath());
            pff.setMd5(map.get(Algorithm.MD5));
            filestoreFiles.add(pff);
            logger.debug("retrieving " + bf.getFilepath() + " - " + bf.getSize());
        }

        return filestoreFiles;
    }

    @Override
    public void purge(PersonalFilestoreTicket ticket) {
        File directory = new File(getPath(ticket));
        try {
            FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            logger.error("Couldn't delete directory at " + directory, e);
        }
    }

    @Override
    public void purge(Person person, PersonalFileType personalFileType) {
        File directory = new File(getPath(person, personalFileType));
        try {
            FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            logger.error("Couldn't delete directory at " + directory, e);
        }
    }
}
