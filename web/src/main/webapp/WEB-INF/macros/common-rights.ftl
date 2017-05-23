<#escape _untrusted as _untrusted?html>


	<#--  render user invite form for rights pages -->
	<#macro invite>
	<h3>Invite New User</h3>
    <div id="divAccessRights">
	    <p><i>Use this to share this resource with someone who's not currently a tDAR user</i></p>
    </div>
            <div class="row">
                <div class="span6" id="invites">
	                <div class="controls-row" >
	                	<div class="span2"><b>First Name</b></div>
	                    <div class="span4">
	                        <@s.textfield name="invites[0].firstName" cssClass="span4" />
	                    </div>
	                </div>
                    
	                <div class="controls-row" >
	                	<div class="span2"><b>Last Name</b></div>
    	                <div class="span4">
        	                <@s.textfield name="invites[0].lastName" cssClass="span4" />
            	        </div>
	                </div>
                    
	                <div class="controls-row" >
	                	<div class="span2"><b>Email</b></div>
	                    <div class="span4">
	                        <@s.textfield name="invites[0].email" cssClass="span4" />
	                    </div>
	                </div>
                    
	                <div class="controls-row" >
	                	<div class="span2"><b>Permission</b></div>
	                    <div class="span4">
	                        <@s.select theme="tdar" cssClass="controls creator-rights-select span4" name="invites[0].permission" emptyOption='false'
	                            listValue='label' list='%{availablePermissions}' disabled=isDisabled />
	                    </div>
	                </div>
                    
	                <div class="controls-row" >
	                	<div class="span2"><b>Expires?</b></div>
	                    <div class=" span4">
	                        <div class="input-append">
	                              <input class="controls datepicker" name="invites[0].until" style="width:6em" 
	                                size="16" type="text" value="" data-date-format="mm-dd-yyyy" >
	
	                              <span class="add-on"><i class="icon-th"></i></span>
	                        </div>
	                    </div>
                    </div>
                </div>
                <div class="span6">
	                <div class="controls-row" >
	                <p><b>Add a Note</b></p>
	                <@s.textarea name="invites[0].note" cssClass="span4" rows=5 />
	                </div>
                </div>
            </div>

	</#macro>

	<#--  render add user form for rights pages -->
	<#macro addUsersForRights >
	
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
            	<#if proxy?? && proxy?has_content>
                	<@_proxy proxy_index proxy/>
                </#if>
            </#items>
            <#else>
                <@_proxy 0 blankProxy />
            </#list>
    </div>
	</#macro>


    <#macro _proxy proxy_index proxy>
    
                <div class="">
                        <div class=" control-group repeat-row" id="authorizedUsersRow_${proxy_index}_">
                            <div class="controls-row" >
                                <div class="span6">
                                    <div id="authorizedUsersRow_${proxy_index}_p" class="creatorPerson  ">
                                    <#if proxy.id?has_content>
                                        <input type="hidden" name="proxies[${proxy_index}].id" value="${(proxy.id!-1)?c}" id="authorizedUsersId__id_${proxy_index}_p" autocompleteparentelement="#authorizedUsersRow_${proxy_index}_p">
                                            <input type="text" name="proxies[${proxy_index}].displayName" maxlength="255" value="${proxy.displayName!''}" id="metadataForm_authorizedUsersFullNames_${proxy_index}_"
                                                 class="span6 userAutoComplete notValidIfIdEmpty   ui-autocomplete-input"
                                                 autocompleteparentelement="#authorizedUsersRow_${proxy_index}_p"
                                                  data-msg-notvalidifidempty="Invalid user name.  Please type a name (or partial name) and choose one of the options from the menu that appears below."
                                                  autocomplete="off" placeholder="Username or Email Address" autocompletename="properName"
                                                   autocompleteidelement="#authorizedUsersId__id_${proxy_index}_p">
                                   <#else>
                                        <input type="hidden" name="proxies[${proxy_index}].inviteId" value="${(proxy.inviteId!-1)?c}">
                                            <input type="text" name="proxies[${proxy_index}].displayName" maxlength="255" value="${proxy.displayName!''}" disabled=true
                                                 class="span6 ">

                                   </#if>
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
    
    <#macro javascript>
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

    </#macro>
    
    
    <#--Render the "Access Permissions" section of a resource view page.  Specifically, this section shows
  the collections associated with the resource and the users + permission assigned to the resource. -->
<#-- @param collections:list? a list of resourceCollections -->
<#-- @param owner:object? Person object representing the collection owner
<#-- FIXME:  both of these parameters have invalid defaults. consider making them mandatory  -->
    <#macro resourceCollectionsRights collections=[] owner="">
        <#if collections?has_content || invites?has_content >
        <h3>Access Permissions</h3>
            <#nested />
        <table class="tableFormat table">
            <thead>
            <tr>
                <th>Collection</th>
                <th>User</th>
                <#list availablePermissions as permission>
                    <th>${permission.label}</th>
                </#list>
                <th>Expires</th>
            </tr>
                <#if owner?has_content>
                <tr>
                    <td>Local Resource</td>
                    <td>${owner.properName} (Submitter)</td>
                    <td><i class="icon-ok"></i></td>
                    <td><i class="icon-ok"></i></td>
                    <td><i class="icon-ok"></i></td>
                    <td></td>
                </tr>
                </#if>
                <#if collections?has_content >
	                <@_collectionSection collections />
                </#if>
                <#if invites?has_content>
                    <@_invitesSection invites />
                </#if>

        </table>
        </#if>
    </#macro>
    
    <#--  print out authorized Users by collection for rights table -->
    <#macro _collectionSection collections>
        <#list collections as collection_ >
            <#if collection_.authorizedUsers?has_content >
                <#list collection_.authorizedUsers as user>
                <tr>
                    <td>
                        <#if collection_.topCollection?has_content >
                            <a href="<@s.url value="${collection_.detailUrl}"/>"> ${collection_.name!"<em>un-named</em>"}</a>
                        <#else>
                            Local Resource
                        </#if>
                    </td>
                    <td>
                    <a href="<@s.url value="${user.user.detailUrl}"/>">${user.user.properName}</a> <!-- ${user.user.properName}:${user.generalPermission} -->
                    </td>
                    <#list availablePermissions as permission>
                        <td>
                            <#if (user.generalPermission.effectivePermissions >= permission.effectivePermissions )>
                                <i class="icon-ok"></i>
                            <#else>
                                <i class="icon-remove"></i>
                            </#if>
                        </td>
                    </#list>
                    <td>${(user.dateExpires?string("MM/dd/yyyy"))!''}</td>
                </tr>
                </#list>
            <#else>
                <#if collection_.type == 'SHARED'>
                <tr>
                    <td>
                        <a href="<@s.url value="${collection_.detailUrl}"/>"> ${collection_.name!"<em>un-named</em>"}</a>
                    </td>
                    <td colspan=5>n/a</td>
                </tr>                    
                </#if>
            </#if>
        </#list>
    </#macro>
    <#--  print out user invites for rights table -->
    <#macro _invitesSection invites>
        <#list invites as invite >
            <tr>
                <td>Invite (<a href="<@s.url value="${invite.authorizer.detailUrl}"/>">${invite.authorizer.properName}</a>)</td>
                <td>
                <a href="<@s.url value="${invite.user.detailUrl}"/>">${invite.user.properName}</a> <!-- ${invite.user.properName}:${invite.permissions} -->
                </td>
                <#list availablePermissions as permission>
                    <td>
                        <#if (invite.permissions.effectivePermissions >= permission.effectivePermissions )>
                            <i class="icon-ok"></i>
                        <#else>
                            <i class="icon-remove"></i>
                        </#if>
                    </td>
                </#list>
                <td>${(invite.dateExpires?string("MM/dd/yyyy"))!''}</td>
            </tr>
        </#list>
    </#macro>
</#escape>