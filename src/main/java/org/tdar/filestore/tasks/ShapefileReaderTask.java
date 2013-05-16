package org.tdar.filestore.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Parser;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.filestore.tasks.Task.AbstractTask;

public class ShapefileReaderTask extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 8419499584733507034L;

    @Override
    public void run() throws Exception {
        File file = getWorkflowContext().getOriginalFiles().get(0).getFile();
        File workingDir = new File(getWorkflowContext().getWorkingDirectory(), file.getName());
        workingDir.mkdir();
        FileUtils.copyFileToDirectory(file, workingDir);
        File workingOriginal = new File(workingDir, file.getName());
        for (InformationResourceFileVersion version : getWorkflowContext().getOriginalFiles()) {
            FileUtils.copyFileToDirectory(version.getFile(), workingDir);
        }

        String extension = FilenameUtils.getExtension(file.getName());
        switch (extension) {
            case "tif":
                // AbstractGridFormat format = GridFormatFinder.findFormat(file);
                // AbstractGridCoverage2DReader reader = format.getReader(file);

                GeoTiffFormat gtf = new GeoTiffFormat();
                // Hints hints = new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, gtf.getDefaultCRS());
                GridCoverageReader reader = gtf.getReader(workingOriginal);// , hints
                // getLogger().info("subname: {} ", reader.getCurrentSubname());
                getLogger()
                        .info("format: {} ({}) -- {} ", reader.getFormat().getVendor(), reader.getFormat().getVersion(), reader.getFormat().getDescription());
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

                getLogger().info(" {} ", sources);
                // GeoTiffReader rdr = (GeoTiffReader) ((new GeoTiffFormat()).getReader(file));
                break;
            case "shp":
                DataStore dataStore = null;
                // http://stackoverflow.com/questions/2044876/does-anyone-know-of-a-library-in-java-that-can-parse-esri-shapefiles
                try {
                    Map connect = new HashMap();
                    connect.put("url", workingOriginal.toURL());

                    dataStore = DataStoreFinder.getDataStore(connect);
                    String[] typeNames = dataStore.getTypeNames();
                    String typeName = typeNames[0];
                    getLogger().info(typeName);
                    System.out.println("Reading content " + typeName);
                    getLogger().info("infO: {} {} ({})", dataStore.getInfo().getTitle(), dataStore.getInfo().getDescription(),
                            dataStore.getInfo().getKeywords());
                    FeatureSource featureSource = dataStore.getFeatureSource(typeName);
                    FeatureCollection collection = featureSource.getFeatures();
                    FeatureIterator iterator = collection.features();
                    getLogger().debug("{}", dataStore.getNames());
                    SimpleFeatureType TYPE = DataUtilities.createType("location", "geom:Point,name:String");

                    // File locationFile = new File("location.xsd");
                    // locationFile = locationFile.getCanonicalFile();
                    // locationFile.createNewFile();
                    //
                    // URL locationURL = locationFile.toURI().toURL();
                    // URL baseURL = locationFile.getParentFile().toURI().toURL();
                    //
                    // FileOutputStream xsd = new FileOutputStream(locationFile);
                    //
                    // GML encode = new GML(GML.Version.GML2);
                    // encode.setBaseURL(baseURL);
                    // encode.setNamespace("location", locationURL.toExternalForm());
                    // FeatureIterator featureIterator = collection.features();
                } catch (Throwable e) {
                    getLogger().error("exception", e);
                } finally {
                    dataStore.dispose();
                }
                break;
            case "kml":
                Parser parser = new Parser(new KMLConfiguration());
                SimpleFeature f = (SimpleFeature) parser.parse(new FileInputStream(file));
                Collection placemarks = (Collection) f.getAttribute("Feature");
                for (Object mark : placemarks) {
                    getLogger().info("{}", mark);
                }
                break;
        }
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

}
