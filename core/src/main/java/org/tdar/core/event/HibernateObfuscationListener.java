package org.tdar.core.event;

import org.hibernate.EmptyInterceptor;
import org.hibernate.HibernateException;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.spi.SaveOrUpdateEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;

public class HibernateObfuscationListener extends EmptyInterceptor implements SaveOrUpdateEventListener {

    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final long serialVersionUID = -7499033317841172362L;

    @Override
    public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException {
        if (event.getEntity() instanceof LatitudeLongitudeBox) {
            LatitudeLongitudeBox entity = (LatitudeLongitudeBox) event.getEntity();
            entity.obfuscateNorthSouth();
            entity.obfuscateEastWest();
//            logger.debug("saveorupdate: {}", event.getEntity());
        }
    }

}
