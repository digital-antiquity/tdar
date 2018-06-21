package org.tdar.core.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.FileType;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.service.workflow.workflows.AudioWorkflow;
import org.tdar.core.service.workflow.workflows.FileArchiveWorkflow;
import org.tdar.core.service.workflow.workflows.GenericColumnarDataWorkflow;
import org.tdar.core.service.workflow.workflows.GenericDocumentWorkflow;
import org.tdar.core.service.workflow.workflows.GenericOntologyWorkflow;
import org.tdar.core.service.workflow.workflows.ImageWorkflow;
import org.tdar.core.service.workflow.workflows.PDFWorkflow;
import org.tdar.core.service.workflow.workflows.VideoWorkflow;
import org.tdar.core.service.workflow.workflows.Workflow;
import org.tdar.filestore.RequiredOptionalPairs;

public class ResourceTypeExtensionWorkflowMap implements Serializable {

    private static final long serialVersionUID = 7491180919462550532L;
    Map<ResourceType, List<RequiredOptionalPairs>> pairs = new HashMap<>();

    public ResourceTypeExtensionWorkflowMap() {
        registerBasicPairs();
    }

    public Set<String> getExtensionsForResourceType(ResourceType rt) {
        List<RequiredOptionalPairs> list = pairs.get(rt);
        Set<String> extensions = new HashSet<>();
        for (RequiredOptionalPairs rop : list) {
            extensions.addAll(rop.getRequired());
            extensions.addAll(rop.getOptional());
        }
        return extensions;
    }

    public FileType getFileTypeForResourceType(ResourceType rt) {
        switch (rt) {
            case ARCHIVE:
                return FileType.FILE_ARCHIVE;
            case AUDIO:
                return FileType.AUDIO;
            case CODING_SHEET:
            case DATASET:
            case GEOSPATIAL:
                // FIXME: sometimes for geospatial
                return FileType.COLUMNAR_DATA;
            case DOCUMENT:
                return FileType.DOCUMENT;
            case IMAGE:
                return FileType.IMAGE;
            case VIDEO:
                return FileType.VIDEO;
            case ONTOLOGY:
            case PROJECT:
            case SENSORY_DATA:
            default:
                return FileType.OTHER;
        }
    }

    public void registerBasicPairs() {
        addRequired(ResourceType.ARCHIVE, FileArchiveWorkflow.class, Arrays.asList("zip", "tar", "bz2", "tgz"));
        addRequired(ResourceType.VIDEO, VideoWorkflow.class, Arrays.asList("mpg", "mpeg", "mp4", "mj2", "mjp2"));
        addRequired(ResourceType.AUDIO, AudioWorkflow.class, Arrays.asList("wav", "aif", "aiff", "flac", "bwf", "mp3"));
        addRequired(ResourceType.IMAGE, ImageWorkflow.class, Arrays.asList("gif", "tif", "jpg", "tiff", "jpeg"));
        addRequired(ResourceType.SENSORY_DATA, ImageWorkflow.class, Arrays.asList("gif", "tif", "jpg", "tiff", "jpeg"));
        addRequired(ResourceType.ONTOLOGY, GenericOntologyWorkflow.class, Arrays.asList("owl", "rdf"));
        addRequired(ResourceType.DOCUMENT, PDFWorkflow.class, Arrays.asList("rtf", "doc", "docx", "txt"));
        addRequired(ResourceType.DOCUMENT, GenericDocumentWorkflow.class, Arrays.asList("pdf"));
        addRequired(ResourceType.DATASET, GenericColumnarDataWorkflow.class, Arrays.asList("csv", "tab", "xls,'xlsx", "mdb", "accdb", "gdb"));
        addRequired(ResourceType.CODING_SHEET, GenericColumnarDataWorkflow.class,  Arrays.asList("csv", "tab", "xls,'xlsx", "merge"));

        List<RequiredOptionalPairs> shapePairs = new ArrayList<>();
        RequiredOptionalPairs shapefile = new RequiredOptionalPairs(GenericColumnarDataWorkflow.class);
        shapefile.getRequired().addAll(Arrays.asList(".shp", ".shx", ".dbf"));
        shapefile.getOptional().addAll(Arrays.asList(".sbn", ".sbx", ".fbn", ".fbx", ".ain", ".aih", ".atx", ".ixs", ".mxs", ".prj", ".cbg", ".ixs", ".rrd"));
        shapePairs.add(shapefile);
        RequiredOptionalPairs layer = new RequiredOptionalPairs();
        layer.getRequired().addAll(Arrays.asList(".lyr", ".jpg"));
        layer.getOptional().add(".mxd");
        shapePairs.add(layer);

        RequiredOptionalPairs geotiff = new RequiredOptionalPairs(ImageWorkflow.class);
        geotiff.getRequired().add(".tif");
        geotiff.getOptional().add(".tfw");
        shapePairs.add(geotiff);
        RequiredOptionalPairs geojpg = new RequiredOptionalPairs(ImageWorkflow.class);
        geojpg.getRequired().add(".jpg");
        geojpg.getOptional().add(".jfw");
        shapePairs.add(geojpg);
        pairs.put(ResourceType.GEOSPATIAL, shapePairs);

    }

    private void addRequired(ResourceType rt, Class<? extends Workflow> cls, List<String> asList) {
        for (String ext : asList) {
            RequiredOptionalPairs rop = new RequiredOptionalPairs(cls);
            rop.getRequired().add(ext);
            List<RequiredOptionalPairs> list = pairs.getOrDefault(rt, new ArrayList<>());
            list.add(rop);
            pairs.put(rt, list);
        }

    }

    public FileType getFileTypeForExtension(InformationResourceFileVersion version) {
        // TODO Auto-generated method stub
        return null;
    }

    public Workflow getWorkflow(ResourceType rt, String lowerCase) {
        // TODO Auto-generated method stub
        return null;
    }

}
