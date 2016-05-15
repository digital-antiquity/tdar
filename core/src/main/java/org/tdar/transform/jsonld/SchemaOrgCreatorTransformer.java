package org.tdar.transform.jsonld;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.RelationType;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.UrlService;

/**
 * Convert a creator to a proper JSON Linked Data String
 * @author abrin
 *
 */
public class SchemaOrgCreatorTransformer extends AbstractSchemaOrgMetadataTransformer {

    private static final long serialVersionUID = -6030535358753854271L;

    @SuppressWarnings({ "unchecked" })
    public String convert(SerializationService ss, Creator<?> creator, String imageUrl) throws IOException {
        Map<String, Object> jsonLd = new HashMap<String, Object>();
        if (creator == null) {
            return ss.convertToJson(jsonLd);
        }
        jsonLd.put(TYPE, "Organization");
        if (StringUtils.isNotBlank(creator.getUrl())) {
            add(jsonLd, "url", creator.getUrl());
        } else {
            add(jsonLd, "url", UrlService.absoluteUrl(creator));
        }
        add(jsonLd, NAME, creator.getProperName());
        add(jsonLd, SCHEMA_DESCRIPTION, creator.getDescription());
        add(jsonLd, "schema:image", imageUrl);
        if (creator instanceof Person) {
            Person person = (Person) creator;
            jsonLd.put(TYPE, "Person");
            if (person.getEmailPublic()) {
                add(jsonLd, "schema:email", person.getEmail());
            }
            if (person.getPhonePublic()) {
                add(jsonLd, "schema:telephone", person.getPhone());
            }
            add(jsonLd, "schema:affiliation", person.getInstitutionName());

        } else {
            add(jsonLd, "schema:logo", imageUrl);
        }

        if (CollectionUtils.isNotEmpty(creator.getAddresses()) && (creator instanceof Institution || ((Person)creator).getPhonePublic())) {
            for (Address address : creator.getAddresses()) {
                if (address.getType() == AddressType.MAILING) {
                    Map<String, Object> addLd = new HashMap<String, Object>();
                    add(addLd, TYPE, "PostalAddress");
                    add(addLd, "schema:addressLocality", address.getCity());
                    add(addLd, "schema:addressRegion", address.getState());
                    add(addLd, "schema:postalCode", address.getPostal());
                    String street = address.getStreet1();
                    if (StringUtils.isNotBlank(address.getStreet2())) {
                        street += "\n" + address.getStreet2();
                    }
                    add(addLd, "schema:streetAddress", street);
                    jsonLd.put("schema:address", addLd);
                    break;
                }
            }
        }
        
        for (Creator<?> syn : (Set<Creator<?>>)creator.getSynonyms()) {
            jsonLd.put(RelationType.HAS_VERSION.getJsonKey(), syn.getDetailUrl());
        }


        addContextSection(jsonLd);
        
        return ss.convertToJson(jsonLd);
    }
}
