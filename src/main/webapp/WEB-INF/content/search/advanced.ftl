<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>

<#macro selectYearType yearTypeOption>
	<#if yearType?? && (yearType == yearTypeOption)>
		selected="selected"
	</#if>
</#macro>

<#macro userAutoCompleteTable namePrefix='user' label="User" registeredUser=true nameFieldsOnly=false>
<#assign _autocompleteType = 'userAutoComplete' />
<#if !registeredUser>
<#assign _autocompleteType='nameAutoComplete'>
</#if>

<h4>${label}</h4>
<table id="${namePrefix}SearchTable" class="tableFormat width99percent" >
<tbody>
<tr id='${namePrefix}_0_'>
<td>
    <div class="width30percent marginLeft10" >
        <@s.hidden name='${namePrefix}Ids[0]' cssClass="" onchange="this.valid()"  />
        <@s.textfield cssClass="watermarked ${_autocompleteType}" watermark="Last Name" 
            name="${namePrefix}.lastName" maxlength="255" onblur="setCalcUserVal(this)" /> 
        <@s.textfield cssClass="watermarked ${_autocompleteType}" watermark="First Name" 
            name="${namePrefix}.firstName" maxlength="255" onblur="setCalcUserVal(this)"  />
        <#if !nameFieldsOnly>
        <@s.textfield cssClass="watermarked ${_autocompleteType}" watermark="Email"
            name="${namePrefix}.email" maxlength="255"/>
        <br />
        </#if>
    </div>
    <#if !nameFieldsOnly>
    <div class="width99percent marginLeft10">
        <@s.textfield cssClass="watermarked ${_autocompleteType}" watermark="Institution Name"
            name="${namePrefix}.institution.name" maxlength="255" onblur="setCalcUserVal(this)" />
    </div>
    </#if>
</td>
<td>
<@edit.clearDeleteButton id="readUserRow" />
</td>
</tr>
</tbody>
</table>
<script>
<#if authenticated>
$(document).ready(function() {
    delegateCreator("#${namePrefix}SearchTable",${registeredUser?string},false);
});
</#if>
$(window).bind("pageshow", function() {
  $("#searchButton").removeAttr('disabled');
});

</script>
</#macro>


<head>
<title>Advanced Search</title>
<script type="text/javascript" src="<@s.url value='/includes/advanced-search.js'/>"></script>
<script type="text/javascript">
    $(document).ready(function() {
        $("#searchButton").removeAttr('disabled');
        $('#searchTipToggle').click(function(){
            $("#divSearchTips").slideToggle();
            return false;
        });
    });

</script>

<style type="text/css">
    h3 {font-weight:bold; color:#333; margin-top:2em}
</style>

<@edit.resourceJavascript "#searchForm" />
</head>


<body>
<@edit.showControllerErrors />

<div id="divSearchTips" class="glide" style="display:none">
    <div id="divSearchTipsContainer" >    
        <h3>Search Tips</h3>
        <a href="#" onclick="$('#divSearchTips').slideToggle(); return false;" style="float:right">(Close this section)</a>
        <br clear=all/>
        <dl>
            <dt>Boolean Operators</dt>
            <dd>
                You can use boolean operators such as AND and OR. For example, the following search query:
                <blockquote>
                    <code>
                        Archaic <em>AND</em> Mogollon
                    </code>
                </blockquote>         
                This will return only those resources which contain BOTH "Archaic" and "Mogollon" for the field you have specified. Using OR returns 
                results that contain "Archaic," "Mogollon," or both terms.
                
            </dd>
            
            <dt>Quoted Terms</dt>
            <dd>
            
                Place multiple terms in quotes to return results that contain the specified terms in that order. For example, searching for the phrase
                <blockquote>
                    <code>
                        They were selected from
                    </code>
                </blockquote>               
                will likely return many pages of results,   however,  placing the term in quotes
                <blockquote>
                    <code>&quot;They were selected from&quot;</code>
                </blockquote>
                will narrow down your search results significantly.
            </dd>
            
            <dt>Grouping terms in parenthesis</dt>
            <dd> 
                You can combine the aforementioned techniques using parentheses to create powerful, targeted searches. For example, if you want to write a 
                search query that returns all of the results from the first two techniques, you could enter the following query:
                <blockquote>
                    <code>
                        (Archaic <em>AND</em> Mogollon) OR ("They were selected from")
                    </code>
                </blockquote>
            </dd>
            
        </dl>
    </div>
</div>
<div class="glide">
       


    <@s.form id='searchForm' method='GET' action='results'>
    <p>Find resources in tDAR by providing information in the fields below and clicking the &quot;Search&quot; button. (<a  href="#" id="searchTipToggle">Click here 
    to show/hide search tips...</a>) </p>
    <@search.queryField freeTextLabel="Search all fields" showAdvancedLink=false>
    <br/>
      <label for="title_s">Title:</label> <input id="title_s" type="text" name="title" value="${title!}" class="longfield" />
      <br/>
      <label for="tdarid">TDAR id:</label><input id="tdarid" type="text" class="number" name="id" value="<#if id?? && id?is_number && id != 0>${(id?c)!}</#if>" />
      <br/>
      <label for="projectList">Project:</label>
      <select name="projectIds[0]" id="projectList">
          <option value="" selected='selected'>All Projects</option>
          <@s.iterator value='projects' status='projectRowStatus' var='project'>
            <option truncate='80' value="${project.id?c}">${project.selectOptionTitle}</option>
          </@s.iterator>
      </select>
      <br/>
    </@search.queryField>
    <br/>
    
        <div id="divTemporalLimits" tiplabel="Temporal Limits" tooltipcontent="#divTemporalLimitTooltip">
        	<h3>Temporal Limits</h3>
        	<select name="yearType" id="yearTypeSelect">
        		<option value="NONE" <@selectYearType yearTypeOption="NONE" />>No Limit</option>
        		<option value="CALENDAR_DATE" <@selectYearType yearTypeOption="CALENDAR_DATE" />>Calendar Years</option>
        		<option value="RADIOCARBON_DATE" <@selectYearType yearTypeOption="RADIOCARBON_DATE" />>Radiocarbon Years</option>
        	</select>
        	From <input type="text" id="fromYear" name="fromYear" value="${(fromYear?c)!}">
        	To <input type="text" id="toYear" name="toYear" value="${(toYear?c)!}">
    	</div>
    
        <div id="divTemporalLimitTooltip" class="hidden">
            <div>
                Limit search results to a date range.
                <dl>
                    <dt>Calendar Date</dt>
                    <dd>Enter a beginning and ending Julian calendar year.  Use negative numbers for BC dates</dd>
                    <dt>Radiocarbon Date </dt>
                    <dd>Enter a maximum/minimum radiocarbon age.</dd>
                </dl>
            </div>
        </div>
        <br/>   
    
    	<h3>Spatial Limits</h3>
    	<div><#-- placeholder div to make sitemesh parser not pickup div id to replace -->
    	<div id='large-google-map' style="height:450px;"></div> 	
    	<input type="hidden" name="minx" id="minx" value="${(minx?c)!}">
    	<input type="hidden" name="maxx" id="maxx" value="${(maxx?c)!}">
    	<input type="hidden" name="miny" id="miny" value="${(miny?c)!}">
    	<input type="hidden" name="maxy" id="maxy" value="${(maxy?c)!}">
        </div>
    
    <h3>Investigation Types</h3>
    <@s.checkboxlist name='investigationTypeIds' list='allInvestigationTypes' listKey='id' listValue='label' numColumns=2 cssClass="smallIndent" 
        listTitle="definition" />
        
        
    <h3>Site Information</h3>
    <div id="divSiteInformation" 
            tiplabel="About Your Site(s)" 
            tooltipcontent="Keyword list: Enter site name(s) and select feature types discussed in the document. Use the Other field if needed.">
        <label>Site Name</label>
        <table id="siteNameKeywordTable" class="field" addAnother="add another site name">
        <tbody>
        <@s.iterator status='rowStatus' value='siteNameKeywords'>
        <tr id='siteNameKeywordRow_${rowStatus.index}_'>
        <td>
        <@s.textfield name='siteNameKeywords[${rowStatus.index}]' cssClass="longfield"/>
        </td><td>
        </td>
        </tr>
        </@s.iterator>
        </tbody>
        </table>
        
        <br/>
        <label>Site Type</label>
        
        
        <table id="siteTypeKeywordTable" class="field">
        <tbody>
            <tr><td><@s.checkboxlist theme="hier" name="approvedSiteTypeKeywordIds" keywordList="approvedSiteTypeKeywords" /></td></tr>
        </tbody>
        </table>
        
        <label>Other</label>
        <@s.iterator status='rowStatus' value='uncontrolledSiteTypeKeywords'>
        <@s.textfield name='uncontrolledSiteTypeKeywords[${rowStatus.index}]' cssClass="longfield"/>
        </@s.iterator>
    </div>
     
        
        
    
    <h3>Material Type(s)</h3>
    <@s.checkboxlist name='materialKeywordIds' list='allMaterialKeywords' listKey='id' listValue='label' listTitle="definition"
        numColumns=3 cssClass="smallIndent" />
    
    <div 
        tiplabel="Cultural Terms"
        tooltipcontent="Keyword list: Select the archaeological &quot;cultures&quot; discussed in the document. Use the Other field if needed.">
        <h3>Cultural Term(s)</h3>
        <div id="divCulturalInformation">
            <label>Culture</label>
            <table id="cultureKeywordTable" class="field">
                <tbody>
                <tr><td><@s.checkboxlist theme="hier" name="approvedCultureKeywordIds" keywordList="approvedCultureKeywords" /></td></tr>
                </tbody>
            </table>
            
            <br />
            <label>Other</label>
            <@s.iterator status='rowStatus' value='uncontrolledCultureKeywords'>
            <@s.textfield name='uncontrolledCultureKeywords[${rowStatus.index}]' cssClass='longfield' />
            </@s.iterator>
        </div>
    </div>
    
    <br/>
    <div id="divPersonSearch">
        <h3>Search by Submitter / Author / Contributor</h3>
        <@userAutoCompleteTable label="Submitter" namePrefix="searchSubmitter"  />
        <br />
        <@userAutoCompleteTable label="Author or Contributor" namePrefix="searchContributor" registeredUser=false nameFieldsOnly=!authenticated />
    </div>
    
    <#if administrator?? && administrator>
    <div id="divAdminSearchOptions">
        <h3>Administrator Search Options</h3>
        
        <h4>Status</h4>
        <@s.checkboxlist name='includedStatuses' list='allStatuses'  listValue='label'  />
    
        <script type="text/javascript"> 
            $('#updatedAfter,#updatedBefore').datepicker({dateFormat: 'm/d/y'});
        </script>
    </div>
    </#if>
    
    <h3>Sorting Options and Submit</h3>
    <label for="sortField">Sort By:</label>
     <@search.sortFields />
    <br/>
    <div id="error">
    </div>
    <div>
        <@s.submit id="searchButton" value="Search" /> <input type='button' value='Reset' id='formResetButton' />
    </div>
    </@s.form>
</div>

<div id="sidebar" parse="true">
    <div id="notice">
    <h3>Introduction</h3>
    This is the advanced search page.  Hover over a specific field for more information.
    </div>
</div>

