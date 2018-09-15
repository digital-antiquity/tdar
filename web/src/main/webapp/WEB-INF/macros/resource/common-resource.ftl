<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/settings.ftl" as settings />
    <#import "../common.ftl" as common />
<#-- 
$Id:Exp$
Common macros used in multiple contexts
-->
<#-- opting out of code formatting: begin -->
<#--@formatter:off-->
<#-- opting out of code formatting: end -->
<#--@formatter:on-->

<#-- Emit the container and script for a bar graph -->
    <#macro resourceBarGraph>
        <div id="resourceBarGraph" style="height:400px" data-source="#homepageResourceCountCache" class="barChart"
        data-x="label" data-values="count" data-click="resourceBarGraphClick" data-yaxis="log" data-colorcategories="true" >
        </div>
        <#noescape>
        <script id="homepageResourceCountCache" type="application/json">
        ${homepageGraphs.resourceTypeJson}
        </script>
        </#noescape>
    
    </#macro>
    



<#-- Emit container div and script for the worldmap control. The worldmap control shows the number of registeresd
 resources for a country as the user hovers their mouse over a country.  If the user clicks on a country,
 the browser redirects to a search results that limits results to show only ressources that have geographic boundaries
 that lie within the selected country -->
<#-- @param forceAddSchemeHostAndPort:boolean if true, clickhandler always includes hostname and port when bulding
            the redirect url.  If false,   the clickhandler builds a url based on the current hostname and port -->
    <#macro renderWorldMap forceAddSchemeHostAndPort=false mode="horizontal" extra="">
    <div class=" <#if mode == 'vertical'>span7<#elseif mode == 'horizontal'>span6 map mapcontainer<#else> mapcontainer</#if> ${mode}" >
            <h3>${siteAcronym} Worldwide</h3>
        <#if (homepageGraphs.mapJson)?has_content>
        <script type="application/json" data-mapdata>
			<#noescape>${homepageGraphs.mapJson}</#noescape>
        </script>
        </#if>
        <#nested />
		<#if (homepageGraphs.localesJson)?has_content>
        <script type="application/json" data-locales>
			<#noescape>${homepageGraphs.localesJson}</#noescape>
        </script>
		</#if>
             <div id="worldmap" style="height:<#if mode == 'mini'>150<#else>350</#if>px" data-max="" <#if (keyword.code)?has_content>data-code="${(keyword.code)!""}"</#if> data-mode="${mode}" >
             </div>
        <#if mode =='vertical'></div></#if>
             <div id="mapgraphdata"  <#if mode == 'vertical'>data-mode="vertical" class="span4 offset1"<#else>style="width:100%"</#if>>
        <#if mode =='vertical'><br/><br/></#if>
                 <h5 id="mapGraphHeader"></h5>
                 <div id='mapgraphpie'>                 
                 </div>
             </div>
        <#if mode !='vertical'></div></#if>
	<script>
	$(function() {
//    	TDAR.worldmap.initWorldMap("worldmap","${mode}", "${extra}");
	});
	</script>
    </#macro>

    <#macro cartouche persistable useDocumentType=false>
        <#local cartouchePart><@upperPersistableTypeLabel persistable /></#local>
    <span class="cartouche">
    <#local type=""/>
    <#if persistable.resourceType?has_content>
        <#local type>svg-icons_icon-${persistable.resourceType?lower_case}</#local>
    <#elseif persistable.collection?has_content><#t>
        <#local type>svg-icons_collection</#local>
    <#elseif persistable.integration?has_content><#t>
        <#local type>svg-icons_integration</#local>
    </#if>
    <svg class="svgicon white svg-cartouche"><use xlink:href="/images/svg/symbol-defs.svg#${type}"></use></svg>
    <#--        <#if (persistable.status)?? && !persistable.active>
            ${persistable.status} <#t>
        </#if>  -->
        <@upperPersistableTypeLabel persistable />
     </span>
        <#nested />
    </#macro>

<#--FIXME: persistable getType() doesn't exist. define it, then override  in Resource.  -->
<#-- Emit the specified persistable's 'type' in uppercase (e.g. DOCUMENT, SENSORYDATA, COLLECTION...) -->
<#-- @param persistable:Persistable either a persistable instance or object instance or dedupable instance -->
    <#macro upperPersistableTypeLabel persistable>
        <#if persistable.resourceType?has_content><#t>
        ${persistable.resourceType?replace("_", " ")?upper_case} <#t>
        <#elseif persistable.collection?has_content><#t>
        COLLECTION <#elseif persistable.integration?has_content> <#t>
        INTEGRATION<#t>
        </#if>
    </#macro>



<#-- emit a div that lists the addresses for the specified person.-->
<#-- @param entity:Person person object containing addresses to render -->
<#-- @param entityType:string type of entity ("person" or "institution" )-->
<#-- @param choiceField:string FIXME: no clue what this means.  parsed as boolean,  caller supplies a number, compared to a string -->
<#-- @addressId:number id of the address to be considered the 'current' address in the context of the page displaying this macro  -->
    <#macro listAddresses entity=person entityType="person" choiceField="" addressId=-1>
    <div class="row">
        <#list entity.addresses  as address>
            <div class="span3">
                <#local label = ""/>
                <#if address.type?has_content>
                    <#local label = address.type.label>
                </#if>
                <#if choiceField?has_content>
                <label class="radio inline">
                    <input type="radio" name="invoice.address.id" label="${label}"
                           value="${address.id}"
                           <#if address.id==addressId || (!addressId?has_content || addressId == -1) && address_index==0>checked=checked</#if>/>
                </#if>

                <@printAddress  address=address creatorId=entity.id creatorType=entityType modifiable=true showLabel=false >
                    <b><#if address.type?has_content>${address.type.label!""}</#if></b>
                </label><br/>
                </@printAddress>
            </div>
        </#list>
        <div class="span3">
            <#local retUrl><@s.url includeParams="all"/></#local>
            <a class="button btn btn-primary submitButton" href="/entity/address/${entity.id?c}/add?returnUrl=${retUrl?url}">Add Address</a>
        </div>
    </div>
    </#macro>

<#-- emit a single formatted address -->
<#-- @param address:Address? address object to render (default: valueStack.address) -->
<#-- @param creatorId:number? id for persisted value (default: -1) -->
<#-- @param creatorType:string?  either "person" or "institution"  (default: "person") -->
<#-- @param modifiable:boolean? render as a editable form fields (default:false)-->
<#-- @param deletable:boolean? render a delete button  (default:false) -->
<#-- @param showLabel:boolean? show the address.addressType.label value (default:false) -->
    <#macro printAddress address=address creatorId=-1 creatorType='person'  modifiable=false deletable=false showLabel=true>
    <p>
        <#if address.type?has_content && showLabel><b>${address.type.label!""}</b><br></#if>
        <span>${address.street1}<br/>
        ${address.street2}</span><br/>
        <span>${address.city}</span>, <span>${address.state}</span>, <span
            >${address.postal}</span><br/>
        <span>${address.country}</span><#if modifiable><br/>
        <a href="<@s.url value="/entity/address/${creatorId?c}/${address.id?c}"/>"><@s.text name="menu.edit" /></a>
    </#if><#if deletable && modifiable> |</#if>
        <#if deletable>
        	<@s.form method="POST" action="/entity/address/delete-address">
				<@s.hidden name="id" value="${creatorId?c}"/>
				<@s.hidden name="addressId" value="${address.id?c}"/>
            	<@s.button name="menu.delete" />
			</@s.form>
        </#if>
    </p>
    </#macro>

<#-- emit "checked" if arg1==arg2 -->
    <#macro checkedif arg1 arg2><#t>
        <@valif "checked='checked'" arg1 arg2 />
    </#macro>

<#-- emit "selected" if arg1==arg2 -->
    <#macro selectedif arg1 arg2>
        <@valif "selected='selected'" arg1 arg2 />
    </#macro>

<#-- emit val if arg1==arg2 -->
    <#macro valif val arg1 arg2><#t>
        <#if arg1=arg2>${val}</#if><#t>
    </#macro>

    <#macro featuredCollection featuredCollection>
        <h3>Featured Collection</h3>
        <p>
    <#if logoAvailable>
        <img class="pull-right collection-logo" src="/files/collection/sm/${featuredCollection.id?c}/logo"
        alt="logo" title="logo" /> 
    </#if>
    <a href="${featuredCollection.detailUrl}"><b>${featuredCollection.name}</b></a>: ${featuredCollection.description}</p>
    </#macro>
    
<#-- emit a "combobox" control.  A combobox is essentially text field element that features both autocomplete support as
 as the ability to view a list of all possible values (by clicking on a 'dropdown' button beside the text box)-->
    <#macro combobox name target autocompleteIdElement placeholder  cssClass value=false autocompleteParentElement="" label="" bootstrapControl=true id="" addNewLink="" collectionType="">
        <#if bootstrapControl>
        <div class="control-group">
            <label class="control-label">${label}</label>
        <div class="controls">
        </#if>
        <div class="input-append">
        <#--if 'value' is not a string,  omit the 'value' attribute so that we don't override the
        s.textfield default (i.e. the value described by the 'name' attribute) -->
            <#if value?is_string>
                <@s.textfield theme="simple" name="${name}"  target="${target}"
                label="${label}"
                autocompleteParentElement="${autocompleteParentElement}"
                autocompleteIdElement="${autocompleteIdElement}"
                placeholder="${placeholder}" cssClass="${cssClass}"
                collectionType="${collectionType}"
                value="${value}"  />
            <#else>
                <@s.textfield theme="simple" name="${name}"  target="${target}"
                label="${label}"
                autocompleteParentElement="${autocompleteParentElement}"
                autocompleteIdElement="${autocompleteIdElement}"
                collectionType="${collectionType}"
                placeholder="${placeholder}" cssClass="${cssClass}" />
            </#if>
            <button type="button" class="btn show-all"><i class="icon-chevron-down"></i></button>
            <#if addNewLink?has_content>
                <a href="${addNewLink}" onClick="TDAR.common.setAdhocTarget(this, '${autocompleteParentElement?js_string}');" class="btn show-all"
                   target="_blank">add new</a>
            </#if>
        </div>
        <#if bootstrapControl>
        </div>
        </div>
        </#if>
    </#macro>


<#-- emit html for single collection list item -->
    <#macro collectionListItem depth=0 collection=collection showOnlyVisible=false showBadge=false>
        <#if !collection.hidden || collection.viewable || showOnlyVisible==false >
            <#local clsHidden = "", clsClosed="" >
            <#if (depth < 1)><#local clsHidden = "hidden"></#if>
            <#if ((depth < 1) && (collection.transientChildren?size > 0))><#local clsClosed = "closed"></#if>
        <li class="${clsClosed}">
            <@s.a href="${collection.detailUrl}">${(collection.name)!"No Title"}<#if collection.new><span style="margin-left:.8em" class="label">new</span></#if></@s.a>
            <#if collection.transientChildren?has_content>
                <ul class="${clsHidden}">
                    <#list collection.transientChildren as child>
                    <@collectionListItem depth= (depth - 1) collection=child showOnlyVisible=showOnlyVisible  showBadge=showBadge />
                </#list>
                </ul>
            </#if>
        </li>
        </#if>
    </#macro>

<#-- emit the collections list in a 'treeview' control -->
    <#macro listCollections collections=resourceCollections_ showOnlyVisible=false showBadge=false>
    <ul class="collection-treeview">
        <#list collections as collection>
            <@collectionListItem collection=collection showOnlyVisible=showOnlyVisible depth=3 showBadge=showBadge />
        </#list>
  <#nested>
    </ul>
    </#macro>

<#-- emit the coding rules section for the current coding-sheet resource. Used on view page and edit page -->
    <#macro codingRules>
        <#if resource.id != -1>
            <#nested>
        <h3 onClick="$(this).next().toggle('fast');return false;">Coding Rules</h3>
            <#if resource.codingRules.isEmpty() >
            <div>
                No coding rules have been entered for this coding sheet yet.
            </div>
            <#else>
            <div id='codingRulesDiv'>
                <table width="60%" class="table table-striped tableFormat">
                    <thead class='highlight'>
                    <tr>
                        <th>Code</th>
                        <th>Term</th>
                        <th>Description</th>
                        <th>Mapped Ontology Node</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#assign hasSpecial=false />
                        <#list resource.sortedCodingRules as codeRule>
                        	<#if !(codeRule.code?starts_with("__")) >
	                        <tr>
	                            <td>${codeRule.code}</td>
	                            <td>${codeRule.term}</td>
	                            <td>${codeRule.description!""}</td>
	                            <td><#if codeRule.ontologyNode?has_content>${codeRule.ontologyNode.displayName!'Unlabeled'}</#if></td>
	                        </tr>
                            <#else>
                              <#assign hasSpecial=true />
	                    	</#if>
                        </#list>

                        <tr>
                        <td colspan=4><b>Special Coding Rules:</b> These entries are not in the coding-sheet, but represent edge-cases in Data Integration that may benefit from custom mappings</td>
                        </tr>
                       <#if hasSpecial>
                        <#list resource.sortedCodingRules as codeRule>
                        	<#if codeRule.code?starts_with("__") >
	                        <tr>
	                            <td></td>
	                            <td>${codeRule.term}</td>
	                            <td>${codeRule.description!""}</td>
	                            <td><#if codeRule.ontologyNode?has_content>${codeRule.ontologyNode.displayName!'Unlabeled'}</#if></td>
	                        </tr>
	                    	</#if>
                        </#list>
                        </#if>

                        <#list missingCodingKeys![]>
                        <tr>
                        <td colspan=4><b>The following coding rules exist in datasets mapped to this coding sheet, but are not in the coding sheet</b></td>
                        </tr>
                        <#items as missing>
                        <tr>
                            <td class="red">${missing}<#if missing?ends_with(" ")>(Note: this code has trailing spaces)</#if></td>
                            <td></td>
                            <td></td>
                            <td></td>
                        </tr>
                        </#items>
                        </#list>
                        	

                    </tbody>
                </table>
            </div>
            </#if>
        </#if>
    </#macro>


</#escape>

