package org.tdar.core.service.processes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.service.resource.ResourceService;

import com.google.common.base.Objects;

/**
 * Sets obfuscated values on LatLongBoxes where necessary. Deals with legacy entries where LLBs have not been set.
 * 
 * @author abrin
 *
 */
@Component
public class LegacyObfuscateLatLongProcess extends ScheduledBatchProcess<LatitudeLongitudeBox> {

    /**
     * 
     */
    private static final long serialVersionUID = -2676575775090929593L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient ResourceService resourceService;

    @Override
    public boolean isSingleRunProcess() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "LLB Obfsucate";
    }

    @Override
    public Class<LatitudeLongitudeBox> getPersistentClass() {
        return LatitudeLongitudeBox.class;
    }

    @Override
    public boolean isEnabled() {
        return false;// geoSearchService.isEnabled();
    }

    @Override
    public void process(LatitudeLongitudeBox llb) {
        Double minObfuscatedLatitude = llb.getMinObfuscatedLatitude();
        Double minObfuscatedLongitude = llb.getMinObfuscatedLongitude();
        Double maxObfuscatedLatitude = llb.getMaxObfuscatedLatitude();
        Double maxObfuscatedLongitude = llb.getMaxObfuscatedLongitude();
        boolean changed = false;
        if (!Objects.equal(llb.getMinimumLatitude(), minObfuscatedLatitude)) {
            changed = true;
        }
        if (!Objects.equal(llb.getMaximumLatitude(), maxObfuscatedLatitude)) {
            changed = true;
        }
        if (!Objects.equal(llb.getMinimumLongitude(), minObfuscatedLongitude)) {
            changed = true;
        }
        if (!Objects.equal(llb.getMaximumLongitude(), maxObfuscatedLongitude)) {
            changed = true;
        }
        if (changed) {
            logger.debug("changed: {}", llb);
            genericDao.saveOrUpdate(llb);
        }
    }

    @Override
    public int getBatchSize() {
        return 30;
    }
}
