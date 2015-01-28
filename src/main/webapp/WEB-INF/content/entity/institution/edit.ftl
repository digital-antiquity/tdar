<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/WEB-INF/content/entity/entity-edit-common.ftl" as entityEdit>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
    <#if ((institution.id)?has_content && institution.id > 0 )>
        <title>Editing ${institution.name}</title>
    <#else>
        <title>Add a new Institution</title>
    </#if>

</head>
<body>

    <@s.form  name="institutionForm" id="frmInstitution"  cssClass="form-horizontal" method='post' enctype='multipart/form-data' action='save'>
        <@s.token name='struts.csrf.token' />
        <@common.jsErrorLog />
    <div class="glide">
        <h1>Institution Information for: ${institution.name}</h1>
        <@s.hidden name="id" />
        <@s.textfield name="name" required=true label="Name" id="txtInstitutionName" cssClass="input-xlarge"  maxlength=255 />

        <#if editor>
            <div id="spanStatus" data-tooltipcontent="#spanStatusToolTip" class="control-group">
                <label class="control-label">Status</label>

                <div class="controls">
                    <@s.select theme="tdar" value="institution.status" name='status'  emptyOption='false' listValue='label' list='%{statuses}'/>
                </div>
            </div>
        </#if>

        <@entityEdit.uploadForm />

        <@s.textfield name="institution.url" label="Website" id="txtUrl" cssClass="input-xlarge url"  maxlength=255 />

        <@s.textfield name="institution.email" label="Email" id="txtEmail" cssClass="input-xlarge email"  maxlength=255 />

        <@s.textarea name="institution.description" label="Description" cssClass="input-xxlarge" />

        <h3>Address List</h3>
        <@common.listAddresses entity=institution entityType="institution" />

        <@entityEdit.hidden />


    </div>
        <@edit.submit "Save" false />

    </@s.form>
    <script type="text/javascript">
        $(function () {
            TDAR.common.initEditPage($('#frmInstitution')[0]);
            TDAR.common.validateProfileImage();
        });
    </script>
</body>
</#escape>
