package org.tdar.struts.action.geospatial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.struts.action.dataset.AbstractDatasetController;
import org.tdar.struts.action.resource.RequiredOptionalPairs;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;

/**
 * $Id$
 * 
 * <p>
 * Manages requests to create/delete/edit an Image and its associated metadata.
 * </p>
 * 
 * 
 * @author <a href='mailto:Adam.Brin@asu.edu'>Adam Brin</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/geospatial")
@Result(name = TdarActionSupport.INPUT, location = "edit.ftl")
public class GeospatialController extends AbstractDatasetController<Geospatial> {

    private static final long serialVersionUID = 6576781526708737335L;

    @Override
    public Class<Geospatial> getPersistableClass() {
        return Geospatial.class;
    }

    public Geospatial getGeospatial() {
        return getPersistable();
    }

    public void setGeospatial(Geospatial dataset) {
        setPersistable(dataset);
    }

    @Override
    public Set<String> getValidFileExtensions() {
        Set<String> extensionsForTypes = getAnalyzer().getExtensionsForTypes(getPersistable().getResourceType(), ResourceType.DATASET, ResourceType.IMAGE);
        // FIXME: these should come from the analyzer
        // Note: aux.xml and shp.xml omitted because we know view layer logic will accept any .xml (so will server, for that matter)
        String[] geoexts = { "shp", "shx", "dbf", "sbn", "sbx", "fbn", "fbx", "ain", "aih", "atx", "ixs", "mxs", "prj", "xml", "cpg", "jpw", "jgw", "tfw",
                "aux", "aux", "ovr", "rrd", "mxd", "lyr" }; // "adf",
        Collections.addAll(extensionsForTypes, geoexts);

        return extensionsForTypes;
    }

    @Override
    public Collection<RequiredOptionalPairs> getRequiredOptionalPairs() {
        List<RequiredOptionalPairs> pairs = new ArrayList<>();
        RequiredOptionalPairs shapefile = new RequiredOptionalPairs();
        shapefile.getRequired().add("shp");
        shapefile.getRequired().add("shx");
        shapefile.getRequired().add("dbf");
        shapefile.getOptional().add("sbn");
        shapefile.getOptional().add("sbx");
        shapefile.getOptional().add("fbn");
        shapefile.getOptional().add("fbx");
        shapefile.getOptional().add("ain");
        shapefile.getOptional().add("aih");
        shapefile.getOptional().add("atx");
        shapefile.getOptional().add("ixs");
        shapefile.getOptional().add("mxs");
        shapefile.getOptional().add("prj");
        shapefile.getOptional().add("cbg");
        shapefile.getOptional().add("ixs");
        shapefile.getOptional().add("rrd");
        pairs.add(shapefile);
        RequiredOptionalPairs layer = new RequiredOptionalPairs();
        layer.getRequired().add("lyr");
        layer.getRequired().add("jpg");
        layer.getOptional().add("mxd");
        pairs.add(layer);

        RequiredOptionalPairs geotiff = new RequiredOptionalPairs();
        geotiff.getRequired().add("tif");
        geotiff.getRequired().add("tiff");
        geotiff.getOptional().add("tfw");
        pairs.add(geotiff);
        RequiredOptionalPairs geojpg = new RequiredOptionalPairs();
        geojpg.getRequired().add("jpg");
        geojpg.getRequired().add("jpeg");
        geojpg.getOptional().add("jfw");
        pairs.add(geojpg);
        return pairs;
    }

    @Override
    public Geospatial getResource() {
        if (getPersistable() == null) {
            setPersistable(createPersistable());
        }
        return getPersistable();
    }

    @Override
    public boolean isMultipleFileUploadEnabled() {
        return true;
    }

    public Geospatial getDataset() {
        return getPersistable();
    }

    public void setDataset(Geospatial geospatial) {
        setGeospatial(geospatial);
    }
}
