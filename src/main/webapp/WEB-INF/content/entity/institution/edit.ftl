<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
    <#if ((institution.id)?has_content && institution.id > 0 )>
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

    <@s.form  name="institutionForm" id="frmInstitution"  cssClass="form-horizontal" method='post' enctype='multipart/form-data' action='save'>
    <div class="glide">
        <h1>Institution Information for: ${institution.name}</h1>
        <@s.hidden name="id" />
        <@s.textfield name="institution.name" required=true label="Name" id="txtInstitutionName" cssClass="input-xlarge"  maxlength=255 />

		<#if editor>    
        <div id="spanStatus" tooltipcontent="#spanStatusToolTip" class="control-group">
            <label class="control-label">Status</label>
            <div class="controls">
                <@s.select theme="tdar" value="institution.status" name='status'  emptyOption='false' listValue='label' list='%{statuses}'/>
            </div>  
        </div>
		</#if>

        <br /><@s.textfield name="institution.location" label="Location" id="txtLocation" cssClass="input-xlarge"  maxlength=255 />
        <br /><@s.textfield name="institution.url" label="Website" id="txtUrl" cssClass="input-xlarge url"  maxlength=255 />
        <br /><@s.textarea name="institution.description" label="Description" cssClass="input-xxlarge" />
    </div>
    <@edit.submit "Save" false />    

    </@s.form>
</body>
</#escape>
