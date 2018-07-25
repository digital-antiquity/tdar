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
    <ul class="hidden-phone-portrait navmenu">
<li><a href="https://www.tdar.org/saa/">SAA</a></li>
<li><a href="https://www.tdar.org/news/">News</a></li>
<li><a href="https://www.tdar.org/about">About</a></li>
<li><a href="https://www.tdar.org/using-tdar">Using ${siteAcronym}</a></li>

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
                <div class="span6 offset6 advancedSearchbox" id="advancedsearch" ignore style="display: none">
                <h5>More search options</h5>
                <div class="control-group condensed">
        <label class="control-label">What:</label>

        <div class="controls controls-row">
			<label class="radio inline">
			  <input type="radio" name="optionsRadios" id="optionsRadios1" value="RESOURCE">  Resources
			</label>
			<label class="radio inline">
			  <input type="radio" name="optionsRadios" id="optionsRadios1" value="COLLECTION">  Collections
			</label>
			<label class="radio inline">
			  <input type="radio" name="optionsRadios" id="optionsRadios1" value="PEOPLE">  People
			</label>
			<label class="radio inline">
			  <input type="radio" name="optionsRadios" id="optionsRadios1" value="INSTITUTIONS" >  Institutions
			</label>
        </div>
                
    </div>

    <div class="searchgroup">
    <div id="groupTable0" class="grouptable condensed repeatLastRow" style="width:100%">
    	<div class="control-group condensed" v-for="(row,index) in rows">
			<part :index="index" :row="row" :options="selectOptions" @removerow="removeRow($event)" :totalrows="rows.length"/>
    	</div>
    </div>

    <div class="control-group condensed add-another-control">
    	<div class="controls">
    		<button class="btn" id="groupTable0AddAnotherButton" type="button" @click="addRow()"><i class="icon-plus-sign"></i>add another search term</button>
		</div>
	</div>
    <div class=" control-group " v-if="rows.length > 1" >
        <label class="control-label">Include in results</label>
        <div class="controls controls-row condensed">
            <select name="groups[0].operator" class="span3" >
                <option value="AND" selected="">When resource matches ALL terms below</option>
                <option value="OR">When resource matches ANY terms below</option>
            </select>
        </div>
    </div>
    
    <p class="text-center">
	<button type="button" class="button btn tdar-button center">Search</button>
	</p>


                </div>
                
                
                </div>
                </div>
                <script type="text/x-template"   id="search-row-template">
    <div id="grouptablerow_0_" class="control-group termrow condensed ">

    <select id="group0searchType_0_" v-model="option" name="groups[0].fieldTypes[0]" class="control-label searchType repeatrow-noreset" style="font-size:smaller">
        <optgroup v-for="(group, idx) in getOptionGroups()" :label="group">
        <option v-for="(option, index) in getOptionsFor(group)" v-bind:value="option"> {{ option.name }}  </option>
        </optgroup>
    </select>
        <div class="controls controls-row simple multiIndex condensed">
            <div class="span term-container">
                <span v-if="option.type == 'basic'">
                {{option.result}}
				  <autocomplete :url="option.autocompleteUrl" :suffix="option.autocompleteSuffix"  :field="option.fieldName" v-if="option.autocompleteUrl != undefined" :resultsuffix="option.resultSuffix" ref="autocomplete" />
                    <input type="text" name="groups[0].allFields[0]" class="input" v-if="option.autocompleteUrl == undefined">
                </span>
                <span v-if="option.type == 'select'">
                    <select name="name" multiple>
                        <option v-for="(opt, i) in option.values">{{opt}}</option>
                    </select>
                </span>
                <span v-if="option.type == 'integer'">
                    <input type="number" name="groups[0].allFields[0]" class="input">
                </span>
                <span v-if="option.type == 'date'">
                    <input type="date" name="groups[0].allFields[0]" class="input">
                </span>
                <div v-if="option.type == 'map'">
                    <div id="latlongoptions">
				        <div id='map' class='leaflet-map-editable span3' data-search="true">
				            <span class="latlong-fields">
				            	<input type="hidden" name="groups[0].latitudeLongitudeBoxes[0].east" id="maxx" class="ne-lng latLongInput maxx" />
				                <input type="hidden" name="groups[0].latitudeLongitudeBoxes[0].south"  id="miny" class="sw-lat latLongInput miny" />
				                <input type="hidden" name="groups[0].latitudeLongitudeBoxes[0].west" id="minx" class="sw-lng latLongInput minx" />
				                <input type="hidden" name="groups[0].latitudeLongitudeBoxes[0].north"  id="maxy" class="ne-lat latLongInput maxy" />
				            </span>
				            <div class="mapdiv"></div>
				        </div>
			        </div>

                </div>
            </div>

            <div class="row text-center">
    			    <button class="btn  btn-mini " @click="clearRow()" type="button" tabindex="-1"><i class="icon-trash"></i></button>
		    </div>
        </div>
    </div>

</script>


<script type="text/x-template" id="autocomplete">
  <div class="autocomplete">
    <input type="text" @input="onChange" v-model="search" @keyup.down="onArrowDown" @keyup.up="onArrowUp" v-on:keyup.enter.self.stop="onEnter" ref="searchfield" class="input" />
    <ul id="autocomplete-results" v-show="isOpen" class="autocomplete-results">
      <li class="loading" v-if="isLoading" :style="getStyleWidth()">
        Loading results...
      </li>
      <li v-else v-for="(result, i) in results" :key="i" @click="setResult(result)" class="autocomplete-result" :class="{ 'is-active': i === arrowCounter }" :style="getStyleWidth()">
      <span v-html="render(result)" v-if="isCustomRender()"></span>
      <span v-if="!isCustomRender()">{{ getDisplay(result) }}  ({{ result.id}})</span>
      </li>
      <li class="autocomplete-result" v-if="!isLoading" :style="getStyleWidth()">
        Showing 1-{{recordsPerPage}} of {{totalRecords}}
      </li>
    </ul>

  </div>
</script>

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
/**
**/
                
                </script>

<script>
$(document).ready(function() {
// https://alligator.io/vuejs/vue-autocomplete-component/
      var autocomplete = Vue.component('autocomplete', {
  name: "autocomplete",
  template: "#autocomplete",
  props: {
    items: {
      type: Array,
      required: false,
      default: () => []
    },
    isAsync: {
      type: Boolean,
      required: false,
      default: true
    },
    url: {
    	type: String
    },
    suffix: {},
    field: {
    	type: String,
    	required: true
    },
    render: {
    	type: Object
    },
    valueprop: {
    	type: String
    },
    resultsuffix: {
    	type: String
	}
  },

  data: function() {
    return {
      isOpen: false,
      results: [],
      search: "",
      searchObj: {},
      isLoading: false,
      width: 100,
      arrowCounter: 0,
      totalRecords:0,
      recordsPerPage: 25,
      cancelToken: undefined
    }
  },
  methods: {
    getStyleWidth: function() {
  	  return "width: " + (this.width - 8)+ "px";
  	}, 
  	isCustomRender: function() {
      if (this.render != undefined && typeof this.render  === 'function') {
      	return true;
      }
      return false;
    },
    getDisplay: function(obj) {
    if (obj == undefined) { return ''; }
      if (this.valueprop != undefined) {
      	return obj['valueprop'];
      }
      var ret =  "";
    	if (obj.name != undefined) {
    		ret =  obj.name;
		} else if (obj.title != undefined) {
			ret = obj.title;
		} else if (obj.label != undefined) {
			ret = obj.label;
		} else if (obj.properName != undefined) {
			ret = obj.properName;
		};
		return ret;
			
    },
    onChange: function() { // Let's warn the parent that a change was made
      this.$emit("input", this.search);

      // Is the data given by an outside ajax request?
      if (this.isAsync) {
        this._setResult();
        var self = this;
       if (this.search != undefined && this.search.length > 0) {
        this.isLoading = true;
        if (this.cancelToken != undefined) {
        	//console.log('cancelling...', this.cancelToken);
            this.cancelToken.cancel();
        }
        
        Vue.set(this, "cancelToken" ,axios.CancelToken.source());

        var searchUrl = this.url + "?" + this.field + "=" + this.search + "&" + this.suffix;
        console.log(searchUrl);
        Vue.set(self, "totalRecords", 0);
        Vue.set(self, "recordsPerPage", 25);
        axios.get(searchUrl, { cancelToken: self.cancelToken.token }).then(function(res) {
	        Vue.set(self, "isLoading",false);
	        Vue.set(self, 'results',res.data[self.resultsuffix]);
	        console.log(res);
	        Vue.set(self, "totalRecords", res.data.status.totalRecords);
	        if (res.data.status.totalRecords < res.data.status.recordsPerPage) {
		        Vue.set(self, "recordsPerPage", self.totalRecords);
	        } else {
		        Vue.set(self, "recordsPerPage", res.data.status.recordsPerPage);
	        }
        }).catch(function(thrown) {
            if (axios.isCancel(thrown)) {
                console.log('First request canceled', thrown.message);
            } else {
            	console.error(thrown);
            }
        });
        this.isOpen = true;
        } else {
        this.isOpen = false;
        Vue.set(self, "isLoading",false);
        Vue.set(self, 'results',[]);
        
        }
      } else {
        // Let's search our flat array
        this.filterResults();
        this.isOpen = true;
      }
    },
    filterResults: function() {
      // first uncapitalize all the things
      this.results = this.items.filter(item => {
        return item.toLowerCase().indexOf(this.search.toLowerCase()) > -1;
      });
    },
    _setResult: function(result) {
      this.searchObj = result;
      this.$emit("setvaluelabel", this.search);
      if (result != undefined && result.id != undefined) {
	      this.$emit("setvalueid", result.id);
      } else {
	      this.$emit("setvalueid", '');
      }
	},
	clear: function() {
		this.searchObj = undefined;
		this.search= '';
	},
    setResult: function(result) {
      this.search = this.getDisplay(result);
      this._setResult(result);
      console.log(result);
      this.isOpen = false;
    },
    onArrowDown: function(evt) {
      if (this.arrowCounter < this.results.length) {
        this.arrowCounter = this.arrowCounter + 1;
      }
    },
    onArrowUp: function() {
      if (this.arrowCounter > 0) {
        this.arrowCounter = this.arrowCounter - 1;
      }
    },
    onEnter: function(e) {
      // make sure you can clear the value with setting 
      if (this.arrowCounter > -1 || (this.search == '' || this.search == undefined)) {
        this.setResult( this.results[this.arrowCounter]);
      }
      this.isOpen = false;
      this.arrowCounter = -1;
    },
    handleClickOutside: function(evt) {
      if (!this.$el.contains(evt.target)) {
        this.isOpen = false;
        this.arrowCounter = -1;
      }
    }
  },
  watch: {
    items: function(val, oldValue) {
      // actually compare them
      if (val.length !== oldValue.length) {
        this.results = val;
        this.isLoading = false;
      }
    }
  },
  mounted: function() {
    Vue.set(this, 'width',this.$refs['searchfield'].offsetWidth);
    document.addEventListener("click", this.handleClickOutside);
  },
  destroyed: function() {
    document.removeEventListener("click", this.handleClickOutside);
  }
});

        var _part = Vue.component('part', {
            template : "#search-row-template",
            props : [ "row", "index" , "options", "totalrows" ],
            data : function() {
                return {
                option:'',
                value:''
                }
            },
            watch: {
            	option: function(n, o) {
            		this.reset();
            		if (n.type == 'map') {
            		Vue.nextTick(function () {
			            TDAR.leaflet.initEditableLeafletMaps();
					});
            		}
            	}
            },
            mounted : function() {},
            methods: {
            	reset: function() {
            		if (this.$refs.autocomplete != undefined) {
            			this.$refs.autocomplete.clear();
            		}
            	},
                getOptionsFor: function(group) {
                   var ret = new Array();
                   this.options.forEach(function(e) {
                     if (group == e.group) {
                         ret.push(e);
                     }
                   });
                   return ret;                
                },
                getOptionGroups: function() {
                   var ret = {};
                   this.options.forEach(function(e) {
                     ret[e.group]= 1;
                   });
                   return Object.keys(ret);
                },
	            clearRow: function() {
		            if (this.index == 0 && this.totalrows == 1) {
		            	this.reset();
		            } else {
		            	this.$emit("removerow", this.index);
	            	}
	            }
            }
		});
		/**
		
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
    **/

        var app = new Vue({
	            el : "#advancedsearch",
	            data : {
	            	selectOptions: [
                        { name: 'Title', group: 'general', type:'basic' },
                        { name: 'Description', group: 'general', type:'basic' },
                        { name: 'Full-Text', group: 'general', type:'basic' },
                        { name: 'Date', group: 'general', type:'integer' },
                        { name: 'Id', group: 'general', type:'integer' },
                        { name: 'Date Added', group: 'general', type:'date' },
                        { name: 'Date Updated', group: 'general', type:'date' },
                        { name: 'Map', group: 'general', type:'map' },
                        { name: 'Project', group: 'general', type:'basic', autocompleteUrl: '/api/lookup/resource', autocompleteSuffix: 'resourceTypes[0]=PROJECT', fieldName: 'term', resultSuffix: 'resources'  },
                        { name: 'Collection', group: 'general', type:'basic', autocompleteUrl: '/api/lookup/collection', fieldName: 'term', resultSuffix: 'collections' },
                        { name: 'Person', group: 'general', type:'basic', autocompleteUrl: '/api/lookup/person', fieldName: 'term', resultSuffix: 'people' },
                        { name: 'Institution', group: 'general', type:'basic', autocompleteUrl: '/api/lookup/institution', fieldName: 'institution', resultSuffix: 'institutions' },
                        { name: 'Site Name', group: 'keywords', type:'basic', autocompleteUrl: '/api/lookup/keyword', autocompleteSuffix: 'keywordType=SiteNameKeyword', fieldName: 'term', resultSuffix: 'items' },
                        { name: 'Site Type', group: 'keywords', type:'basic', autocompleteUrl: '/api/lookup/keyword', autocompleteSuffix: 'keywordType=SiteTypeKeyword', fieldName: 'term', resultSuffix: 'items' },
                        { name: 'Geographic Keywords', group: 'keywords', type:'basic', autocompleteUrl: '/api/lookup/keyword', autocompleteSuffix: 'keywordType=GeographicKeyword', fieldName: 'term', resultSuffix: 'items' },
                        { name: 'Culture Keywords', group: 'keywords', type:'basic', autocompleteUrl: '/api/lookup/keyword', autocompleteSuffix: 'keywordType=CultureKeyword', fieldName: 'term', resultSuffix: 'items' },
                        { name: 'Material Keywords', group: 'keywords', type:'basic', autocompleteUrl: '/api/lookup/keyword', autocompleteSuffix: 'keywordType=MaterialKeyword', fieldName: 'term', resultSuffix: 'items' },
                        { name: 'Temporal Keywords', group: 'keywords', type:'basic', autocompleteUrl: '/api/lookup/keyword', autocompleteSuffix: 'keywordType=TemporalKeyword', fieldName: 'term', resultSuffix: 'items' },
                        { name: 'Other Keywords', group: 'keywords', type:'basic', autocompleteUrl: '/api/lookup/keyword', autocompleteSuffix: 'keywordType=OtherKeyword', fieldName: 'term', resultSuffix: 'items' },
                        { name: 'Investigation Type', group: 'keywords', type:'basic', autocompleteUrl: '/api/lookup/keyword', autocompleteSuffix: 'keywordType=InvestigationType', fieldName: 'term', resultSuffix: 'items' },
	            	],
	            	rows: [{option:'',value:''}]
	            },
	            computed: {},
	            methods: {
		            addRow: function() {
		            	this.rows.push({option:'',value:''});
		            },
		            removeRow: function(idx) {
		            	this.rows.splice(idx, 1);

		            },
	            },
            });
});
            
</script>
            </#if>
        </li>
    </ul>

</nav>

<style>

.autocomplete {
  position: relative;
  width: 130px;
}

.autocomplete-results {
  padding: 0;
  margin: 0;
  border: 1px solid #eeeeee;
  line-height:initial;
  weight:normal;
  font-weight:normal;
  overflow: auto;
  position:  absolute;
  background: white;
  //height: 120px;
//  width: 100%;
}

.autocomplete-result {
  list-style: none;
  text-align: left;
  padding: 4px 2px;
  cursor: pointer;
  line-height: initial;
  display: inline-block;
  width: intrinsic;
}

.autocomplete-result.is-active,
.autocomplete-result:hover {
  background-color: #4aae9b;
  color: white;
}

</style>

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
