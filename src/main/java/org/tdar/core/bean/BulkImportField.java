/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Adam Brin
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface BulkImportField {
    public static final String FILENAME_DESCRIPTION = "List each file you would like to upload individually.  A new tDAR record will be created for every file listed.  Note:  the filenames MUST match the actual file names you uploaded on the tDAR \"New Batch Upload\" screen.";
    public static final String TITLE_DESCRIPTION = "Title - is the Main title of the Resource. This may or may not be the actual title of the document, image, database, etc. Note: The title is preference when searching for a file. ";
    public static final String DESCRIPTION_DESCRIPTION = "The description may include an abstract, outline of contents, and or additional information that is not addressed in the other metadata will be a valuable took for others to find the files in your projects.";
    public static final String YEAR_DESCRIPTION = "The year the physical resource was created";
    public static final String DOCUMENT_TITLE = "Gray items - used for Documents only";
    public static final String COPY_LOCATION_DESCRIPTION = "Copy Location - can be a physical location (name of organization/company/etc. with address if possible) or could be a digital location (physical server location and/or web address).  Idea is to allow user to relocate original physical/digital item(s).";
    public static final String METADATA_LANGUAGE_DESCRIPTION = "Metadata language - Language of tDAR metadata you entered- NOT language as it refers to the actual file.";
    public static final String RESOURCE_LANGAGE_DESCRIPTION = "Resource Language: Language the file/resource you are uploading is in";
    public static final String CREATOR_ROLE_DESCRIPTION = "The Role of the person that created/authored/  contributed/ sponsored the resource. NOTE:  if there are multiple individuals and/ or institutions that should be credited, you will need to add them individually to each tDAR record using the tDAR website and editing each applicable resource created by this batch upload. NOTE: This does field  does not refer to the submitter/creator of the tDAR record - which  is an automatically populated field. ";
    public static final String CREATOR_INSTITUTION_DESCRIPTION = "An Institutional creator/ author/ sponsor's / etc. ";
    public static final String CREATOR_PERSON_INSTITUTION_DESCRIPTION = "The creator/ author/ sponsor's / etc. associated institution.  NOTE: This does field does not refer to the submitter/creator of the tDAR record -which  is an automatically populated field. ";
    public static final String CRETOR_EMAIL_DESCRIPTION = "The e-mail of the person that created/authored/  contributed/ sponsored the resource.  This would be useful should a user have any questions/comments. NOTE: This does field  does not refer to the submitter/creator of the tDAR record - which  is an automatically populated field. ";
    public static final String CREATOR_FNAME_DESCRIPTION = "The first name of the person that created/authored/  contributed/ sponsored the resource. NOTE:  if there are multiple individuals and/ or institutions that should be credited, you will need to add them individually to each tDAR record using the tDAR website and editing each applicable resource created by this batch upload. NOTE: This does field  does not refer to the submitter/creator of the tDAR record - which  is an automatically populated field. ";
    public static final String CREATOR_LNAME_DESCRIPTION = "The name of the person that created/authored/  contributed/ sponsored the resource. NOTE:  if there are multiple individuals and/ or institutions that should be credited, you will need to add them individually to each tDAR record using the tDAR website and editing each applicable resource created by this batch upload. NOTE: This does field  does not refer to the submitter/creator of the tDAR record - which  is an automatically populated field. ";

    public Class<?>[] implementedSubclasses() default {};

    public String label() default "";

    public String comment() default "";

    public boolean required() default false;

    public int order() default 0;
}
