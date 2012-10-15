<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit />
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view />
<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Id$"/>
</head>
<body>
<@edit.subNavMenu />
<@edit.toolbar "${resource.urlNamespace}" "edit" />
<div>

<@s.form id='resourceMetadataForm' method='post' action='save' enctype='multipart/form-data' cssClass="form-horizontal">

<@edit.basicInformation "ontology" "ontology"/>
<@edit.citationInfo "ontology" />
<@edit.organizeResourceSection />

<#if (resource.latestVersions?has_content )>
<div class="glide">
    <@view.ontology />
</div>
</#if>
<div class="glide">
<h3>Categorize this Ontology</h3>
         <@edit.categoryVariable />

</div>
<@edit.manualTextInput typeLabel="Ontology" type="ontology" />

<@edit.allCreators 'Ontology Creators' authorshipProxies 'authorship' />

<@edit.resourceNoteSection />

<@edit.fullAccessRights />

<@edit.submit  fileReminder=false />
</@s.form>
</div>
<@edit.sidebar />

<@edit.resourceJavascript includeInheritance=true>
    $(function() {
        setupSupportingResourceForm(${resource.getTotalNumberOfFiles()?c}, "ontology");
        $('#fileInputTextArea').tabby();
    });
</@edit.resourceJavascript>
</body>
</#escape>
