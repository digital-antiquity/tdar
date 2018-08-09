<#escape _untrusted as _untrusted?html>
    <#global itemPrefix="document"/>
    <#global inheritanceEnabled=true />
    <#global multipleUpload=true />
    <#global hideRelatedCollections=true/>



    <#macro basicInformation>
        <#if linkedInformationResource??>
        <div class='help-block'>
            This will be linked as a <b>${linkType}</b> citation for: <b>${linkedInformationResource.title}</b>
            <@s.hidden name='linkType' value='${linkType}'/>
            <@s.hidden name='linkedResourceId' value='${linkedInformationResource.id?c}'/>
        </div>
        </#if>
    </#macro>


    <#macro citationInformationToggle>
        <div data-tiplabel="Document Type" data-tooltipcontent="Select the document type. Appropriate citation fields will be displayed below." class="doctype">
            <@s.radio name='document.documentType' emptyOption='false' listValue="label" inline=true
            list='%{documentTypes}' label="Document Type"  labelposition="left"/>
        </div>

        <div class="doctypeToggle thesis" id="t-degree">
            <@s.radio name='document.degree' label="Degree" emptyOption='false' listValue="label"  list='%{degrees}' labelposition="left"/>
        </div>

    </#macro>

    <#macro citationInformation>


    <div data-tiplabel="Additional Title" data-tooltipcontent="Enter the title of the book, report, or journal this document is part of"
         class="journal_article doctypeToggle" id="t-journal-name">
        <@s.textfield label="Journal Title" id='journalName'
        title="A journal title is required"  maxlength=255
        name='document.journalName' cssClass="requiredIfVisible tdartext " labelposition="left" />
    </div>

    <div class="doctypeToggle book_section" data-tooltipcontent="Enter the title of the book, report, or journal this document is part of."
         data-tiplabel="Additional Title" id="t-book-title">
        <@s.textfield label="Book Title" id='bookTitle' title="A book title is required" name='document.bookTitle' cssClass="requiredIfVisible tdartext" labelposition="left"  maxlength=255 />
    </div>


    <div data-tiplabel="Language" data-tooltipcontent="Select the language in which the document is written.">
        <@s.select label='Language'  emptyOption='false' name='resourceLanguage'  listValue="label" list='%{languages}' labelposition="left" cssClass="col-5" />
    </div>

    <div class="doctypeToggle journal_article other report form-group row" id="t-vol">
        <label class="col-form-label col-2">Volume Information</label>

        <div class="col-10">
        <div class="controls form-row">
            <@s.textfield id='volume'  placeholder='Volume' name='document.volume' cssClass="col-2 mr-2"   maxlength=255 />
        <@s.textfield id='journalNumber'placeholder='Issue Number' name='document.journalNumber' cssClass="col-2"  maxlength=255 />
        </div>
        </div>
    </div>

    <div class='doctypeToggle book_section book other report form-group row' id="t-series">
        <label class="col-2 col-form-label">Series Information</label>

        <div class="col-10">
        <div class="controls form-row">
            <@s.textfield id='seriesName' placeholder='Series Title' name='document.seriesName' cssClass="col-7 mr-2"  maxlength=255 />
            <@s.textfield id='seriesNumber' placeholder='Series #' name='document.seriesNumber' cssClass="col-2 mr-2"  maxlength=255 />
            <@s.textfield id='edition' placeholder='Edition' name='document.edition' cssClass="col-2"  maxlength=255 />
        </div>
        </div>
    </div>

    <div class="doctypeToggle book_section journal_article other report" id="t-start-end">
        <div class="form-group row">
            <label class="col-2 col-form-label">Start/End Page</label>

        <div class="col-10">
            <div class="controls form-row">
                <@s.textfield  id='startPage' placeholder="Start#" name='document.startPage' cssClass="col-2" maxlength=9/>
                <@s.textfield id='endPage'  placeholder="End#" name='document.endPage' cssClass="col-2" maxlength=9 />
            </div>
            </div>
        </div>
    </div>

    <div data-tooltipcontent="Actual physical location of a copy of the document, e.g. an agency, repository, or library." data-tiplabel="Copy Location">
        <@s.textfield id='copyLocation' label='Copy Location' name='document.copyLocation' cssClass="input-xxlarge"  maxlength=255 labelposition="left" />
    </div>

    <div id="t-isbn" placeholder="XXXX-XXXX" data-tiplabel="ISBN" data-tooltipcontent="International Standard Book Number."
         class="book_section book other report doctypeToggle">
        <@s.textfield labelposition='left' id='isbn' title="please add a valid ISBN" label='ISBN' name='document.isbn' cssClass="isbn "  maxlength=25 />
    </div>

    <div id="t-issn" placeholder="XXXX-XXXX" data-tiplabel="ISSN"
         data-tooltipcontent="International Standard Serial Number, an eight-digit number assigned to many serial publications."
         class="journal_article doctypeToggle">
        <@s.textfield labelposition='left' id='issn' title="please add a valid ISSN" label='ISSN' name='document.issn' cssClass="issn journal_article"  maxlength=25 />
    </div>
    </#macro>

    <#macro localJavascript>
    TDAR.common.setupDocumentEditForm();
    </#macro>

</#escape>
