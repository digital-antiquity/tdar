package org.tdar.core.bean.statistics;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.tdar.core.bean.resource.file.InformationResourceFile;

@Entity
@Table(name = "information_resource_file_download_statistics", indexes = {
        @Index(name = "file_download_stats_count_id", columnList = "information_resource_file_id, id")
})
/**
 * Tracks anonymous download statistics
 * 
 * @author abrin
 *
 */
public class FileDownloadStatistic extends AbstractResourceStatistic<InformationResourceFile> {
    private static final long serialVersionUID = 3754152671288642718L;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "information_resource_file_id")
    private InformationResourceFile reference;

    public FileDownloadStatistic() {
    };

    public FileDownloadStatistic(Date date, InformationResourceFile r) {
        setDate(date);
        setReference(r);
    }

    @Override
    public InformationResourceFile getReference() {
        return reference;
    }

    @Override
    public void setReference(InformationResourceFile reference) {
        this.reference = reference;
    }

}
