<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>
<@edit.toolbar "${resource.urlNamespace}" "edit" />
<div>
<@s.form id='resourceMetadataForm' method='post' enctype='multipart/form-data' action='save'>
<@edit.basicInformation 'document' 'document' >
<#if linkedInformationResource??>
<div class='info'>
This will be linked as a <b>${linkType}</b> citation for: <b>${linkedInformationResource.title}</b>
<@s.hidden name='linkType' value='${linkType}'/>
<@s.hidden name='linkedResourceId' value='${linkedInformationResource.id?c}'/>
</div>
<!--br/-->
</#if>

<div class="wrapper" 
    tiplabel="Document Type" 
    tooltipcontent="Select the document type. Appropriate citation fields will be displayed below.">
<@s.radio name='document.documentType' id="documentType" emptyOption='false' listValue="label"  
    list='%{documentTypes}' groupLabel="Document Type" numColumns=3 />
</div>
<p id="t-title2-journal" tooltipcontent="Enter the title of the book, report, or journal this document is part of" tiplabel="Additional Title">
    <@s.textfield labelposition='left' id='journalName' label='Journal Title' 
    title="A journal title is required"
    name='document.journalName' cssClass="requiredIfVisible tdartext longfield" />
</p>
<p id="t-title2-book" tooltipcontent="Enter the title of the book, report, or journal this document is part of." tiplabel="Additional Title">
    <@s.textfield labelposition='left' id='bookTitle' label='Book Title' 
        title="A book title is required" name='document.bookTitle' cssClass="requiredIfVisible tdartext longfield" />
</p>


</@edit.basicInformation>

<@edit.allCreators "Authors / Editors" authorshipProxies 'authorship' false />

<@edit.citationInfo "document">


<p>
    <span tiplabel="Language" tooltipcontent="Select the language in which the document is written.">
        <@s.select labelposition='left' label='Language'  emptyOption='false' name='resourceLanguage'  listValue="label" list='%{languages}' cssClass="right-shortfield "/>
    </span>
        <span id="publisher-hints" 
            book="Publisher" 
            book_section="Publisher"
            journal_article="Publisher" 
            conference="Conference"
            thesis="Institution"
            other="Publisher"> &nbsp;</span>
        <span id="publisherLocation-hints" style="display:none"
            book="Publisher Loc." 
            book_section="Publisher Loc." 
            journal_article="Publisher Loc."
            conference="Location" 
            thesis="Department"
            other="Publisher Loc."> &nbsp;</span>
<br/>

    
    <p id="t-vol">
        <@s.textfield labelposition='left' id='volume' label='Volume' name='document.volume' cssClass="shortfield"  />
        <@s.textfield labelposition='left' id='journalNumber' label='Issue Number' name='document.journalNumber' cssClass="right-shortfield"  />
    </p>
    
    <p id='t-series'>
        <@s.textfield labelposition='left' id='seriesName' label='Series Title' name='document.seriesName' cssClass="longfield" />
        <br />
        <@s.textfield labelposition='left' id='seriesNumber' label='Series #' name='document.seriesNumber' cssClass="shortfield" />

        <@s.textfield labelposition='left' id='edition' label='Edition' name='document.edition' cssClass="right-shortfield" />
    </p>
    
    <p id="t-start-end">
        <@s.textfield labelposition='left' id='startPage' label='Start page' name='document.startPage' cssClass="shortfield" />
        <@s.textfield labelposition='left' id='endPage' label='End page' name='document.endPage' cssClass="right-shortfield" />    
    </p>

    <p tiplabel="Department / Publisher Location" tooltipcontent="Department name, or City,State (and Country, if relevant)">
        <label id="publisher-hints-label" for="publisher">Publisher</label>
        <@s.textfield id='publisher' name='document.publisher'
            cssClass="longfield" />
    </p><p>
        <label id="publisherLocation-hints-label" for="publisherLocation">Publisher Loc.</label>
        <@s.textfield id='publisherLocation' 
            name='document.publisherLocation'
            cssClass='longfield' />
    </p>
    <div id="t-degree">
        <@s.radio name='document.degree' id="degreeType" emptyOption='false' listValue="label"  
    list='%{degrees}' groupLabel="Degree" numColumns=3 />
	</div>        
    <p id="t-located"  tooltipcontent="Actual physical location of a copy of the document, e.g. an agency, repository, 
        or library." tiplabel="Copy Location">
        <@s.textfield labelposition='left' id='copyLocation' label='Copy Location' name='document.copyLocation' cssClass="longfield"/>
    </p>

<p id="t-ident" class="clear">
    <span id="t-doi" tiplabel="DOI" tooltipcontent="Digital Object Identifier.">
        <@s.textfield labelposition='left' id='doi' label='DOI' name='document.doi' cssClass="shortfield" />
    </span>
    <span id="t-isbn" tiplabel="ISBN" tooltipcontent="International Standard Book Number.">
       <@s.textfield labelposition='left' id='isbn' title="please add a valid ISBN" label='ISBN' name='document.isbn' cssClass="right-shortfield  isbn" />
    </span>
    <span id="t-issn" tiplabel="ISSN" tooltipcontent="International Standard Serial Number, an eight-digit number assigned to many serial publications.">
        <@s.textfield labelposition='left' id='issn' title="please add a valid ISSN" label='ISSN' name='document.issn' cssClass="right-shortfield  issn" />
    </span>
</p>


</@edit.citationInfo>

<@edit.asyncFileUpload "${resource.resourceType}" true />



<@edit.sharedFormComponents />

</@s.form>

</div>

<@edit.sidebar />

<@edit.resourceJavascript includeAsync=true includeInheritance=true>

setupDocumentEditForm();

//certain fields in the "about your document" become extraneous upon selecting a new documentType value.  Clear the value of these fields prior to 
//submission
$(function(){
    $('#resourceMetadataForm').submit(function() {
        //assumption:  divAboutYourDocument has no radio buttons, select boxes, or checkboxes. This piece wont work if our assumption becomes false
        $(':input', '#divAboutYourDocument').filter(':hidden').val("");
    });
});




</@edit.resourceJavascript>
</body>
</#escape>
