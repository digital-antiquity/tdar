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
 * Annotation for tracking bulk import fields and providing a central location for all of the long descriptions.
 * 
 * @author Adam Brin
 * 
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface BulkImportField {
    String FILENAME_DESCRIPTION = "List each file you would like to upload individually.  A new metadata record will be created for every file listed.  Note:  the filenames MUST match the actual file names you uploaded on the \"New Batch Upload\" screen.";
    String TITLE_DESCRIPTION = "Title - is the Main title of the Resource. This may or may not be the actual title of the document, image, database, etc. Note: The title is preference when searching for a file. ";
    String DESCRIPTION_DESCRIPTION = "The description may include an abstract, outline of contents, and or additional information that is not addressed in the other metadata will be a valuable took for others to find the files in your projects.";
    String YEAR_DESCRIPTION = "The year the physical resource was created";
    String DOCUMENT_TITLE = "Gray items - used for Documents only";
    String COPY_LOCATION_DESCRIPTION = "Copy Location - can be a physical location (name of organization/company/etc. with address if possible) or could be a digital location (physical server location and/or web address).  Idea is to allow user to relocate original physical/digital item(s).";
    String METADATA_LANGUAGE_DESCRIPTION = "Metadata language - Language of record metadata you entered- NOT language as it refers to the actual file.";
    String RESOURCE_LANGAGE_DESCRIPTION = "Resource Language: Language the file/resource you are uploading is in";
    String CREATOR_ROLE_DESCRIPTION = "The Role of the person that created/authored/  contributed/ sponsored the resource. NOTE:  if there are multiple individuals and/ or institutions that should be credited, you will need to add them individually to each metadata record by editing each applicable resource created by this batch upload. NOTE: This does field  does not refer to the submitter/creator of the metadata record - which  is an automatically populated field. ";
    String CREATOR_INSTITUTION_DESCRIPTION = "An Institutional creator/ author/ sponsor's / etc. ";
    String CREATOR_PERSON_INSTITUTION_DESCRIPTION = "The creator/ author/ sponsor's / etc. associated institution.  NOTE: This does field does not refer to the submitter/creator of the metadata record -which  is an automatically populated field. ";
    String CRETOR_EMAIL_DESCRIPTION = "The e-mail of the person that created/authored/  contributed/ sponsored the resource.  This would be useful should a user have any questions/comments. NOTE: This does field  does not refer to the submitter/creator of the metadata record - which  is an automatically populated field. ";
    String CREATOR_FNAME_DESCRIPTION = "The first name of the person that created/authored/  contributed/ sponsored the resource. NOTE:  if there are multiple individuals and/ or institutions that should be credited, you will need to add them individually to each metadata record by editing each applicable resource created by this batch upload. NOTE: This does field  does not refer to the submitter/creator of the metadata record - which  is an automatically populated field. ";
    String CREATOR_LNAME_DESCRIPTION = "The name of the person that created/authored/  contributed/ sponsored the resource. NOTE:  if there are multiple individuals and/ or institutions that should be credited, you will need to add them individually to each metadata record by editing each applicable resource created by this batch upload. NOTE: This does field  does not refer to the submitter/creator of the metadata record - which  is an automatically populated field. ";

    String LICENSE_TEXT = "License Text";
    String LICENSE_TYPE = "License Type";
    String COPYRIGHT_HOLDER = "Copyright Holder";
    String TITLE_LABEL = "Title";
    String DESCRIPTION_LABEL = "Description";
    String YEAR_LABEL = "Date Created (Year)";

    // a way to tell the parser about subclasses (Creator -> Person/Institution)
    Class<?>[] implementedSubclasses() default {};

    // the label that will show in Excel
    String label() default "";

    // The comment field in excel
    String comment() default "";

    // Whether the field is required or not
    boolean required() default false;

    // The sort order for the excel columns, lower means closer to the left. Sorting is within the class
    int order() default 0;
}
