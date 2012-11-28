<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit />
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view />
<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Id$"/>
</head>
<body>
<@edit.sidebar />
<@edit.subNavMenu />
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
<@edit.manualTextInput 
    typeLabel="Ontology" 
    type="ontology"
    uploadOptionText="Upload an OWL file" 
    manualEntryText="Manually enter your ontology into a textarea"; _divid>

    <#if _divid=="upload">    
    <p class="help-block">
        We currently support uploads of <a class='external' href='http://www.w3.org/TR/owl2-overview/'>OWL XML/RDF files</a>.  
        You can create OWL files by hand (difficult) or with a tool like <a    class='external' href='http://protege.stanford.edu/'>the
        Prot&eacute;g&eacute; ontology editor</a>.  Alternatively, choose the <em>Submit as: Manually enter your ontology</em> option above and 
        enter your ontology  into a text area.  
    </p>
    <#elseif _divid=="manualEntry">
        <p class="help-block">
            You can enter your ontology in the text area below.  Separate each concept in
            your ontology with newlines (hit enter), and indicate parent-child relationships
            with tabs (make sure you use the tab key on your keyboard - spaces do not work).
            To specify synonyms for a given term use comma-separated parentheses, e.g.,
        </p>
        <blockquote>
            <p><code>Flake (Debris, Debitage)</code></p>
        </blockquote> 
        
        <p class="help-block">For lithic form, the following would be a simple ontology:</p>
        <pre>
Tool
    Projectile Point
    Scraper (Grattoir)
        End Scraper
        Side Scraper
    Other Tool
Flake (Debris, Debitage)
    Utilized
    Unutilized
Core
        </pre>
    </#if>
</@edit.manualTextInput>

<@edit.allCreators 'Ontology Creators' authorshipProxies 'authorship' />

<@edit.resourceNoteSection />

<@edit.fullAccessRights />

<@edit.submit  fileReminder=false />
</@s.form>
</div>

<@edit.resourceJavascript includeInheritance=true>
    $(function() {
        setupSupportingResourceForm(${resource.getTotalNumberOfFiles()?c}, "ontology");
        $('#fileInputTextArea').tabby();
    });
</@edit.resourceJavascript>
</body>
</#escape>
