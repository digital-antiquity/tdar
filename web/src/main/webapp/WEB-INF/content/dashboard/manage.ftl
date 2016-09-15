<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/search/search-macros.ftl" as search>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "dashboard-common.ftl" as dash />
    <#import "/${themeDir}/settings.ftl" as settings>

<head>
    <title>${authenticatedUser.properName}'s Dashboard</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />
</head>

<div id="titlebar" parse="true">
    <h1>Manage Rights &amp; Permissions</h1>
</div>
<div class="row">
    <div class="span2">
    <@dash.sidebar current="share" />
    </div>
    <div class="span10">
        <@shareSection />
        <@collectionsSection />
    </div>

</div>

    <#macro repeat num val>
        <#if (num > 0)>
            <@repeat (num-1) val /><#noescape>${val}</#noescape>
        </#if>
    </#macro>

    <#macro collectionsSection>

        <table class="table">
            <thead>
                <th>Name</th>
                <th># of resources</th>
                <th># of users</th>
                <th>action</th>
            </thead>
            <tbody>
            <#list allResourceCollections as collection>
                <tr>
                <td><a href="${collection.name}">${collection.name}</a></td>
                <td>${collection.resources?size}</td>
                <td>${collection.authorizedUsers?size}</td>
                <td>
                    <div class="btn-group">
                      <a class="btn" href="${collection.detailUrl}">Details</a>
                      <a class="btn" href="/collection/${collection.id?c}/edit">Edit</a>
                      <a class="btn">Delete</a>
                    </div>
                </td>
                </tr>
            </#list>
            </tbody>
        </table>

        <#if sharedResourceCollections?? && !sharedResourceCollections.empty >
        <div class="">
            <h2>Collections Shared With You</h2>
            <@common.listCollections collections=sharedResourceCollections />
        </div>
        </#if>

        <table class="table">
            <thead>
            	<tr>
                <th>Id</th>
                <th>Name</th>
                <th># of users</th>
                <th>action</th>
			</tr>
            </thead>
            <tbody>
            <#list internalCollections![] as collection>
                <#if (collection.resources?size > 0 )>
					<#list collection.resources as resource>
                        <tr>
                        <td>${resource.id?c}</td>
                        <td>
        						<a href="${resource.detailUrl}">${resource.title}</a></td>
                        <td>${collection.authorizedUsers?size}</td>
                        <td>edit | delete | details</td>
                        </tr>
                    </#list>
                </#if>
            </#list>
            </tbody>
        </table>

    </#macro>



<#macro shareSection>
    <form class="form-horizontal">
    <div class="well">
    <div class="row">
        <div class="span8">
            <h4>Share:</h4>
        
            <div class="control-group">
                        <select name="type" id="what" class="control-label">
                            <option value="RESOURCE">a resource</option>
                            <option value="SHARE">resources from</option>
                        </select>
                        <div class="controls">
                          <input type="text" id="autocomplete" placeholder="Search" class="input-xxlarge">
                        </div>
            </div>
        </div>
        </div>
        <div class="row">
            <div class="span4">
                    <div class="control-group">
                         <label class="control-label" for="inputEmail">With:</label>
                         <div  class="controls"><input type="text" id="inputEmail" placeholder="Email"></div>
                </div>
            </div>
            
              <div class="span4">
                  <div class="control-group">
                        <label class="control-label" for="inputPassword">Permission:</label>
                        <div class="controls">
                            <select name="authorizedUsers[0].generalPermission" id="metadataForm_authorizedUsers_0__generalPermission" class="creator-rights-select span3">
                                <option value="VIEW_ALL" selected="selected">View and Download</option>
                                <option value="MODIFY_METADATA">Modify Metadata</option>
                                <option value="MODIFY_RECORD">Modify Files &amp; Metadata</option>
                                <option value="ADMINISTER_GROUP">Add/Remove Items from Collection</option>
                            </select>
                        </div>
                  </div>
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
              <button class="btn tdar-button btn-primary" type="button">Submit</button>
           </div>
    
        </div>
    </div>
</form>

</#macro>
            <script>
            $(document).ready(function() {
                var picker = $('.datepicker').datepicker();
                picker.on('changeDate', function(ev){
                    $(ev.target).datepicker('hide');
                });
                TDAR.notifications.init();
                TDAR.common.collectionTreeview();
            });
            </script>



</#escape>
