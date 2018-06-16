package org.tdar.db.conversion.converters;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.datatable.DataTableColumnType;
import org.tdar.datatable.TDataTable;
import org.tdar.datatable.TDataTableColumn;
import org.tdar.db.ImportDatabase;
import org.tdar.db.conversion.ConversionStatisticsManager;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.FileStoreFileProxy;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * The class reads an access db file, and converts it into other types of db
 * files.
 * 
 * @author <a href='Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 * @latest $Date$
 */
public class ShapeFileDatabaseConverter extends AbstractDatabaseConverter {
    private static final String DB_PREFIX = "s";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private File databaseFile;

    @Override
    public String getDatabasePrefix() {
        return DB_PREFIX;
    }

    public ShapeFileDatabaseConverter() {
    }

    private List<FileStoreFileProxy> versions = new ArrayList<>();
    private File geoJsonFile;

    public ShapeFileDatabaseConverter(ImportDatabase targetDatabase, FileStoreFileProxy... versions) {
        setTargetDatabase(targetDatabase);
        setInformationResourceFileVersion(versions[0]);
        this.versions = Arrays.asList(versions);
    }

    @Override
    protected void openInputDatabase() throws IOException {
        setDatabaseFile(getInformationResourceFileVersion().getTransientFile());

        File workingDir = new File(TdarConfiguration.getInstance().getTempDirectory(), getDatabaseFile().getName());
        workingDir.mkdir();
        for (FileStoreFileProxy version : versions) {
            FileUtils.copyFileToDirectory(version.getTransientFile(), workingDir);
            File workingOriginal = new File(workingDir, getDatabaseFile().getName());
            setDatabaseFile(workingOriginal);
        }
        this.setIrFileId(getInformationResourceFileVersion().getId());
        this.setFilename(getDatabaseFile().getName());
    }

    /**
     * Dumps the access database wrapped by this converter into the target
     * database (in our current case, PostgresDatabase).
     * 
     * @param targetDatabase
     */
    @Override
    public void dumpData() throws Exception {
        // start dumping ...
        // Map<String, DataTable> dataTableNameMap = new HashMap<String, DataTable>();
        setIndexedContentsFile(new File(TdarConfiguration.getInstance().getTempDirectory(), String.format("%s.%s.%s", getFilename(), "index", "txt")));
        FileOutputStream fileOutputStream = new FileOutputStream(getIndexedContentsFile());
        BufferedOutputStream indexedFileOutputStream = new BufferedOutputStream(fileOutputStream);
        TDataTable dataTable = createDataTable(getFilename(), 0);
        // drop the table if it has been there
        targetDatabase.dropTable(dataTable);

        Map<String, URL> connect = new HashMap<>();
        connect.put("url", getDatabaseFile().toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(connect);
        String[] typeNames = dataStore.getTypeNames();
        String typeName = typeNames[0];
        logger.info(typeName);
        System.out.println("Reading content " + typeName);
        try {
            logger.info("infO: {} {} ({})", dataStore.getInfo().getTitle(), dataStore.getInfo().getDescription(), dataStore.getInfo().getKeywords());
        } catch (Error e) {
            logger.warn("exception in shapefile processing", e);
        }
        FeatureSource<?, ?> featureSource = dataStore.getFeatureSource(typeName);
        FeatureCollection<?, ?> collection = featureSource.getFeatures();
        FeatureIterator<?> iterator = collection.features();

        dumpToGeoJson(collection);

        // Filter filter = CQL.toFilter(text.getText());
        // SimpleFeatureCollection features = source.getFeatures(filter);
        // FeatureCollectionTableModel model = new FeatureCollectionTableModel(features);
        int count = 0;
        for (PropertyDescriptor descriptors : collection.getSchema().getDescriptors()) {
            PropertyType type = descriptors.getType();
            DataTableColumnType columnType = DataTableColumnType.BLOB;

            // FIXME: not 100% sure this is right
            if (type.getBinding().isAssignableFrom(String.class) || type.getBinding().isAssignableFrom(MultiLineString.class)
                    || type.getBinding().isAssignableFrom(LineString.class)) {
                columnType = DataTableColumnType.TEXT;
            } else if (type.getBinding().isAssignableFrom(Double.class) || type.getBinding().isAssignableFrom(Float.class)) {
                columnType = DataTableColumnType.DOUBLE;
            } else if (type.getBinding().isAssignableFrom(Long.class) || type.getBinding().isAssignableFrom(Integer.class)) {
                columnType = DataTableColumnType.BIGINT;
            } else if (type.getBinding().isAssignableFrom(Polygon.class) || type.getBinding().isAssignableFrom(Point.class) ||
                    type.getBinding().isAssignableFrom(MultiPolygon.class) || type.getBinding().isAssignableFrom(MultiPoint.class)) {
                columnType = DataTableColumnType.BLOB;
            } else {
                logger.error("unknown binding: {} ", type.getBinding());
            }
            createDataTableColumn(descriptors.getName().getLocalPart(), columnType, dataTable, count);
            count++;
        }

        targetDatabase.createTable(dataTable);
        ConversionStatisticsManager statisticsManager = new ConversionStatisticsManager(dataTable.getDataTableColumns());

        try {
            @SuppressWarnings("unused")
            int rowCount = collection.size();
            int rowNum = 0;
            while (iterator.hasNext()) {
                rowNum++;
                HashMap<TDataTableColumn, String> valueColumnMap = new HashMap<>();
                Feature feature = iterator.next();
                StringBuilder sb = new StringBuilder();
                for (Property prop : feature.getValue()) {
                    TDataTableColumn column = dataTable.getColumnByDisplayName(prop.getName().toString());
                    String value = null;
                    if (prop.getValue() != null) {
                        logger.debug("{} - {}", prop.getName(), prop.getValue().getClass());
                        value = prop.getValue().toString();
                    }
                    valueColumnMap.put(column, value);
                    sb.append(value).append(" ");
                    statisticsManager.updateStatistics(column, value, rowNum);

                }
                targetDatabase.addTableRow(dataTable, valueColumnMap);
                logger.trace("{}", valueColumnMap);
                IOUtils.write(sb.toString(), indexedFileOutputStream);

            }
        } catch (Exception e) {
            logger.error("could not process shapefile: {}", e);
            throw new TdarRecoverableRuntimeException("shapeFileConveter.corrupt");
        } finally {
            iterator.close();
            dataStore.dispose();
            completePreparedStatements();
            alterTableColumnTypes(dataTable, statisticsManager.getStatistics());
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void dumpToGeoJson(final FeatureCollection<?, ?> collection_) {
        FeatureJSON fjson = new FeatureJSON();
        FeatureCollection<?, ?> collection = collection_;
        CoordinateReferenceSystem crs = collection.getSchema().getCoordinateReferenceSystem();
        logger.debug("{}", collection.getSchema());

        try {
            if (crs != null) {
                collection = new ReprojectingFeatureCollection((FeatureCollection) collection, crs, CRS.decode("EPSG:4326"));
            }
            setGeoJsonFile(new File(System.getProperty("java.io.tmpdir"), FilenameUtils.getBaseName(getDatabaseFile().getName()) + ".json"));
            FileWriter writer = new FileWriter(getGeoJsonFile());
            fjson.writeFeatureCollection(collection, writer);
            IOUtils.closeQuietly(writer);
        } catch (IOException | FactoryException e) {
            logger.error("could not convert dataset to GeoJSON", e);
        }
    }

    public File getDatabaseFile() {
        return databaseFile;
    }

    public void setDatabaseFile(File databaseFile) {
        this.databaseFile = databaseFile;
    }

    public File getGeoJsonFile() {
        return geoJsonFile;
    }

    public void setGeoJsonFile(File geoJsonFile) {
        this.geoJsonFile = geoJsonFile;
    }
}
