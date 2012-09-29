<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
<#if resource.id == -1>
<title>Create a Document</title>
<#else>
<title>Editing: ${resource.title}</title>
</#if>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>
<@edit.toolbar "${resource.urlNamespace}" "edit" />
<div>
<@s.form id='resourceMetadataForm' method='post' enctype='multipart/form-data' action='save'>
<@edit.basicInformation 'document' >
<#if linkedInformationResource??>
<div class='info'>
This will be linked as a <b>${linkType}</b> citation for: <b>${linkedInformationResource.title}</b>
<@s.hidden name='linkType' value='${linkType}'/>
<@s.hidden name='linkedResourceId' value='${linkedInformationResource.id?c}'/>
</div>
<!--br/-->
</#if>
<p id="t-title" tooltipcontent="Enter the entire title, including sub-title, if appropriate." tiplabel="Title"> 
    <@s.textfield labelposition='left' id='resourceTitle' label='Title' required='true' name='document.title' size="75" cssClass="required descriptiveTitle longfield"  title="A title is required for all documents." maxlength="512" />
</p>

<p id="t-year" >
    <span tooltipcontent="Four digit year, e.g. 1966 or 2005. " tiplabel="Year">
      <#assign dateVal = ""/>
       <#if resource.dateCreated?? && resource.dateCreated != -1>
         <#assign dateVal = resource.dateCreated?c />
      </#if>
        <@s.textfield labelposition='left' id='dateCreated' label='Year' name='resource.dateCreated' value="${dateVal}" cssClass="shortfield reasonableDate required" required=true
         title="Please enter the year this document was created" />
    </span>
</p>

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

<p id="t-abstract" class="clear"
    tiplabel="Abstract / Description"
    tooltipcontent="Short description of the document. Often comes from the document itself, but sometimes will include additional information from the contributor."
    >
    <@s.textarea label='Abstract / Description' labelposition='top' id='resourceDescription'  name='resource.description' rows="5" cssClass='required resizable tdartext' required=true title="A description is required" />
</p>


</@edit.basicInformation>

<@edit.asyncFileUpload "Document" true />

<@edit.resourceCreators "Authors / Editors" authorshipProxies 'authorship' false />

<div class="glide">
<h3>About Your Document</h3>

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

<p>
    <span tiplabel="Language" tooltipcontent="Select the language in which the document is written.">
        <@s.select labelposition='left' label='Language'  emptyOption='false' name='resourceLanguage'  listValue="label" list='%{languages}' cssClass="right-shortfield "/>
    </span>
</p>

<p 
    tiplabel="URL"
    tooltipcontent="Uniform Resource Locator (Web address).">
    <@s.textfield labelposition='left' id='url' label='URL' name='document.url' cssClass='longfield url' />
</p>

<p> 
    <h3>Complete the Citation for your Document</h3> 
    <p class="comment" id="showCite">add publisher, edition information, etc... 
    <button type="button" id="link-more" name="show" value="show fields">show fields</button>
</p> 

<div id="showmorecite"> 
        <span id="publisher-hints" style="display:none"
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

    <p id="t-edition">
        <@s.textfield labelposition='left' id='edition' label='Edition' name='document.edition' cssClass="shortfield" />
    </p>
    
    <p id="t-vol">
        <@s.textfield labelposition='left' id='volume' label='Volume' name='document.volume' cssClass="shortfield"  />
        <@s.textfield labelposition='left' id='journalNumber' label='Issue Number' name='document.journalNumber' cssClass="right-shortfield"  />
    </p>
    
    <p id='t-series'>
        <@s.textfield labelposition='left' id='seriesName' label='Series Title' name='document.seriesName' cssClass="longfield" />
        <br />
        <@s.textfield labelposition='left' id='seriesNumber' label='Series #' name='document.seriesNumber' cssClass="shortfield" />
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
        
    <p id="t-located"  tooltipcontent="Actual physical location of a copy of the document, e.g. an agency, repository, 
        or library." tiplabel="Copy Location">
        <@s.textfield labelposition='left' id='copyLocation' label='Copy Location' name='document.copyLocation' cssClass="longfield"/>
    </p>
        
 </div>
</div>


<@edit.sharedFormComponents />

</@s.form>

</div>

<@edit.sidebar />

<@edit.resourceJavascript includeAsync=true includeInheritance=true>

setupDocumentEditForm();
</@edit.resourceJavascript>
</body>
