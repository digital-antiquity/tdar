package org.tdar.filestore.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.geotools.data.DataUtilities;
import org.geotools.factory.BasicFactories;
import org.geotools.factory.Hints;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.kml.KMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.Geometry;
import org.opengis.geometry.primitive.Point;
import org.opengis.geometry.primitive.PrimitiveFactory;
import org.opengis.referencing.operation.MathTransform;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.filestore.tasks.Task.AbstractTask;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Polygon;


public class GisFileReaderTask extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 8419499584733507034L;

    @Override
    public void run() throws Exception {
        File file = getWorkflowContext().getOriginalFiles().get(0).getTransientFile();
        File workingDir = new File(getWorkflowContext().getWorkingDirectory(), file.getName());
        workingDir.mkdir();
        FileUtils.copyFileToDirectory(file, workingDir);
        File workingOriginal = new File(workingDir, file.getName());
        InformationResourceFileVersion original = null;
        for (InformationResourceFileVersion version : getWorkflowContext().getOriginalFiles()) {
            FileUtils.copyFileToDirectory(version.getTransientFile(), workingDir);
            version.setTransientFile(new File(workingDir, version.getFilename()));
            if(version.isPrimaryFile()) {
                original = version;
            }
        }

        String extension = FilenameUtils.getExtension(file.getName());
        switch (extension) {
            case "jpg":
            case "tif":
                // AbstractGridFormat format = GridFormatFinder.findFormat(file);
                // AbstractGridCoverage2DReader reader = format.getReader(file);

                GeoTiffFormat gtf = new GeoTiffFormat();
                // Hints hints = new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, gtf.getDefaultCRS());
                GridCoverageReader reader = gtf.getReader(workingOriginal);// , hints
                // getLogger().info("subname: {} ", reader.getCurrentSubname());
                Format format = reader.getFormat();
                getLogger().info("format: {} ({}) -- {} ", format.getVendor(), format.getVersion(), format.getDescription());
                // getLogger().info("more coverages: {} ", reader.hasMoreGridCoverages());
                GridCoverage tiffCov = reader.read(null); // We do not use any parametery here.
                if (ArrayUtils.isNotEmpty(reader.getMetadataNames())) {
                    for (String name : reader.getMetadataNames()) {
                        getLogger().info("{} {}", name, reader.getMetadataValue(name));
                    }
                }
                getLogger().info(tiffCov.toString());
                // http://docs.geotools.org/latest/userguide/library/coverage/grid.html#coveragestack

                getLogger().info("env {} ", tiffCov.getEnvelope());
                getLogger().info("CRS {} ", tiffCov.getCoordinateReferenceSystem());
                getLogger().info("Geom {} ", tiffCov.getGridGeometry().toString());
                getLogger().info("overviews {} ", tiffCov.getNumOverviews());
                List<GridCoverage> sources = tiffCov.getSources();

                File geoJson = new File(System.getProperty("java.io.tmpdir"), FilenameUtils.getBaseName(file.getName()) + ".json");

                FeatureJSON fjson = new FeatureJSON();
                StringWriter writer = new StringWriter();
                fjson.writeCRS(tiffCov.getCoordinateReferenceSystem(), writer);
                Envelope env = tiffCov.getEnvelope();
                ReferencedEnvelope re = new ReferencedEnvelope(env);
                Polygon geometry = JTS.toGeometry(re);
                getLogger().debug("{}", geometry);
                DefaultFeatureCollection collection = new DefaultFeatureCollection(null, null);
                SimpleFeatureType type = DataUtilities.createType("location", "geom:Polygon,name:String");
                final SimpleFeature feature1 = SimpleFeatureBuilder.build(type, new Object[] { geometry, file.getName() }, null);
                collection.add(feature1);
                try {
                    FileWriter fwriter = new FileWriter(geoJson);
                    fjson.writeFeatureCollection(collection, fwriter);
                    IOUtils.closeQuietly(fwriter);
                    if (original != null) {
                        addDerivativeFile(original, geoJson, VersionType.GEOJSON);
                    }
                } catch (IOException e) {
                    getLogger().error("could not convert dataset to GeoJSON", e);
                }

                getLogger().debug(writer.toString());

                getLogger().info(" {} ", sources);
                // GeoTiffReader rdr = (GeoTiffReader) ((new GeoTiffFormat()).getReader(file));
                break;
            case "kml":
                Configuration config = new org.geotools.kml.v22.KMLConfiguration();
                try {
                    parseFile(file, config);
                } catch (Exception e) {
                    config = new KMLConfiguration();
                    parseFile(file, config);

                }
                break;
        }
    }

    private void parseFile(File file, Configuration config) throws IOException, SAXException, ParserConfigurationException, FileNotFoundException {
        /*
         * this may be a better way to parse the KML
         * http://gis.stackexchange.com/questions/4549/how-to-parse-kml-data-using-geotools
         * issue -- this doesn't properly detect the version of KML and use the appropriate configuration...
         * FIXME: once we get to the point that we have "data" we need to move this into a database parser, but that would also require that we figure out how
         * to use the external data callout to unify a dataset as well...
         */

        Parser parser = new Parser(config);
        SimpleFeature f = (SimpleFeature) parser.parse(new FileInputStream(file));
        Collection<?> placemarks = (Collection<?>) f.getAttribute("Feature");
        for (Object mark : placemarks) {
            SimpleFeature feature = (SimpleFeature) mark;
            @SuppressWarnings("unused")
            Set<Entry<Object, Object>> entrySet = feature.getUserData().entrySet();
            getLogger().info("props:{} \n attributes: {}\nuserData:{}", feature.getProperties(), feature.getAttributes(), feature.getUserData());
            getLogger().info("{}: {}", feature.getAttribute("name"), feature.getAttribute("description"));
        }
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

}
