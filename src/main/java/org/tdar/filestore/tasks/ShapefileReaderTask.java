package org.tdar.filestore.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.feature.simple.SimpleFeature;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.filestore.tasks.Task.AbstractTask;
import org.xml.sax.SAXException;

public class ShapefileReaderTask extends AbstractTask {

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
        for (InformationResourceFileVersion version : getWorkflowContext().getOriginalFiles()) {
            FileUtils.copyFileToDirectory(version.getTransientFile(), workingDir);
            version.setTransientFile(new File(workingDir, version.getFilename()));
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
