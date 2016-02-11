package org.tdar.core.service.processes.upgradeTasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.service.processes.AbstractScheduledBatchProcess;
import org.tdar.core.service.resource.ResourceService;

import com.google.common.base.Objects;

/**
 * Sets obfuscated values on LatLongBoxes where necessary. Deals with legacy entries where LLBs have not been set.
 * 
 * @author abrin
 *
 */
@Component
public class LegacyObfuscateLatLongProcess extends AbstractScheduledBatchProcess<LatitudeLongitudeBox> {

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
        double minObfuscatedLatitude = llb.getMinObfuscatedLatitude().doubleValue();
        double minObfuscatedLongitude = llb.getMinObfuscatedLongitude().doubleValue();
        double maxObfuscatedLatitude = llb.getMaxObfuscatedLatitude().doubleValue();
        double maxObfuscatedLongitude = llb.getMaxObfuscatedLongitude().doubleValue();
        boolean changed = false;
        if (!Objects.equal(llb.getMinimumLatitude().doubleValue(), minObfuscatedLatitude)) {
            changed = true;
        }
        if (!Objects.equal(llb.getMaximumLatitude().doubleValue(), maxObfuscatedLatitude)) {
            changed = true;
        }
        if (!Objects.equal(llb.getMinimumLongitude().doubleValue(), minObfuscatedLongitude)) {
            changed = true;
        }
        if (!Objects.equal(llb.getMaximumLongitude().doubleValue(), maxObfuscatedLongitude)) {
            changed = true;
        }
        if (Math.abs(maxObfuscatedLatitude - minObfuscatedLatitude) <= LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES ||
                Math.abs(maxObfuscatedLongitude - minObfuscatedLongitude) <= LatitudeLongitudeBox.ONE_MILE_IN_DEGREE_MINUTES) {
            logger.error("HUH: {}|{}| {}", maxObfuscatedLatitude, minObfuscatedLongitude, llb);
        }

        if (changed) {
            genericDao.detachFromSession(llb);
            genericDao.update(llb);
        }
    }

    @Override
    public int getBatchSize() {
        return 500;
    }
}
