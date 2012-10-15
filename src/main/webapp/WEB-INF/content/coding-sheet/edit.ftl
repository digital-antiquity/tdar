<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Id$"/>

</head>
<body>
<@edit.subNavMenu />

<@edit.toolbar "${resource.urlNamespace}" "edit" />

<@s.form name='resourceRegistrationForm' id='resourceRegistrationForm' method='post' enctype='multipart/form-data' action='save' cssClass="form-horizontal">

<@edit.basicInformation "coding sheet" "codingSheet"/>
<@edit.citationInfo "codingSheet" />

    <span class="hidden" id="ontologyToolTip">
        If you would like to link this column to a ${siteAcronym} ontology, make that selection here. This is important if you (or other researchers) intend to integrate this dataset with other datasets using the ${siteAcronym} data integration tool. 
    </span>

     <div id='divOntology' class="glide ontologyInfo" tooltipcontent="#ontologyToolTip" tiplabel="Ontology" >
     
         <@edit.categoryVariable />
     
     
     <!-- FIXME: This screams to be refactored into a common format; one issue in a cursory try though is the ability to do the assignments of the defaultId and Text-->
        <b>Map it to an Ontology:</b><br/>
            <#assign ontologyId="" />
            <#assign ontologyTxt="" />
            <#if ontology??  && ontology.id??>
                <#assign ontologyId=ontology.id?c />
                <#assign ontologyTxt="${ontology.title} (${ontology.id?c})"/>
            </#if>
            <@s.hidden name="ontology.id" value="${ontologyId}" id="oid" />
            <@s.textfield name="ontology.title" target="#divOntology"
                 value="${ontologyTxt}"  
                 watermark="Enter the name of an Ontology"
                 autocompleteParentElement="#divOntology"
                 autocompleteIdElement="#oid"
                 cssClass="longfield ontologyfield" />
            <div class="down-arrow"></div>
            <small><a target="_blank" onclick="setAdhocTarget(this);" href='<@s.url value="/ontology/add?returnToResourceMappingId=${resource.id?c}"/>'>Create Ontology</a> </small>
    </div>

<@edit.organizeResourceSection />



<div class="glide">
<@view.codingRules />
</div>
<@edit.manualTextInput typeLabel="Coding Sheet" type="coding" />

<@edit.allCreators 'Coding Sheet Creators' authorshipProxies 'authorship' />

<@edit.resourceNoteSection />

<@edit.fullAccessRights />

<@edit.submit  fileReminder=false  />
</@s.form>

<@edit.sidebar />

<@edit.resourceJavascript formSelector="#resourceRegistrationForm" selPrefix="#resourceRegistration" includeInheritance=true>
    $(function() {
        setupSupportingResourceForm(${codingSheet.getTotalNumberOfFiles()?c}, "coding sheet");
        $(formId).delegate(".down-arrow", "click",autocompleteShowAll);
        $(formId).delegate('input.ontologyfield',"focusin", function() {
            applyResourceAutocomplete($('input.ontologyfield'), "ONTOLOGY");
        });
    });
    
</@edit.resourceJavascript>

</body>
</#escape>