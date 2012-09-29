<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
<#if resource.id == -1>
<title>Register a New Coding Sheet With tDAR</title>
<#else>
<title>Editing Coding Sheet Metadata for ${resource.title} (tDAR id: ${resource.id?c})</title>
</#if>
<meta name="lastModifiedDate" content="$Id$"/>

<@edit.resourceJavascript formId="#resourceRegistrationForm" selPrefix="#resourceRegistration">
    $('#resourceRegistrationDateCreated').rules("add", {
        rangelength: [4,4],
        messages: {
            digits: "Please enter a four digit number for the year.",
            rangelength: "The year should be four digits long."
        }
    });
    $.ajaxSetup({ 
        cache: false 
    });
    
    var totalNumberOfFiles = ${codingSheet.getTotalNumberOfFiles()?c};

    //the coding rule textarea or file upload field is required whenever it is visible AND
    //no cocding sheet rules are already present from a previous upload

    $('#fileInputTextArea').rules("add", {
        required: {depends:isFieldRequired},
        messages: {required: "No coding rules entered. Please enter them manually or upload a coding rule file."}
    });

    $('#resourceRegistrationForm_uploadedFiles').rules("add", {
        required: {depends:isFieldRequired},
        messages: {required: "No coding rule file selected. Please select a file or upload coding rules manually."}
    });
    
    function isFieldRequired(elem) {
        var noRulesExist =  !( (totalNumberOfFiles > 0) || 
        ($("#fileInputTextArea").val().length > 0 ) ||
        ($("#resourceRegistrationForm_uploadedFiles").val().length > 0));
        return noRulesExist && $(elem).is(":visible");
    }


    refreshInputDisplay();
</@edit.resourceJavascript>

</head>
<body>

<@edit.toolbar "${resource.urlNamespace}" "edit" />

<@s.form name='resourceRegistrationForm' id='resourceRegistrationForm' method='post' enctype='multipart/form-data' action='save'>

<@edit.basicInformation>
<span
    tiplabel="Title"
    tooltipcontent="Enter the entire title, including sub-title, if appropriate."
>
<@s.textfield labelposition='left' id="resourceRegistrationTitle" label='Title' title="A title is required for all coding sheets" name='codingSheet.title' cssClass="required longfield" required=true maxlength="512"/>
</span>
<br/>
<span tiplabel="Year" tooltipcontent="Four digit year, e.g. 1966 or 2005. If your document does not have a date published, please leave this field blank">
<@s.textfield labelposition='left' id='resourceRegistrationDateCreated' label='Year Created' name='codingSheet.dateCreated' cssClass="reasonableDate" />
</span>
<p id="t-abstract" class="new-group"
    tiplabel="Abstract / Description"
    tooltipcontent="Short description of the resource. Often comes from the resource itself, but sometimes will include additional information from the contributor.">
    <@s.textarea label='Abstract / Description' labelposition='top' id="resourceRegistrationDescription" rows='5' 
        title="A basic description is required for all coding sheets" name='codingSheet.description' cssClass="resizable required" required=true />
</p>
</@edit.basicInformation>

<div class="glide">
<@view.codingRules />
</div>

<@edit.manualTextInput typeLabel="Coding Sheet" type="coding" />

<@edit.resourceCreators 'Coding Sheet Creators' authorshipProxies 'authorship' />

<@edit.resourceNoteSection />

<@edit.fullAccessRights /> 

<@edit.submit  fileReminder=false  />
</@s.form>

<div id="sidebar" parse="true">
    <div id="notice">
    <h3>Introduction</h3>
    This is the page editing form for a project.
    </div>
</div>

</body>
