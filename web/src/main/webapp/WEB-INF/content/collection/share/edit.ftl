<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/search/search-macros.ftl" as search>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/${themeDir}/settings.ftl" as settings>

<head>
    <title>Invite a user to edit this collection</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />
</head>

<div id="titlebar" parse="true">
    <h1>Invite to Edit: ${resourceCollection.title}</span></h1>
</div>
<div class="row">
    <div class="span12">
        <@shareSection />
    </div>
</div>

    <#macro shareSection>
    <form class="form-horizontal" method="POST" action="/collection/share/save?id=${id?c}">
        <div class="well">
            <div class="row">
                <div class="span4">
                    <@s.textfield name="adhocShare.email" id="txtShareEmail" label="Email" labelPosistion="left" />
                </div>

                <div class="span4">
                    <@s.select name="adhocShare.generalPermissions" label="Permission" labelposition="left" listValue='label' list="%{availablePermissions}" />
                </div>
            </div>
            <div class="row">
                <div class="span5">
                    <div class="control-group">
                        <label class="control-label" for="inputPassword">Until:</label>
                        <div class="controls">
                            <div class="input-append">
                                <input class="span2 datepicker" size="16" type="text" value="12-02-2016" id="dp3" data-date-format="mm-dd-yyyy" >
                                <span class="add-on"><i class="icon-th"></i></span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="span3">
                    <input type="submit" class="btn tdar-button btn-primary" value="Submit">
                </div>

            </div>
        </div>
    </form>

    </#macro>

    <@edit.personAutocompleteTemplate />

<div id="customIncludes" parse="true">
    <script src="/js/tdar.manage.js"></script>
</div>


</#escape>
