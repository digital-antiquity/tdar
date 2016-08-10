<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/search/search-macros.ftl" as search>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/${themeDir}/settings.ftl" as settings>

<head>
    <title>${authenticatedUser.properName}'s Dashboard</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />
</head>

<div id="titlebar" parse="true">
    <h1>Manage Rights &amp; Permissions</h1>
</div>
    <form class="form">
<div class="row">
<div class="span12">
    <h4>Grant Access</h4>
    <div class="well">
    <div class="row">
        <div class="span6">
        <div class="row">
          <div class="control-group span3">
            <label class="control-label" for="inputEmail">User or email:</label>
            <div class="controls">
              <input type="text" id="inputEmail" placeholder="Email">
            </div>
          </div>
          <div class="control-group span3">
            <label class="control-label" for="inputPassword">Permission</label>
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
    <div class="span5">

          <div class="control-group span4">
            <label class="control-label" for="inputPassword">Expire?</label>
            <div class="controls">
                <div class="input-append">
                  <input class="span2 datepicker" size="16" type="text" value="12-02-2016" id="dp3" data-date-format="mm-dd-yyyy" >
                  <span class="add-on"><i class="icon-th"></i></span>
                </div>
            </div>
         </div>


    </div>
          </div>
    <div class="row">
        <div class="span12">
    <h5>To what:</h5>
    
   
        <div id="authorshipRow_0_" class="repeat-row control-group">
            <!-- fixme: careful with this styling -->
            <div class="control-label">
                <div class="btn-group creator-toggle-button" data-toggle="buttons-radio">
                    <button type="button" class="btn btn-small personButton btn-active active" data-toggle="button">Resource</button>
                    <button type="button" class="btn btn-small institutionButton " data-toggle="button">Collection</button>
            </div>
            <div class="controls controls-row">
                <div class="span6">
                    <div id="authorshipProxiesRow_0_p" class="creatorPerson active ">
                       <div class="controls-row">
                        <@common.combobox name="resource" target="#columnDiv_0"
                                placeholder="select a resource"
                                autocompleteParentElement="#divResource_0"
                                autocompleteIdElement="#resource_0_oid"
                                cssClass="input-xxlarge-combo " />
                            <hidden name="resource.id" id="resource_0_oid" />
                        </div>
                    </div>
                <div id="authorshipProxiesRow_0_i" class="creatorInstitution hidden">
                      <div class="controls-row">
                                    <@common.combobox name="resource" target="#columnDiv_0"
                                            placeholder="select a resource"
                                            autocompleteParentElement="#divResource_0"
                                            autocompleteIdElement="#resource_0_oid"
                                            cssClass="input-xxlarge-combo " />
                                        <hidden name="resource.id" id="resource_0_oid" />
                    </div>
                </div>
                    </div>
                </div>
                </div>
            </div>
                
<p>
  <button class="btn btn-primary" type="button">Submit</button>
<!--  <button class="btn" type="button"><i class="icon-cog"></i> more options</button> -->
</p>                
                
</div>
        </div>
    </div>
</div>
</div>
</form>
<div class="row">
<div class="span12">
        <@collectionsSection />
</div>

</div>
</div>

    <#macro repeat num val>
        <#if (num > 0)>
            <@repeat (num-1) val /><#noescape>${val}</#noescape>
        </#if>
    </#macro>

    <#macro collectionsSection>

<ul class="nav nav-tabs" id="myTab">
  <li class="active" data-toggle="tab"><a href="#groups">Groups</a></li>
  <li><a href="#shared" data-toggle="tab">Shared With Me</a></li>
  <li><a href="#individual" data-toggle="tab">Individual Resources</a></li>
</ul>
 
<div class="tab-content" >
  <div class="tab-pane active" id="groups">
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
                <td>${collection.name}</td>
                <td>${collection.resources?size}</td>
                <td>${collection.authorizedUsers?size}</td>
                <td>edit | delete | details</td>
                </tr>
            </#list>
            </tbody>
        </table>
        <a href="<@s.url value="/collection/add"/>">create one</a>
  
  </div>
  <div class="tab-pane" id="shared">
        <#if sharedResourceCollections?? && !sharedResourceCollections.empty >
        <div class="">
            <h2>Collections Shared With You</h2>
            <@common.listCollections collections=sharedResourceCollections />
        </div>
        </#if>
  </div>

  <div class="tab-pane" id="individual">
        <table class="table">
            <thead>
                <th>Name</th>
                <th># of users</th>
                <th>action</th>
            </thead>
            <tbody>
            <#list intenralCollections![] as collection>
                <tr>
                <td><#if (collection.resources?size > 0 )>
					<#list collection.resources as resource>
						${resource.title}</#list></#if></td>
                <td>${collection.authorizedUsers?size}</td>
                <td>edit | delete | details</td>
                </tr>
            </#list>
            </tbody>
        </table>

  </div>
</div>



    </#macro>

    <#macro accountSection>
    <div id="accountSection" class="row">
        <div id="divAccountInfo" class="<#if payPerIngestEnabled>span4<#else>span9</#if>">
            <h2>About ${authenticatedUser.firstName}</h2>
            <strong>Full Name: </strong>${authenticatedUser.properName}<#if authenticatedUser.institution??>, ${authenticatedUser.institution.name}</#if><br>
            <#if authenticatedUser.penultimateLogin??>
                <strong>Last Login: </strong>${authenticatedUser.penultimateLogin?datetime}<br>
            </#if>
            <a href="<@s.url value='/entity/user/edit?id=${authenticatedUser.id?c}'/>">edit your profile</a><br>
            <a href="<@s.url value='/export/request'/>">Export</a>
        </div>

        <div class="span5" id="billing">
            <@common.billingAccountList accounts />
        </div>
    </div>
    </#macro>


    <#macro bookmarksSection>
    <div class="row">
        <div class="span9" id="bookmarks">
            <#if ( bookmarkedResources?size > 0)>
            <h2 >Bookmarks</h2>
                <@rlist.listResources resourcelist=bookmarkedResources sortfield='RESOURCE_TYPE' listTag='ol' headerTag="h3" />
            <#else>
            <h3>Bookmarked resources appear in this section</h3>
            Bookmarks are a quick and useful way to access resources from your dashboard. To bookmark a resource, click on the star <i class="icon-star"></i> icon next to any resource's title.
            </#if>
        </div>
    </div>
    </#macro>

<script>
    $(document).ready(function () {
        TDAR.notifications.init();
        TDAR.common.collectionTreeview();
        $("#myCarousel").carousel('cycle');
    });
</script>



<#macro headerNotifications>
    <#list currentNotifications as notification>
        <div class="${notification.messageType} alert" id="note_${notification.id?c}">
        <button type="button" id="close_note_${notification.id?c}" class="close" data-dismiss="alert" data-dismiss-id="${notification.id?c}" >&times;</button>
        <#if notification.messageDisplayType.normal>
        <@s.text name="${notification.messageKey}"/> [${notification.dateCreated?date?string.short}]
        <#else>
            <#local file = "../notifications/${notification.messageKey}.ftl" />
            <#if !notification.messageKey?string?contains("..") >
                <#attempt>
                    <#include file />
                <#recover>
                    Could not load notification.
                </#attempt>
            </#if>
        </#if>
        </div>
    </#list>

    <#list resourcesWithErrors![]>
    <div class="alert-error alert">
        <h3><@s.text name="dashboard.archiving_heading"/></h3>

        <p><@common.localText "dashboard.archiving_errors", serviceProvider, serviceProvider /> </p>
        <ul>
            <#items as resource>
                <li>
                    <a href="<@s.url value="${resource.detailUrl}" />">${resource.title}:
                        <#list resource.filesWithProcessingErrors as file><#if file_index !=0>,</#if>${file.filename!"unknown"}</#list>
                    </a>
                </li>
            </#items>
        </ul>
    </div>
    </#list>

<#if showUserSuggestions!false>
	<#list userSuggestions>
    <div class="alert-error alert">
        <h3>Are any of these you?</h3>
		<ul class="unstyled">
			<#items as sug>
				<li><input type="checkbox" name="merge" value="${sug.id?c}-${sug.properName}">&nbsp;<@s.a value="${sug.detailUrl}">${sug.properName}</@s.a></li>
			</#items>
		</ul>
		<form>
		<!-- fixme - send email via email-controller 
			construct list of people; contruct url for -- dedup /admin/authority/select-authority?selectedDupeIds=...&entityType=PERSON -->
		<button name="yes" class="button btn">Yes</button>
		<button name="no"  class="button btn">No</button>
		</form>
    </div>
	</#list>
</#if>
    <#list overdrawnAccounts![]>
    <div class="alert-error alert">
        <h3><@s.text name="dashboard.overdrawn_title"/></h3>

        <p><@s.text name="dashboard.overdrawn_description" />
            <a href="<@s.url value="/cart/add"/>"><@s.text name="dashboard.overdrawn_purchase_link_text" /></a>
        </p>
        <ul>
            <#items as account>
                <li>
                    <a href="<@s.url value="/billing/${account.id?c}" />">${account.name!"unamed"}</a>
                </li>
            </#items>
        </ul>
    </div>
    </#list>

</#macro>
            <script>
            $(document).ready(function() {
                var picker = $('.datepicker').datepicker();
                picker.on('changeDate', function(ev){
                    $(ev.target).datepicker('hide');
                });
                
            });
            </script>

</#escape>
