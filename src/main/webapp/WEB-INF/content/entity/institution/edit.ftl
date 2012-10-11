<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
    <#if (id > 0 )>
        <title>Editing ${institution.name}</title>
    <#else>
        <title>Add a new Institution</title>
    </#if>
    
    <script type="text/javascript">
        $(function() {
            initializeView();
            TDAR.common.initEditPage($('#frmInstitution')[0]);
        });
    </script>
</head>
<body>
    <@nav.creatorToolbar "edit" />

    <@s.form  name="institutionForm" id="frmInstitution" method='post' enctype='multipart/form-data' action='save'>
    <div class="glide">
        <h3>Institution Information</h3>
        <@s.hidden name="id" />
        <@s.textfield name="institution.name" required=true label="Name" id="txtInstitutionName" cssClass="longfield" />
        <br /><@s.textfield name="institution.location" label="Location" id="txtLocation" cssClass="longfield" />
        <br /><@s.textfield name="institution.url" label="Website" id="txtUrl" cssClass="longfield url" />
    </div>
    <@edit.submit "Save" false />    

    </@s.form>
</body>
</#escape>
