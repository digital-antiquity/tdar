package org.tdar.dataone.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBException;

import org.dspace.foresite.OREException;
import org.dspace.foresite.ORESerialiserException;
import org.jdom2.JDOMException;
import org.tdar.core.bean.resource.InformationResource;

public interface D1Formatter {

    ObjectResponseContainer constructMetadataFormatObject(InformationResource resource)
            throws JAXBException, UnsupportedEncodingException, NoSuchAlgorithmException;

    ObjectResponseContainer constructD1FormatObject(InformationResource resource)
            throws OREException, URISyntaxException, ORESerialiserException, JDOMException, IOException, UnsupportedEncodingException, NoSuchAlgorithmException;

}
