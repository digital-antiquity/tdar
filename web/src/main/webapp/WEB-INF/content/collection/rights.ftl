<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "common-collection.ftl" as commonCollection>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
    <#if persistable.id == -1>
        <title>Create a Collection</title>
    <#else>
        <title>Rights &amp; Permissions: ${persistable.name}</title>
    </#if>
    <meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>

    <div id="sidebar-right" parse="true">
        <div id="notice">
            <h3>Introduction</h3>
            This is the editing form for a Collection.
        </div>
    </div>


    <h1>Rights &amp; Permissions: <span> ${persistable.name!"New Collection"}</span></h1>
        <@s.form name='metadataForm' id='metadataForm'  method='post' cssClass="form-horizontal tdarvalidate"  dynamicAttributes={"data-validate-method":"initBasicForm"} enctype='multipart/form-data' action='rights-save'>
        <@s.token name='struts.csrf.token' />
        <@common.jsErrorLog />
        <@s.hidden name="id" value="${persistable.id?c}" />
        <#if editor>
            <div class="control-group" id="divSubmitter">
                <label class="control-label">Owner</label>

                <div class="controls controls-row">
                    <#if owner?has_content>
                    <@edit.registeredUserRow person=owner isDisabled=disabled   _personPrefix="" _indexNumber=''
                        prefix="owner" includeRights=false includeRepeatRow=false />
	 	        <#else>
                    <@edit.registeredUserRow person=authenticatedUser isDisabled=disabled   _personPrefix="" _indexNumber=''
                        prefix="owner" includeRights=false includeRepeatRow=false />
                    </#if>
                </div>
            </div>
        </#if>

        <div id="divCollectionAccessRightsTips" style="display:none">
            <p>Determines who can edit a document or related metadata. Enter the first few letters of the person's last name.
                The form will check for matches in the ${siteAcronym} database and populate the related fields.</p>
            <em>Types of Permissions</em>
            <dl>
                <dt>View and Download</dt>
                <dd>User can view/download all file attachments associated with the resources in the collection.</dd>
                <dt>Modify Record
                <dt>
                <dd>User can edit the resources listed in the collection.
                <dd>
                <dt>Administer Collection
                <dt>
                <dd>User can edit resources listed in the collection, and also modify the contents of the collection.
                <dd>
            </dl>
        </div>
            <@edit.fullAccessRights tipsSelector="#divCollectionAccessRightsTips" label="Users who can View or Modify this Collection" type='collection' header=false />

            <@edit.submit fileReminder=false />
        </@s.form>

        <#noescape>
        <script type='text/javascript'>

            $(function () {
                'use strict';
                var form = $("#metadataForm")[0];
                TDAR.common.initEditPage(form);
        });
        </script>
        </#noescape>
        <@edit.personAutocompleteTemplate />
<div style="display:none"></div>
</body>
</#escape>
