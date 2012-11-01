<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Id$"/>

</head>
<body>
<@edit.sidebar />
<@edit.subNavMenu />


<@s.form name='resourceRegistrationForm' id='resourceRegistrationForm' method='post' enctype='multipart/form-data' action='save' cssClass="form-horizontal">

<@edit.basicInformation "coding sheet" "codingSheet"/>
<@edit.citationInfo "codingSheet" />

    <span class="hidden" id="ontologyToolTip">
        If you would like to link this column to a ${siteAcronym} ontology, make that selection here. This is important if you (or other researchers) intend to integrate this dataset with other datasets using the ${siteAcronym} data integration tool. 
    </span>

     <div id='divOntology' class="glide ontologyInfo" tooltipcontent="#ontologyToolTip" tiplabel="Ontology" >
     
         <@edit.categoryVariable />
     
     
     <!-- FIXME: This screams to be refactored into a common format; one issue in a cursory try though is the ability to do the assignments of the defaultId and Text-->
        <h4>Map To an Ontology</h4>
            <#assign ontologyId="" />
            <#assign ontologyTxt="" />
            <#if ontology??  && ontology.id??>
                <#assign ontologyId=ontology.id?c />
                <#assign ontologyTxt="${ontology.title} (${ontology.id?c})"/>
            </#if>
            <@s.hidden name="ontology.id" value="${ontologyId}" id="oid" />
            <@edit.combobox name="ontology.title"
                label="Ontology Name" 
                target="#divOntology"
                 value="${ontologyTxt}"  
                 placeholder="Enter the name of an Ontology"
                 autocompleteParentElement="#divOntology"
                 autocompleteIdElement="#oid"
                 cssClass="longfield ontologyfield" />
                 
             <div class="control-group">
                <div class="controls">
                    <span class="help-block">
                        Alternately, you can create a new ontology by pressing the button below. TDAR will start the process in a new window, and return here when you are finished.
                    </span>
                    
                        
                    <a class="btn btn-small"target="_blank" 
                        onclick="setAdhocTarget(this);" 
                        href='<@s.url value="/ontology/add?returnToResourceMappingId=${resource.id?c}"/>'>Create An Ontology</a>
                </div>
             </div>
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


<@edit.resourceJavascript formSelector="#resourceRegistrationForm" selPrefix="#resourceRegistration" includeInheritance=true>
    $(function() {
        var $form = $("#resourceRegistrationForm");
        setupSupportingResourceForm(${codingSheet.getTotalNumberOfFiles()?c}, "coding sheet");
        applyComboboxAutocomplete($('input.ontologyfield', $form), "ONTOLOGY");
    });
    
</@edit.resourceJavascript>

</body>
</#escape>