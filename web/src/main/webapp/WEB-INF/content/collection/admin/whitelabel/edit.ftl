<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "../../common-collection.ftl" as commonCollection>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
    <#if persistable.id == -1>
        <title>Create a Collection</title>
    <#else>
        <title>Editing: ${persistable.name}</title>
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


    <h1><#if persistable.id == -1>Creating<#else>Editing</#if>: <span> ${persistable.name!"New Collection"}</span></h1>
        <@s.form name='metadataForm' id='metadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='save'>
        <@s.token name='struts.csrf.token' />
        <@common.jsErrorLog />
        <h2>Private Label  Settings</h2>

        <div class="" id="basicInformationSection" data-tiplabel="Basic Information"
             data-tooltipcontent="Enter a name and description for this collection.  You may also choose a &quot;parent
    collection&quot; which allows you to inherit all of the access permissions defined by the parent.">
            <#if collection.id?? &&  collection.id != -1>
                <@s.hidden name="id"  value="${collection.id?c}" />
            </#if>
            <@edit.hiddenStartTime />
            <div class="row">
            <div class="span4">
                <div class="control-group">
                    <label class="control-label">Enable Custom Header?</label>
    
                    <div class="controls">
                        <label for="rdoheaderTrue" class="radio inline"><input type="radio" id="rdoheaderTrue" name="collection.properties.customHeaderEnabled"
                                                                                value="true" <@common.checkedif collection.properties.customHeaderEnabled true /> />Yes</label>
                        <label for="rdoheaderFalse" class="radio inline"><input type="radio" id="rdoheaderFalse" name="collection.properties.customHeaderEnabled"
                                                                                 value="false" <@common.checkedif collection.properties.customHeaderEnabled false /> />No</label>
                    </div>
                </div>
    
    
                <div class="control-group">
                    <label class="control-label">Enable Custom Document Logo?</label>
                    <i>Requires logo file in the filestore/collection/.../logo_lg.jpg</i>
                    <div class="controls">
                        <label for="rdodocTrue" class="radio inline"><input type="radio" id="rdodocTrue" name="collection.properties.customDocumentLogoEnabled"
                                                                                value="true" <@common.checkedif collection.properties.customDocumentLogoEnabled true /> />Yes</label>
                        <label for="rdodocFalse" class="radio inline"><input type="radio" id="rdodocFalse" name="collection.properties.customDocumentLogoEnabled"
                                                                                 value="false" <@common.checkedif collection.properties.customDocumentLogoEnabled false /> />No</label>
                    </div>
                </div>
    
    
                <div class="control-group">
                    <label class="control-label">Show Featured Resources?</label>
                    <i>Requires manual SQL to add /modify featured resources</i>
    
                    <div class="controls">
                        <label for="rdofeatTrue" class="radio inline"><input type="radio" id="rdofeatTrue" name="collection.properties.featuredResourcesEnabled"
                                                                                value="true" <@common.checkedif collection.properties.featuredResourcesEnabled true /> />Yes</label>
                        <label for="rdofeatFalse" class="radio inline"><input type="radio" id="rdofeatFalse" name="collection.properties.featuredResourcesEnabled"
                                                                                 value="false" <@common.checkedif collection.properties.featuredResourcesEnabled false /> />No</label>
                    </div>
                </div>

            </div>
            <div class="span4">
    
                <div class="control-group">
                    <label class="control-label">Show Search on Collection Page?</label>
    
                    <div class="controls">
                        <label for="rdosearchTrue" class="radio inline"><input type="radio" id="rdosearchTrue" name="collection.properties.searchEnabled"
                                                                                value="true" <@common.checkedif collection.properties.searchEnabled true /> />Yes</label>
                        <label for="rdosearchFalse" class="radio inline"><input type="radio" id="rdosearchFalse" name="collection.properties.searchEnabled"
                                                                                 value="false" <@common.checkedif collection.properties.searchEnabled false /> />No</label>
                    </div>
                </div>
    
                <div class="control-group">
                    <label class="control-label">List Subcollections on Page?</label>
    
                    <div class="controls">
                        <label for="rdosubcollTrue" class="radio inline"><input type="radio" id="rdosubcollTrue" name="collection.properties.subCollectionsEnabled"
                                                                                value="true" <@common.checkedif collection.properties.subCollectionsEnabled true /> />Yes</label>
                        <label for="rdosubcollFalse" class="radio inline"><input type="radio" id="rdosubcollFalse" name="collection.properties.subCollectionsEnabled"
                                                                                 value="false" <@common.checkedif collection.properties.subCollectionsEnabled false /> />No</label>
                    </div>
                </div>
            </div>
            </div>
            <@s.textfield name="collection.properties.subtitle" label="Subtitle" cssClass="descriptiveTitle input-xxlarge"/>

            <@s.textarea cols=80 rows=20 name="collection.properties.css" label="WhiteLabel CSS" />

    
        </div>



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
<div style="display:none"></div>
</body>
</#escape>
