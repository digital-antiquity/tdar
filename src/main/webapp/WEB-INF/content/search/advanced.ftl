<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>

<#macro selectYearType yearTypeOption>
	<#if yearType?? && (yearType == yearTypeOption)>
		selected="selected"
	</#if>
</#macro>

<#macro userAutoCompleteTable namePrefix='user' label="User" registeredUser=true>
<#assign _autocompleteType = 'userAutoComplete' />
<#if !registeredUser>
<#assign _autocompleteType='nameAutoComplete'>
</#if>

<h4>${label}</h4>
<table id="${namePrefix}SearchTable" class="tableFormat width99percent repeatLastRow" addAnother="add another read-only user">
<tbody>
<tr id='${namePrefix}_0_'>
<td>
    <div class="width30percent marginLeft10" >
        <@s.hidden name='${namePrefix}Ids[0]' cssClass="rowNotEmpty" onchange="this.valid()"  />
        <@s.textfield cssClass="watermarked ${_autocompleteType}" watermark="First Name" 
            name="${namePrefix}[0].firstName" maxlength="255" onblur="setCalcUserVal(this)"  />
        <@s.textfield cssClass="watermarked ${_autocompleteType}" watermark="Last Name" 
            name="${namePrefix}[0].lastName" maxlength="255" onblur="setCalcUserVal(this)" /> 
        <@s.textfield cssClass="watermarked ${_autocompleteType}" watermark="Email"
            name="${namePrefix}[0].email" maxlength="255"/>
        <br />
    </div>
    <div class="width99percent marginLeft10">
        <@s.textfield cssClass="watermarked ${_autocompleteType}" watermark="Institution Name"
            name="${namePrefix}[0].institution.name" maxlength="255" onblur="setCalcUserVal(this)" />
    </div>
</td>
<td>
<@edit.clearDeleteButton id="readUserRow" />
</td>
</tr>
</tbody>
</table>
    
</#macro>


<head>
<title>Advanced Search</title>
<script src="<@s.url value='/includes/advanced-search.js'/>" type='text/javascript'></script>

<style type="text/css">
    h3 {font-weight:bold; color:#333; margin-top:2em}
</style>

<@edit.resourceJavascript />
</head>


<body>
<div class="glide">
<@s.form id='searchForm' method='GET' action='results'>

<@search.queryField freeTextLabel="Search all fields" showAdvancedLink=false>
  <label for="title">Title:</label> <input id="title" type="text" name="title" value="${title!}" />
  <br/>
  <label for="tdarid">TDAR id:</label><input id="tdarid" type="text" class="number" name="id" value="<#if id?? && id != 0>${(id?c)!}</#if>" />
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

    <div id="divTemporalLimits">
    	<h3>Temporal Limits</h3>
    	<select name="yearType" id="yearTypeSelect">
    		<option value="NONE" <@selectYearType yearTypeOption="NONE" />>No Limit</option>
    		<option value="CALENDAR_DATE" <@selectYearType yearTypeOption="CALENDAR_DATE" />>Calendar Years</option>
    		<option value="RADIOCARBON_DATE" <@selectYearType yearTypeOption="RADIOCARBON_DATE" />>Radiocarbon Years</option>
    	</select>
    	From <input type="text" id="fromYear" name="fromYear" value="${(fromYear?c)!}">
    	To <input type="text" id="toYear" name="toYear" value="${(toYear?c)!}">
	</div>

    <div tooltipfor="divTemporalLimits" class="hidden">
        <h2>Temporal Limits</h2>
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
<div id="divSiteInformation">
    <label>Site Name</label>
    <table id="siteNameKeywordTable" class="field" addAnother="add another site name" 
        tiplabel="About Your Site(s)"
        tooltipcontent="Keyword list: Enter site name(s) and select feature types discussed in the document. Use the Other field if needed.">
    <tbody>
    <@s.iterator status='rowStatus' value='siteNameKeywords'>
    <tr id='siteNameKeywordRow_${rowStatus.index}_'>
    <td>
    <@s.textfield name='siteNameKeywords[${rowStatus.index}]' />
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
    <@s.textfield name='uncontrolledSiteTypeKeywords[${rowStatus.index}]'/>
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
        <@s.textfield name='uncontrolledCultureKeywords[${rowStatus.index}]' cssClass='shortfield' />
        </@s.iterator>
    </div>
</div>

<br/>

<#if administrator?? && administrator>
<div id="divAdminSearchOptions">
    <h3>Administrator Search Options</h3>
    
    <@userAutoCompleteTable label="Submitter" namePrefix="searchSubmitter" />
    <br /><@userAutoCompleteTable label="Contributor" namePrefix="searchContributor" registeredUser=false />
    <!-- <br /><@s.textfield name="updatedAfter" id="updatedAfter" label="Updated After"  labelPosition="left"  cssClass='shortField date'/> -->
    <!--<br /><@s.textfield name="updatedBefore" id="updatedBefore" label="Updated Before"  labelPosition="left"  cssClass='shortField date'/> -->
    
    <h4>Status</h4>
    <@s.checkboxlist name='includedStatuses' list='allStatuses'  listValue='label'  />

<script type="text/javascript"> 
    $('#updatedAfter,#updatedBefore').datepicker({dateFormat: 'm/d/y'});
</script>
</#if>

<h3>Sorting Options and Submit</h3>
<label for="sortField">Sort By:</label>
 <@search.sortFields />
<br/>
<div id="error">
</div>
<div>
<@s.submit value="Search" /> <input type='button' value='Reset' id='formResetButton' />
</div>
</@s.form>
</div>

<div id="sidebar" parse="true">
    <div id="notice">
    <h3>Introduction</h3>
    This is the advanced search page.  Hover over a specific field for more information.
    </div>
</div>
</body>
