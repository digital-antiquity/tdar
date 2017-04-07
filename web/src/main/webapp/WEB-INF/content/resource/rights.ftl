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
    <h1>Sharing: <span class="red">My Test Resource</span></h1>
</div>
    <form class="form-horizontal tdarvalidate" action="save" method="POST">
<div class="row">
    <div class="span12">
    <p><b>This resource is shared with ${proxies?size} people and is in ${persistable.sharedCollections?size} collections.</b></p>
    <@s.hidden name="id" />
    <h3>Add / Modify User(s)</h3>
    <div id="divAccessRights" class="repeatLastRow" data-addanother="add another user">
    <div class="row">
        <div class='span6'>
        <b>User or Email </b>
        </div>
        <div class="span2">
         <b>Permissions</b>
        </div>
        <div class='span2'>
        <b>Remove access after</b>
        </div>
                <div class='span1'>
        </div>

    </div>
            <#list proxies>
            <#items as proxy>
				<@_proxy proxy_index proxy/>
            </#items>
            <#else>
				<@_proxy 0 blankProxy />
            </#list>
    </div>
    
    <#macro _proxy proxy_index proxy>
    
                <div class="">
                        <div class=" control-group repeat-row" id="authorizedUsersRow_${proxy_index}_">
                            <div class="controls-row" >
                                <div class="span6">
                                    <div id="authorizedUsersRow_${proxy_index}_p" class="creatorPerson  ">
                                        <input type="hidden" name="proxies[${proxy_index}].id" value="${(proxy.id!-1)?c}" id="authorizedUsersId__id_${proxy_index}_p" autocompleteparentelement="#authorizedUsersRow_${proxy_index}_p">
                                            <input type="text" name="proxies[${proxy_index}].displayName" maxlength="255" value="${proxy.displayName!''}" id="metadataForm_authorizedUsersFullNames_${proxy_index}_"
                                            	 class="span6 userAutoComplete notValidIfIdEmpty   ui-autocomplete-input" 
                                            	 autocompleteparentelement="#authorizedUsersRow_${proxy_index}_p"
                                            	  data-msg-notvalidifidempty="Invalid user name.  Please type a name (or partial name) and choose one of the options from the menu that appears below." 
                                            	  autocomplete="off" placeholder="Username or Email Address" autocompletename="properName"
                                            	   autocompleteidelement="#authorizedUsersId__id_${proxy_index}_p">
                                    </div>
                                </div>
                                <div class="span2">
	                                <@s.select theme="tdar" cssClass="creator-rights-select span2" name="proxies[${proxy_index}].permission" emptyOption='false'
                    					listValue='label' list='%{availablePermissions}' disabled=isDisabled />
                                </div>
                                <div class=" span2">
                                    <div class="input-append">
                                    <#local val=""/>
                                    <#if proxy.until?has_content>
                                    	<#local val=proxy.until />
                                    </#if>
                                          <input class="datepicker" name="proxies[${proxy_index}].until" style="width:6em" 
                                          	size="16" type="text" value="${val}" data-date-format="mm-dd-yyyy" >

                                          <span class="add-on"><i class="icon-th"></i></span>
                                    </div>
                                </div>

                                <div class="span1">
                                    <button class="btn btn-mini repeat-row-delete" type="button" tabindex="-1" title="delete this item from the list"><i class="icon-trash"></i></button>
                                </div>
                            </div>

            </div>
        </div>
    </#macro>

<h3>Invite New User</h3>
    <div id="divAccessRights" class="repeatLastRow" data-addanother="add another user">
    <p><i>Use this to share this resource with someone who's not currently a tDAR user</i></p>
    <div class="row">
        <div class='span2'> <b>First Name</b> </div>
        <div class='span2'> <b>Last Name</b> </div>
        <div class='span2'> <b>Email</b> </div>
        <div class="span2"> <b>Permissions</b> </div>
        <div class='span2'> <b>Remove access after</b> </div>
        <div class='span1'>
        </div>

    </div>
            <div class="control-group">
                            <div class=" repeat-row" id="authorizedUsersRow_0_">
                            <div class="controls-row" >
                                <div class="span2">
                                    <@s.textfield name="firstName" cssClass="span2" />
                                </div>
                                <div class="span2">
                                    <@s.textfield name="lasstName" cssClass="span2" />
                                </div>
                                <div class="span2">
                                    <@s.textfield name="email" cssClass="span2" />
                                </div>
                                <div class="span2">
                                        <select name="authorizedUsers[0].generalPermission" id="metadataForm_authorizedUsers_0__generalPermission" class="creator-rights-select span2">
                                            <option value="VIEW_ALL">View and Download</option>
                                            <option value="MODIFY_METADATA">Modify Metadata</option>
                                            <option value="MODIFY_RECORD">Modify Files &amp; Metadata</option>
                                        </select>
                                        <input type="hidden" name="authorizedUsers[0].generalPermission" value="" id="authorizedUsers[0]hdnGeneralPermission" class="repeat-row-remove">
                                </div>
                                <div class=" span2">
                                    <div class="input-append">
                                          <input class="datepicker" style="width:6em" size="16" type="text" value="" id="dp3" data-date-format="mm-dd-yyyy" >
                                          <span class="add-on"><i class="icon-th"></i></span>
                                    </div>
                                </div>

                                <div class="span1">
                                    <button class="btn btn-mini repeat-row-delete" type="button" tabindex="-1" title="delete this item from the list"><i class="icon-trash"></i></button>
                                </div>
                            </div>
            </div>
        </div>
    </div>


</div>
                </form>

    <#macro repeat num val>
        <#if (num > 0)>
            <@repeat (num-1) val /><#noescape>${val}</#noescape>
        </#if>
    </#macro>


<@edit.personAutocompleteTemplate />

<@edit.submit fileReminder=false />
<div id="customIncludes" parse="true">
    <!-- <script src="/js/tdar.manage.js"></script> -->
<script>
$(function() {
    TDAR.repeatrow.registerRepeatable(".repeatLastRow");
	TDAR.autocomplete.delegateCreator("#divAccessRights", true, false);
    $("form").on("repeatrowadded",function() {TDAR.datepicker.bind($("input.datepicker",$("form")))});
            
})
</script>
</div>


</#escape>
