<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit />
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view />
<head>
<#if resource.id == -1>
<title>Register a new ontology with tDAR</title>
<#else>
<title>Editing Ontology Metadata for ${resource.title} (tDAR id: ${resource.id?c})</title>
</#if>
<meta name="lastModifiedDate" content="$Id$"/>
<@edit.resourceJavascript>
    $('#fileInputTextArea').tabby();
    var totalNumberOfFiles = ${resource.getTotalNumberOfFiles()?c};

    //the ontology textarea or file upload field is required whenever it is visible AND
    //no ontology  rules are already present from a previous upload

    $('#resourceRegistrationDateCreated').rules("add", {
        rangelength: [4,4],
        messages: {
            digits: "Please enter a four digit number for the year.",
            rangelength: "The year should be four digits long."
        }
    });

    $('#fileInputTextArea').rules("add", {
        required: {depends:isFieldRequired},
        messages: {required: "No ontology data entered. Please enter ontology manually or upload a file."}
    });

    $('#resourceMetadataForm_uploadedFiles').rules("add", {
        required: {depends:isFieldRequired},
        messages: {required: "No ontology file selected. Please select a file or enter ontology data manually."}
    });
    
    function isFieldRequired(elem) {
        var noRulesExist =  !( (totalNumberOfFiles > 0) || 
        ($("#fileInputTextArea").val().length > 0 ) ||
        ($("#resourceMetadataForm_uploadedFiles").val().length > 0));
        return noRulesExist && $(elem).is(":visible");
    }

    refreshInputDisplay();
</@edit.resourceJavascript>

</head>
<body>
<@edit.toolbar "${resource.urlNamespace}" "edit" />
<div>

<@s.form id='resourceMetadataForm' method='post' action='save' enctype='multipart/form-data'>

<@edit.basicInformation>
<@s.textfield labelposition='left' id='resourceTitle' label='Title' title="A title is required for all ontologies" name='ontology.title' required='true' cssClass="longfield required" maxlength="512" />
<br/>
<span tiplabel="Year" tooltipcontent="Four digit year, e.g. 1966 or 2005. If your document does not have a date published, please leave this field blank">
<@s.textfield labelposition='left' id='resourceRegistrationDateCreated' label='Year Created' name='ontology.dateCreated' cssClass="reasonableDate" />
</span>

<p id="t-abstract" class="new-group">
    <@s.textarea id='resourceDescription' label='Abstract / Description' labelposition='top'rows='5' name='ontology.description'
        title="A basic description is required for all ontologies" cssClass='resizable required' required=true />
</p>

</@edit.basicInformation>

<#if (resource.getLatestVersions().size()>0)>
<div class="glide">
    <@view.ontology />
</div>
</#if>

<@edit.manualTextInput typeLabel="Ontology" type="ontology" />

<@edit.resourceCreators 'Ontology Creators' authorshipProxies 'authorship' />

<@edit.resourceNoteSection />

<@edit.fullAccessRights />

<@edit.submit  fileReminder=false />
</@s.form>
</div>
<div id="sidebar" parse="true">
    <div id="notice">
    <h3>Introduction</h3>
    This is the page editing form for a project.
    </div>
</div>
</body>
