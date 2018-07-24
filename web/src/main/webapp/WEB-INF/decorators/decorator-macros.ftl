<#macro layout_header>

<h1 id="top"><img src="${staticHost}/images/r4/bg-logo.png" title="tDAR - the Digital Archaeological Record" usemap="#tdarmap" alt="tDAR Logo">
    <map name="tdarmap">
        <area shape="rect" coords="0,0,187,65" href="/" alt="tDAR" tabindex="-1">
        <area shape="rect" coords="0,65,187,77" href="http://www.digitalantiquity.org/" alt="digital antiquity" tabindex="-1">
    </map>
</h1>
<#if (authenticatedUser??) >

<p id="welcome-menu" class="welcome  screen ">
    <@s.text name="menu.welcome_back"/> <a href="">${authenticatedUser.properName}
    <i class="caret drop-down"></i>
</a>
</p>

<div class="welcome-drop  screen ">

    <p>${authenticatedUser.properName}</p>

    <ul>
        <li><a href="<@s.url value="/contribute"/>"><@s.text name="menu.create_a_resource"/></a></li>
        <li><a href="<@s.url value="/project/add"/>"><@s.text name="menu.create_a_project"/></a></li>
        <li><a href="<@s.url value="/collection/add"/>"><@s.text name="menu.create_a_collection"/></a></li>
        <li><a href="<@s.url value="/dashboard"/>"><@s.text name="menu.dashboard"/></a></li>
        <li><a href="<@s.url value="/dashboard#bookmarks"/>"><@s.text name="menu.bookmarks"/></a></li>
    </ul>

    <ul>
        <li><a href="<@s.url value='/entity/user/myprofile'/>"><@s.text name="menu.my_profile"/></a></li>
        <li><a href="${commentUrlEscaped}?subject=tDAR%20comments"><@s.text name="menu.contact"/></a></li>
        <li>
             <form class="form-complete-inline seleniumIgnoreForm" id="frmMenuLogout" name="logoutFormMenu" method="post" action="/logout" >
                    <button type="submit" class="btn btn-link tdar-btn-link serif" name="logout" value="Logout">Logout</button>
             </form>
         </li>
    </ul>

    <#if administrator>
        <ul>
            <li><@s.text name="menu.admin_header"/></li>
            <li><a href="<@s.url value='/admin'/>"><@s.text name="menu.admin_main"/></a></li>
            <li><a href="<@s.url value='/admin/system/activity'/>"><@s.text name="menu.admin_activity"/></a></li>
            <li><a href="<@s.url value='/admin/searchindex/build'/>"><@s.text name="menu.admin_reindex"/></a></li>
        </ul>
    </#if>

</div>
</#if>

<nav>
    <ul class="hidden-phone-portrait">
<li><a href="https://www.tdar.org/saa/">SAA</a></li>
<li><a href="https://www.tdar.org/news/">News</a></li>
<li><a href="https://www.tdar.org/about">About</a></li>
<li><a href="https://www.tdar.org/using-tdar">Using ${siteAcronym}</a></li>

<!--        <li class="button hidden-phone"><a href="<@s.url value="/search/results"/>">BROWSE</a></li> -->
        <#if ((authenticatedUser.contributor)!true)>
            <li class="button hidden-phone"><a href="<@s.url value="/contribute"/>">UPLOAD</a></li></#if>
        <li>
            <#if navSearchBoxVisible>
                <form name="searchheader" id="searchheader" action="<@s.url value="/search/results"/>" class="inlineform form-horizontal seleniumIgnoreForm hidden-phone hidden-tablet  screen">
                <#-- fixme -- boostrap 3/4 should provide a better unstyled way to handle the magnifying glass -->
                    <input type="text" name="query" class="searchbox" accesskey="s" placeholder="Search ${siteAcronym} &hellip; "  value="${(query!'')?html}" maxlength="512">
                    <input type="hidden" name="_tdar.searchType" value="simple">
                ${(page.properties["div.divSearchContext"])!""}
                <div class="advanced container">
                <div class="row">
                <div class="span6 offset6 advancedSearchbox" id="advancedsearch" style="display: none">
                <h5>More search options</h5>
                <div class="control-group">
        <label class="control-label">What:</label>

        <div class="controls controls-row">
			<label class="radio inline">
			  <input type="radio" name="optionsRadios" id="optionsRadios1" value="RESOURCE" checked>  Resources
			</label>
			<label class="radio inline">
			  <input type="radio" name="optionsRadios" id="optionsRadios1" value="COLLECTION" checked>  Collections
			</label>
			<label class="radio inline">
			  <input type="radio" name="optionsRadios" id="optionsRadios1" value="PEOPLE" checked>  People
			</label>
			<label class="radio inline">
			  <input type="radio" name="optionsRadios" id="optionsRadios1" value="INSTITUTIONS" checked>  Institutions
			</label>
        </div>
                
    </div>

    <div class="searchgroup">
    <div class="groupingSelectDiv control-group fade" v-if="rows.length > 0">
        <label class="control-label">Include in results</label>
        <div class="controls controls-row">
            <select name="groups[0].operator" class="span5" style="display: none;">
                <option value="AND" selected="">When resource matches ALL terms below</option>
                <option value="OR">When resource matches ANY terms below</option>
            </select>
        </div>
    </div>
    <div id="groupTable0" class="grouptable repeatLastRow" style="width:100%">
    	<div class="control-group" v-for="(row,index) in rows">
			<part :index="index" :row="row" :options="selectOptions" @removerow="removeRow($event)" :totalrows="rows.length"/>
    	</div>
    </div>

    <div class="control-group add-another-control">
    	<div class="controls">
    		<button class="btn" id="groupTable0AddAnotherButton" type="button" @click="addRow()"><i class="icon-plus-sign"></i>add another search term</button>
		</div>
	</div>


<script type="text/x-template"   id="search-row-template">

    <div id="grouptablerow_0_" class="control-group termrow ">
    <select id="group0searchType_0_" name="groups[0].fieldTypes[0]" class="control-label searchType repeatrow-noreset" style="font-size:smaller">
    <option v-for="(option, index) in options" v-bind:value="option"> {{ option }} </option>
<!--                 <optgroup label="Basic Fields">
    <option value="TITLE">Title</option>
    <option value="DESCRIPTION">Description</option>
    <option value="CONTENTS">Full-Text</option>
    <option value="RESOURCE_CREATOR_PERSON">Person</option>
    <option value="RESOURCE_CREATOR_INSTITUTION">Institution</option>
    <option value="TDAR_ID">Id</option>
    <option value="COVERAGE_DATE_CALENDAR">Calendar Dates</option>
    <option value="COVERAGE_DATE_RADIOCARBON">RadioCarbon Dates</option>
    <option value="PROJECT">Project</option>
    <option value="COLLECTION">Collection</option>
    <option value="FILENAME">File Name</option>
    <option value="DATE_CREATED">Year</option>
    <option value="DATE_REGISTERED">Date Created</option>
    <option value="DATE_UPDATED">Date Updated</option>
                        </optgroup>
                <optgroup label="Controlled Keywords">
    <option value="KEYWORD_INVESTIGATION">Investigation Types</option>
    <option value="KEYWORD_SITE">Site Type(Controlled)</option>
    <option value="KEYWORD_MATERIAL">Material Types</option>
    <option value="KEYWORD_CULTURAL">Culture Keywords</option>
                        </optgroup>
                <optgroup label="Freeform Keywords">
    <option value="FFK_GEOGRAPHIC">Geographic Keywords</option>
    <option value="FFK_SITE">Site Names</option>
    <option value="FFK_SITE_TYPE">Site Type</option>
    <option value="FFK_CULTURAL">Culture Keywords</option>
    <option value="FFK_MATERIAL">Material Keywords</option>
    <option value="FFK_TEMPORAL">Temporal Keywords</option>
    <option value="FFK_GENERAL">General Keywords</option>
    </optgroup>-->
    </select>
        <div class="controls controls-row simple multiIndex">
            <div class="span term-container">
                            <span class="term">
                                <input type="text" name="groups[0].allFields[0]" class="input">
								</span>
			    <button class="btn  btn-mini " @click="clearRow()" type="button" tabindex="-1"><i class="icon-trash"></i></button>
            </div>
        </div>
    </div>

</script>

                </div>
                
                
                </div>
                </div>
                </div>
                </div>
                </form>
                <script>
               $("#searchheader").mouseover(function() {
	               $("#advancedsearch").show();
               });
               $("#searchheader").mouseout(function() {
	               $("#advancedsearch").hide();
               });
                </script>

<script>
$(document).ready(function() { 
        var _fpart = Vue.component('part', {
            template : "#search-row-template",
            props : [ "row", "index" , "options", "totalrows" ],
            data : function() {
                return {
                }
            },
            mounted : function() {},
            methods: {
            	reset: function() {
            	
            	},
	            clearRow: function() {
		            console.log(this.index);
		            if (this.index == 0 && this.totalrows == 1) {
		            	this.reset();
		            } else {
		            	this.$emit("removerow", this.index);
	            	}
	            }
            }
		});
        var app = new Vue({
	            el : "#advancedsearch",
	            data : {
	            	selectOptions: ["title","description","full-text","project","collection","site name"],
	            	rows: [{}]
	            },
	            computed: {},
	            methods: {
		            addRow: function() {
		            	this.rows.push({});
		            },
		            removeRow: function(idx) {
		            	this.rows.splice(idx, 1);

		            }
	            },
            });
});
            
</script>
            </#if>
        </li>
    </ul>

</nav>


</#macro>

<#macro homepageHeader>
    <div class="row">
        <div class="hero">
<h2>What can you dig up?</h2>

<p><strong>The Digital Archaeological Record (tDAR)</strong> is your online archive <br/>for archaeological information.</p>

<form name="searchheader" action="<@s.url value="/search/results"/>" class="searchheader">
    <input type="text" name="query" placeholder="Find archaeological data..." accesskey="s" class="searchbox input-xxlarge">
    <a href="<@s.url value="/search"/>">advanced</a>
    <input type="hidden" name="_tdar.searchType" value="simple">
</form>

        <@auth.loginMenu true/>
        </div>
        <ul class="inline-menu hidden-desktop"><@auth.loginMenu false/></ul>
    </div>


<div class="row">
    <div class="span3 bucket">
        <img src="${staticHost}/images/r4/icn-data.png" alt="Access / Use" title="Access / Use" />

        <h3><a href="http://www.tdar.org/why-tdar/data-access/">Access &amp; Use</a></h3>

        <p style="min-height:4em">Broadening the access to archaeological data through simple search and browse functionality.</p>

        <p>
            <a href="http://www.tdar.org/why-tdar/data-access/" class="button">Learn More</a>
        </p>
    </div>
    <div class="span3 bucket">
        <img src="${staticHost}/images/r4/icn-pres.png" alt="Preservation" title="Preservation" />

        <h3><a href="http://www.tdar.org/why-tdar/preservation/">Preservation</a></h3>

        <p style="min-height:4em">Dedicated to ensuring long-term preservation of digital archaeological data.</p>

        <p>
            <a href="http://www.tdar.org/why-tdar/preservation/" class="button">Learn More</a>
        </p>
    </div>
    <div class="span3 bucket">
        <img src="${staticHost}/images/r4/icn-stew.png" alt="Stewardship" title="Stewardship"/>

        <h3><a href="http://www.tdar.org/why-tdar/contribute/">Upload Resources</a></h3>

        <p style="min-height:4em">Contribute documents, data sets , images, and other critical archaeological materials.</p>

        <p>
            <a href="http://www.tdar.org/why-tdar/contribute/" class="button">Learn More</a>
        </p>
    </div>
    <div class="span3 bucket">
        <img src="${staticHost}/images/r4/icn-uses.png" alt="Use" title="Use" />

        <h3><a href="http://www.tdar.org/using-tdar/">Who Uses tDAR</a></h3>

        <p style="min-height:4em">Researchers like you. Uncover knowledge of the past, and preserve and protect archaeological resources.</p>

        <p>
            <a href="http://www.tdar.org/using-tdar/" class="button">Learn More</a>
        </p>
    </div>
</div>
</#macro>

<#macro subnav>
<#if (subnavEnabled!true)>
<div class="subnav-section">
    <div class="container">
        <div class="row">
            <div class="span12 subnav">
                <ul class="subnav-lft">
                    <li><a href="<@s.url value="/search"/>"><@s.text name="menu.search"/></a></li>
                    <li><a href="<@s.url value="/browse/explore"/>"><@s.text name="menu.explore"/></a></li>
                    <#if sessionData?? && sessionData.authenticated>
                        <li><a href="<@s.url value="/dashboard"/>"><@s.text name="menu.dashboard"/></a></li>
<!--
                        <li><a href="<@s.url value="/organize"/>"><@s.text name="menu.organize"/></a></li>
                        <li><a href="<@s.url value="/manage"/>"><@s.text name="menu.manage"/></a></li>
                        <li><a href="<@s.url value="/billing"/>"><@s.text name="menu.billing"/></a></li>

-->                        <li><a href="<@s.url value="/workspace/list"/>"><@s.text name="menu.integrate"/></a></li>
                        <#if editor>
                            <li><a href="<@s.url value="/admin"/>"><@s.text name="menu.admin"/></a></li>
                        </#if>
                    </#if>
                </ul>
                <#if actionName!='login' && actionName!='register' && actionName!='download' && actionName!='review-unauthenticated'>
                    <@auth.loginMenu true />
                </#if>
            </div>
        </div>
    </div>
</div>
</#if>
</#macro>
