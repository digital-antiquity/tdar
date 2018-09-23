package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.fileprocessing.workflows.AudioWorkflow;
import org.tdar.fileprocessing.workflows.FileArchiveWorkflow;
import org.tdar.fileprocessing.workflows.GenericColumnarDataWorkflow;
import org.tdar.fileprocessing.workflows.GenericDocumentWorkflow;
import org.tdar.fileprocessing.workflows.GenericOntologyWorkflow;
import org.tdar.fileprocessing.workflows.GeospatialWorkflow;
import org.tdar.fileprocessing.workflows.ImageWorkflow;
import org.tdar.fileprocessing.workflows.RequiredOptionalPairs;
import org.tdar.fileprocessing.workflows.VideoWorkflow;

public class TdarFileReconcilerTest {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private List<RequiredOptionalPairs> setup() {
        List<RequiredOptionalPairs> pairs = new ArrayList<>();
        pairs.addAll(new GeospatialWorkflow().getRequiredOptionalPairs());
        pairs.addAll(new ImageWorkflow().getRequiredOptionalPairs());
        pairs.addAll(new GenericDocumentWorkflow().getRequiredOptionalPairs());
        pairs.addAll(new GenericColumnarDataWorkflow().getRequiredOptionalPairs());
        pairs.addAll(new AudioWorkflow().getRequiredOptionalPairs());
        pairs.addAll(new VideoWorkflow().getRequiredOptionalPairs());
        pairs.addAll(new GenericOntologyWorkflow().getRequiredOptionalPairs());
        pairs.addAll(new FileArchiveWorkflow().getRequiredOptionalPairs());
        return pairs;
    }
    
    @Test(expected=TdarRecoverableRuntimeException.class)
    public void testDuplicateFilenameInList() {
        List<TdarFile> files = new ArrayList<>();
        files.add(new TdarFile("test.shp", null,null));
        files.add(new TdarFile("test.dbf", null,null));
        files.add(new TdarFile("test.idx", null,null));
        files.add(new TdarFile("test.idx", null,null));
        files.add(new TdarFile("test.doc", null,null));
        TdarFileReconciller tfr = new TdarFileReconciller(setup());
        
        tfr.reconcile(files);
    }
    
    @Test
    public void testShapefile() {
        List<TdarFile> files = new ArrayList<>();
        TdarFile img = new TdarFile("test.jpg", null,null);
        files.add(img);
        files.add(new TdarFile("test.shx", null,null));
        TdarFile shp = new TdarFile("test.shp", null,null);
        files.add(shp);
        files.add(new TdarFile("test.dbf", null,null));
        files.add(new TdarFile("test.idx", null,null));
        TdarFile doc = new TdarFile("test.doc", null,null);
        files.add(doc);
        TdarFileReconciller tfr = new TdarFileReconciller(setup());
        
        Map<TdarFile, RequiredOptionalPairs> reconcile = tfr.reconcile(files);
        assertEquals("should have 3 keys", 3, reconcile.keySet().size());
        assertTrue("shapefile should be in the key", reconcile.containsKey(shp));
        assertTrue("doc should be in the key", reconcile.containsKey(doc));
        assertTrue("image should be in the key", reconcile.containsKey(img));
        
    }

}
