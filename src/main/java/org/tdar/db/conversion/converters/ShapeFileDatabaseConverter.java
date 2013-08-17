package org.tdar.db.conversion.converters;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.db.conversion.ConversionStatisticsManager;
import org.tdar.db.model.abstracts.TargetDatabase;

import com.vividsolutions.jts.geom.MultiLineString;

/**
 * The class reads an access db file, and converts it into other types of db
 * files.
 * 
 * @author <a href='Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 * @latest $Date$
 */
public class ShapeFileDatabaseConverter extends DatasetConverter.Base {
    private static final String DB_PREFIX = "s";
    private static final String ERROR_CORRUPT_DB = "The system was unable to read portions of this Shapefile.";
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private File databaseFile;

    public String getDatabasePrefix() {
        return DB_PREFIX;
    }

    public ShapeFileDatabaseConverter() {
    }

    private List<InformationResourceFileVersion> versions = new ArrayList<>();

    public ShapeFileDatabaseConverter(TargetDatabase targetDatabase, InformationResourceFileVersion... versions) {
        setTargetDatabase(targetDatabase);
        setInformationResourceFileVersion(versions[0]);
        this.versions = Arrays.asList(versions);
    }

    protected void openInputDatabase() throws IOException {
        setDatabaseFile(getInformationResourceFileVersion().getTransientFile());

        File workingDir = new File(TdarConfiguration.getInstance().getTempDirectory(), getDatabaseFile().getName());
        workingDir.mkdir();
        for (InformationResourceFileVersion version : versions) {
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
    public void dumpData() throws Exception {
        // start dumping ...
        // Map<String, DataTable> dataTableNameMap = new HashMap<String, DataTable>();
        setIndexedContentsFile(new File(TdarConfiguration.getInstance().getTempDirectory(), String.format("%s.%s.%s", getFilename(), "index", "txt")));
        FileOutputStream fileOutputStream = new FileOutputStream(getIndexedContentsFile());
        BufferedOutputStream indexedFileOutputStream = new BufferedOutputStream(fileOutputStream);
        DataTable dataTable = createDataTable(getFilename());
        // drop the table if it has been there
        targetDatabase.dropTable(dataTable);

        Map<String, URL> connect = new HashMap<>();
        connect.put("url", getDatabaseFile().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(connect);
        String[] typeNames = dataStore.getTypeNames();
        String typeName = typeNames[0];
        logger.info(typeName);
        System.out.println("Reading content " + typeName);
        logger.info("infO: {} {} ({})", dataStore.getInfo().getTitle(), dataStore.getInfo().getDescription(), dataStore.getInfo().getKeywords());
        FeatureSource<?, ?> featureSource = dataStore.getFeatureSource(typeName);
        FeatureCollection<?, ?> collection = featureSource.getFeatures();
        FeatureIterator<?> iterator = collection.features();
        logger.debug("{}", dataStore.getNames());
        // Filter filter = CQL.toFilter(text.getText());
        // SimpleFeatureCollection features = source.getFeatures(filter);
        // FeatureCollectionTableModel model = new FeatureCollectionTableModel(features);
        for (PropertyDescriptor descriptors : collection.getSchema().getDescriptors()) {
            PropertyType type = descriptors.getType();
            DataTableColumnType columnType = DataTableColumnType.BLOB;

            //FIXME: not 100% sure this is right
            if (type.getBinding().isAssignableFrom(String.class) || type.getBinding().isAssignableFrom(MultiLineString.class)) {
                columnType = DataTableColumnType.TEXT;
            } else if (type.getBinding().isAssignableFrom(Double.class) || type.getBinding().isAssignableFrom(Float.class)) {
                columnType = DataTableColumnType.DOUBLE;
            } else if (type.getBinding().isAssignableFrom(Long.class) || type.getBinding().isAssignableFrom(Integer.class)) {
                columnType = DataTableColumnType.BIGINT;
            } else if (type.getBinding().isAssignableFrom(MultiLineString.class)) {
                columnType = DataTableColumnType.BLOB;
            } else {
                logger.error("unknown binding: {} ", type.getBinding());
            }
            createDataTableColumn(descriptors.getName().getLocalPart(), columnType, dataTable);
        }

        targetDatabase.createTable(dataTable);
        ConversionStatisticsManager statisticsManager = new ConversionStatisticsManager(dataTable.getDataTableColumns());

        try {
            @SuppressWarnings("unused")
            int rowCount = collection.size();
            while (iterator.hasNext()) {
                HashMap<DataTableColumn, String> valueColumnMap = new HashMap<DataTableColumn, String>();
                Feature feature = iterator.next();
                StringBuilder sb = new StringBuilder();
                for (Property prop : feature.getValue()) {
                    DataTableColumn column = dataTable.getColumnByDisplayName(prop.getName().toString());
                    String value = prop.getValue().toString();
                    valueColumnMap.put(column, value);
                    sb.append(value).append(" ");
                    statisticsManager.updateStatistics(column, value);

                }
                targetDatabase.addTableRow(dataTable, valueColumnMap);
                logger.trace("{}", valueColumnMap);
                IOUtils.write(sb.toString(), indexedFileOutputStream);

            }
        } catch (Exception e) {
            logger.error("could not process shapefile: {}", e);
            throw new TdarRecoverableRuntimeException(ERROR_CORRUPT_DB);
        } finally {
            iterator.close();
            dataStore.dispose();
            completePreparedStatements();
            alterTableColumnTypes(dataTable, statisticsManager.getStatistics());
        }
    }

    public File getDatabaseFile() {
        return databaseFile;
    }

    public void setDatabaseFile(File databaseFile) {
        this.databaseFile = databaseFile;
    }
}
