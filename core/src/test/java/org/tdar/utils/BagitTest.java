/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;
import org.tdar.core.configuration.TdarConfiguration;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.Manifest.Algorithm;
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
 * @author Adam Brin
 * 
 */
public class BagitTest {

    private BagFactory bagFactory = new BagFactory();
    private final transient Logger logger = LoggerFactory.getLogger(getClass());


    private static final String PATH = TestConstants.TEST_ROOT_DIR;

    private TdarConfiguration cfg = TdarConfiguration.getInstance();
    private File bagHome;

    @Before
    public void createBagHome() throws Exception {
        bagHome = new File(cfg.getPersonalFileStoreLocation() + "/testbag");
        Assert.assertTrue(bagHome.mkdirs());
    }

    // FIXME: this fails to delete the directory on windows for some reason. Something else holding on to file?
    @After
    public void deleteBagHome() throws Exception {
        if (bagHome.exists()) {
            FileUtils.deleteQuietly(bagHome);
        }
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

    private Bag getBag(File sourceFile) {
        return getBag(sourceFile, Version.V0_96, null);
    }

    @Test
    public void test() {

        DefaultCompleter completer = new DefaultCompleter(bagFactory);
        completer.setGenerateBagInfoTxt(true);
        completer.setUpdateBaggingDate(false);
        completer.setUpdateBagSize(false);
        completer.setUpdatePayloadOxum(false);
        completer.setGenerateTagManifest(false);
        completer.setTagManifestAlgorithm(Algorithm.MD5);
        completer.setPayloadManifestAlgorithm(Algorithm.MD5);
        completer.setNonDefaultManifestSeparator("\t");

        List<File> filesToAdd = new ArrayList<File>();
        filesToAdd.add(new File(PATH + "images/handbook_of_archaeology.jpg"));
        filesToAdd.add(new File(PATH + "data_integration_tests/evmpp-fauna.xls"));
        filesToAdd.add(new File(PATH + "xml/documentImport.xml"));

        Bag bag = this.getBag(bagHome);
        for (File fileToAdd : filesToAdd) {
            Assert.assertTrue("file exists", fileToAdd.exists());
            bag.addFileToPayload(fileToAdd);
        }

        Writer writer = new FileSystemWriter(bagFactory);
        Bag newBag = completer.complete(bag);
        newBag.write(writer, bagHome);
        Assert.assertTrue(true);

        CompleteVerifier completeVerifier = new CompleteVerifierImpl();
        // completeVerifier.setMissingBagItTolerant(false);
        // completeVerifier.setAdditionalDirectoriesInBagDirTolerant(false);
        ValidVerifier verifier = new ValidVerifierImpl(completeVerifier, new ParallelManifestChecksumVerifier());

        // assert the bag is valid (baginfo.txt matches contents) then iterate over the bag contents.
        Bag bagOut = this.getBag(bagHome, Version.V0_96, LoadOption.BY_MANIFESTS);
        SimpleResult result = verifier.verify(bagOut);
        Assert.assertTrue("Bag contents valid", result.isSuccess());
        logger.info("mesages;{}", result.getMessages());
        for (BagFile bf : bagOut.getPayload()) {
            logger.info(bf.getFilepath() + " - " + bf.getSize());

        }
    }

}
