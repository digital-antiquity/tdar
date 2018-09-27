<#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
<#escape _untrusted as _untrusted?html>


	<#--  render user invite form for rights pages -->
	<#macro invite>
	<h3>Invite New User</h3>
    <div id="divAccessRights">
	    <p><i>Use this to share this resource with someone who's not currently a tDAR user</i></p>
    </div>
            <div class="row">
                <div class="col-6" id="invites">
	                <div class="form-row" >
	                	<div class="col-4"><b>First Name</b></div>
	                    <div class="col-8">
	                        <@s.textfield name="invites[0].firstName" cssClass=" inviteForm" id="firstName" />
	                    </div>
	                </div>
                    
	                <div class="form-row" >
	                	<div class="col-4"><b>Last Name</b></div>
    	                <div class="col-8">
        	                <@s.textfield name="invites[0].lastName" cssClass=" inviteForm" id="lastName" />
            	        </div>
	                </div>
                    
	                <div class="form-row" >
	                	<div class="col-4"><b>Email</b></div>
	                    <div class="col-8">
	                        <@s.textfield name="invites[0].email" cssClass=" inviteForm" id="email" />
	                    </div>
	                </div>
                    
	                <div class="form-row" >
	                	<div class="col-4"><b>Permission</b></div>
	                    <div class="col-8">
	                        <@s.select  cssClass="controls creator-rights-select" name="invites[0].permission" emptyOption='false'
	                            listValue='label' list='%{availablePermissions}' disabled=isDisabled />
	                    </div>
	                </div>
                    
	                <div class="form-row" >
	                	<div class="col-4"><b>Expires?</b></div>
	                    <div class=" col-8">
	                        <div class="input-group">
	                              <input class="form-control datepicker" name="invites[0].until" style="width:6em" 
	                                size="16" type="text" value="" data-date-format="mm-dd-yyyy" >
	                               <div class="input-group-append">
	                              <span class="input-group-text"><i class="far fa-calendar-alt"></i></span>
	                              </div>
	                        </div>
	                    </div>
                    </div>
                </div>
                <div class="col-6">
	                <div class="form-row" >
	                <p><b>Add a Note</b></p>
	                <@s.textarea name="invites[0].note" cssClass="col-12" rows=8 />
	                </div>
                </div>
            </div>

	</#macro>

	<#--  render add user form for rights pages -->
	<#macro addUsersForRights  showDate=true>
	
    <h3>Add / Modify User(s)</h3>
    <div id="divAccessRights" class="repeatLastRow" data-addanother="add another user">
    <div class="row">
        <div class='col-<#if showDate>6<#else>8</#if>'>
        <b>User or Email </b>
        </div>
        <div class="col-3">
         <b>Permissions</b>
        </div>
        <#if showDate>
        <div class='col-2'>
        <b>Remove access after</b>
        </div>
        </#if>
                <div class='col-1'>
        </div>

    </div>
            <#list proxies![]>
            <#items as proxy>
            	<#if proxy?? && proxy?has_content>
                	<@_proxy proxy_index proxy showDate/>
                </#if>
            </#items>
            <#else>
                <@_proxy 0 blankProxy showDate/>
            </#list>
    </div>
	</#macro>


    <#macro _proxy proxy_index proxy showDate=true>
    
                        <div class=" control-group repeat-row mb-3" id="authorizedUsersRow_${proxy_index}_">
                            <div class="form-row"  id="authorizedUsersRow_${proxy_index}_p" class="creatorPerson  ">
                                    <div class="col-<#if showDate>6<#else>8</#if>">
                                        <input type="hidden" name="proxies[${proxy_index}].inviteId" value="<#if proxy.inviteId?has_content>${(proxy.inviteId!-1)?c}</#if>">

                                        <input type="hidden" name="proxies[${proxy_index}].id" value="${(proxy.id!-1)?c}" id="authorizedUsersId__id_${proxy_index}_p" autocompleteparentelement="#authorizedUsersRow_${proxy_index}_p">
                                        <input type="text" name="proxies[${proxy_index}].displayName" maxlength="255" value="${proxy.displayName!''} <#if proxy.inviteId?has_content> (invite) </#if>" id="metadataForm_authorizedUsersFullNames_${proxy_index}_"
                                                 class="mr-2 userAutoComplete notValidIfIdEmpty   ui-autocomplete-input form-control"
                                                 <#if proxy.inviteId?has_content> disabled=true </#if>
                                                 
                                                 autocompleteparentelement="#authorizedUsersRow_${proxy_index}_p"
                                                 data-msg-notvalidifidempty="Invalid user name.  Please type a name (or partial name) and choose one of the options from the menu that appears below."
                                                 autocomplete="off" placeholder="Username or Email Address" autocompletename="properName"
                                                 autocompleteidelement="#authorizedUsersId__id_${proxy_index}_p">
                                            </div>
                                            <@s.select cssClass="creator-rights-select col-3" name="proxies[${proxy_index}].permission" emptyOption='false'
                                        listValue='label' list='%{availablePermissions}' disabled=isDisabled />
                                <#if showDate>
                                    <div class="input-append col-2">
                                    <#local val=""/>
                                    <#if proxy.until?has_content>
                                        <#local val=proxy.until />
                                    </#if>
                                            <div class="input-group ml-2">
                                          <input class="datepicker form-input" name="proxies[${proxy_index}].until" style="width:6em" 
                                            size="16" type="text" value="${val}" data-date-format="mm-dd-yyyy" >
                                           <div class="input-group-append">
                                              <span class="input-group-text"><i class="far fa-calendar-alt"></i></span>
                                          </div>
                                          </div>
                                    </div>
                                </#if>
									<@nav.clearDeleteButton  />
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
    TDAR.common.initFormNavigate($("form.rightsform"));
    $("form").on("repeatrowadded",function() {TDAR.datepicker.bind($("input.datepicker",$("form")))});
    TDAR.common.initRightsPage();
            
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
        <#if editable && (collections?has_content || invites?has_content || ((persistable.authorizedUsers)![])?has_content) >
        <h2>Access Permissions</h2>
            <#nested />
        <table class="table table-sm table-striped"">
              <thead class="thead-dark">

            <tr>
                <th>Where</th>
                <th>User</th>

                <#list availablePermissions as permission>
                    <th>${permission.label}</th>
                </#list>
                <th>Expires</th>
            </tr>
            	<#if persistable.authorizedUsers?has_content>
	            	<@_authorizedUsers />
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

   	<#macro _authorizedUsers>
            <#list persistable.authorizedUsers as user>
                <tr>
                    <td>
                    <!-- id:${user.id?c}--> 
                        Local
                    </td>
                    <td>
                    <a href="<@s.url value="${user.user.detailUrl}"/>">${user.user.properName}</a> <!-- ${user.user.properName}:${user.generalPermission} -->
                    </td>
                    <#list availablePermissions as permission>
                        <td>
                            <#if (user.generalPermission.effectivePermissions >= permission.effectivePermissions )>
                                <i class="fas fa-check"></i>
                            <#else>
                                <i class="fas fa-times"></i>
                            </#if>
                        </td>
                    </#list>
                    <td>${(user.dateExpires?string("MM/dd/yyyy"))!'∞'}</td>
                </tr>
            </#list>
   	</#macro>
            
    <#--  print out authorized Users by collection for rights table -->
    <#macro _collectionSection collections>
        <#list collections as collection_ >
        <#--  if the persistable is NOT a collection OR the collection != current persistable (the latter is handled above) -->
        <#if !(persistable?has_content && persistable == collection_) >
            <#if collection_.authorizedUsers?has_content >
                <#list collection_.authorizedUsers as user>
                <tr>
                    <td>
                        <a href="<@s.url value="${collection_.detailUrl}"/>"> ${collection_.name!"<em>un-named</em>"}</a>
                    </td>
                    <td>
                        <a href="<@s.url value="${user.user.detailUrl}"/>">${user.user.properName}</a> <!-- ${user.user.properName}:${user.generalPermission} -->
                    </td>
                    <#list availablePermissions as permission>
                        <td>
                            <#if (user.generalPermission.effectivePermissions >= permission.effectivePermissions )>
                                <i class="fas fa-check"></i>
                            <#else>
                                <i class="fas fa-times"></i>
                            </#if>
                        </td>
                    </#list>
                    <td>${(user.dateExpires?string("MM/dd/yyyy"))!'∞'}</td>
                </tr>
                </#list>
            <#else>
                <tr>
                    <td>
                        <a href="<@s.url value="${collection_.detailUrl}"/>"> ${collection_.name!"<em>un-named</em>"}</a>
                    </td>
                    <td colspan=8>n/a</td>
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
                <td>${(invite.dateExpires?string("MM/dd/yyyy"))!'∞'}</td>
            </tr>
        </#list>
    </#macro>
</#escape>