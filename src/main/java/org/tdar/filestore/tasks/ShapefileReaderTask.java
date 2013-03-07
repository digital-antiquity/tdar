package org.tdar.filestore.tasks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.geometry.Geometry;
import org.tdar.filestore.tasks.Task.AbstractTask;

public class ShapefileReaderTask extends AbstractTask {

    @Override
    public void run() throws Exception {

        
        File file = new File("C:\\Users\\abrin\\Desktop\\Ruins of Tikal map-v11.tif.xml");
//        http://stackoverflow.com/questions/2044876/does-anyone-know-of-a-library-in-java-that-can-parse-esri-shapefiles
        try {
          Map connect = new HashMap();
          connect.put("url", file.toURL());

          DataStore dataStore = DataStoreFinder.getDataStore(connect);
          String[] typeNames = dataStore.getTypeNames();
          String typeName = typeNames[0];
          getLogger().info(typeName);
          System.out.println("Reading content " + typeName);

          FeatureSource featureSource = dataStore.getFeatureSource(typeName);
          FeatureCollection collection = featureSource.getFeatures();
          FeatureIterator iterator = collection.features();


          try {
            while (iterator.hasNext()) {
              Feature feature = iterator.next();
              GeometryAttribute sourceGeometry = feature.getDefaultGeometryProperty();
            }
          } finally {
            iterator.close();
          }

        } catch (Throwable e) {
            getLogger().error("exception" , e);
        }        
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

}
