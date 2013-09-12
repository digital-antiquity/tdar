package org.tdar.core.bean.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.constraints.Length;

/**
 * The elements on this resource are inspired by:
 * http://guides.archaeologydataservice.ac.uk/g2gp/Audio_3
 * Not done yet are:
 *   Transcript
 * Also, not yet resolved: we should try to automatically extract some of the fields from the audio file: are the ones below enough?
 * Note: the length of the recording is kept on the InformationResourceFileVersion
 * @author Martin Paulo
 */
@Entity
@Indexed
@Table(name = "audio")
@XmlRootElement(name = "audio")
public final class Audio extends InformationResource {

    private static final long serialVersionUID = -5207630181373559506L;

    @Column(name = "software")
    @Length(max = 255)
    private String software;
    
    // all the following fields should be in the header (and probably length)
    @Column(name = "bit_depth")
    private Integer bitDepth;

    @Column(name = "bit_rate")
    private Integer bitRate;

    @Column(name = "sample_rate")
    private Integer sampleRate;

    @Column(name = "audio_codec")
    @Length(max = 255)
    private String audioCodec;

    public Audio() {
        setResourceType(ResourceType.AUDIO);
    }

    /**
     * @return The software (or device) used to create the file.
     */
    public String getSoftware() {
        return software;
    }

    /**
     * @param software
     *            The software (or device) used to create the file.
     */
    public void setSoftware(String software) {
        this.software = software;
    }

    /**
     * @return The bit depth of the recording E.g. 16 or 24 bit.
     */
    public Integer getBitDepth() {
        return bitDepth;
    }

    /**
     * @param bitDepth
     *            The bit depth of the recording E.g. 16 or 24 bit.
     */
    public void setBitDepth(Integer bitDepth) {
        this.bitDepth = bitDepth;
    }

    /**
     * @return The bit rate Optional, often recorded as kbps
     */
    public Integer getBitRate() {
        return bitRate;
    }

    /**
     * @param bitRate
     *            The bit rate Optional, often recorded as kbps
     */
    public void setBitRate(Integer bitRate) {
        this.bitRate = bitRate;
    }

    /**
     * @return The sample rate (KHz) E.g. 44.1kHz
     */
    public Integer getSampleRate() {
        return sampleRate;
    }

    /**
     * @param sampleRate
     *            The sample rate (KHz) E.g. 44.1kHz
     */
    public void setSampleRate(Integer sampleRate) {
        this.sampleRate = sampleRate;
    }

    /**
     * @return The codec used in creating the file e.g. FLAC or AAC.
     */
    public String getAudioCodec() {
        return audioCodec;
    }

    /**
     * @param audioCodec
     *            The codec used in creating the file e.g. FLAC or AAC.
     */
    public void setAudioCodec(String audioCodec) {
        this.audioCodec = audioCodec;
    }

}
