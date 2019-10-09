<#escape _untrusted as _untrusted?html>


<#macro datamappedSearchForm  collectionId>


    <#include "/components/tdar-autocomplete/template/autocomplete.html" />
    <#include "/components/tdar-values/template/values.html" />

    <#include "/components/vue-checkbox-list/templates/checkboxlist.html" />
    <#include "/components/vue-checkbox-list/templates/selectlist.html" />

    <!-- Template for top-level app -->
    <div class="advanced container" style="z-index: 10000; border-top:1px solid #DDD; border-bottom:1px solid #DDD;" id="advancedsearch" group="custom">
        <form onSubmit="return false" action="/search/results" ref="form" class="seleniumIgnoreForm">
            <input type="hidden" name="dataMappedCollectionId" value="${collectionId?c}">
            <div class="row">
                <div class="col-12">
                    <h2>form debug</h2>
                    <button type="button" id="btnSerialize"   class="btn btn-sm btn-secondary" @click="serializeState" >Serialize Form</button>
                    <button type="button" id="btnDeserialize" class="btn btn-sm btn-secondary" @click="deserializeState" >Load Form</button>
                    <button type="button" id="btnSetCheckboxRow" class="btn btn-sm btn-secondary" @click="setCheckboxRow" >setCheckboxRow</button>
                    <button type="button" id="btnSetSelectRow" class="btn btn-sm btn-secondary" @click="setSelectRow" >setSelectRow</button>
                    <hr>
                </div>
            </div>
            <div class="row">
                <div class="col-12">
                    <textarea v-bind:value="jsondata">{{jsondata}}</textarea>
                </div>

            </div>


            <div class="row">
                <div class=" advancedSearchbox col-12" >
                    <!--
                    <div class="control-group condensed">
                        <label class="control-label">What:</label>

                        <div class="controls controls-row">
                            <label class="radio inline"> <input type="radio" name="optionsRadios" id="optionsRadios1" value="RESOURCE">
                                Resources
                            </label> <label class="radio inline"> <input type="radio" name="optionsRadios" id="optionsRadios1" value="COLLECTION">
                                Collections
                            </label> <label class="radio inline"> <input type="radio" name="optionsRadios" id="optionsRadios1" value="PEOPLE">
                                People
                            </label> <label class="radio inline"> <input type="radio" name="optionsRadios" id="optionsRadios1" value="INSTITUTIONS">
                                Institutions
                            </label>
                        </div>
                    </div>
                    -->
                    <div class="searchgroup">
                        <div id="groupTable0" class="condensed">
                            <div class="condensed" v-for="(row,index) in rows">
                                <part
                                        :index="index"
                                        :row="row"
                                        :options="selectOptions"
                                        @removerow="removeRow($event)"
                                        :totalrows="rows.length"
                                        ref="parts"
                                />
                            </div>
                        </div>

                        <div class="controls-row condensed">
                            <div class="controls">
                                <button class="btn" id="groupTable0AddAnotherButton" type="button" @click="addRow()">
                                    <i class="icon-plus-sign"></i>add another search term
                                </button>
                            </div>
                        </div>
                        <div class=" control-group " v-if="rows.length > 1">
                            <label class="control-label">Include in results</label>
                            <div class="controls controls-row condensed">
                                <select name="groups[0].operator" class="col-3">
                                    <option value="AND" selected="">When resource matches ALL terms below</option>
                                    <option value="OR">When resource matches ANY terms below</option>
                                </select>
                            </div>
                        </div>

                        <p class="text-center">
                            <button type="button" class="button btn tdar-button center" @click="submit()">Search</button>
                        </p>
                    </div>
                </div>
            </div>
        </form>
    </div>


    <!-- Template for 'Part' component -->
    <script type="text/x-template" id="search-row-template">
        <div class="row pb-2">
            <!-- fixme: consider binding to row.option instead of option? -->
            <selectlist name="columnId" :options="getOptionsFor('custom')" labelKey="name" valueKey="id" v-model="row.option" blankrow  />

            <select v-model="row.option"  class="col-2 col-form-label form-control" ref='fieldselect' @change="optionChanged" >
                <option v-for="(opt, index) in getOptionsFor('custom')" v-bind:value="opt" :selected="row.option.id == opt.id"> {{ opt.name }}  </option>
            </select>
            <div class="col-10" ref='valuearea'>
                <div class="row">
                    <div v-if="row.option.type == 'basic' || row.option.type == undefined" class="col-11">
                        <autocomplete
                                :url="row.option.autocompleteUrl"
                                :suffix="row.option.autocompleteSuffix"
                                :field="(!!row.option.columnType) ?  valueFieldName : fieldName"
                                v-if="row.option.autocompleteUrl != undefined || row.option.choices != undefined"
                                :bootstrap4="true"
                                :items="row.option.choices"
                                :resultsuffix="row.option.resultSuffix"
                                ref="autocomplete"
                                :span="'form-control'"
                                :queryParameterName="row.option.searchFieldName"
                                :allowCreate="false"
                                :idname="idName"
                                v-model="row.value"
                        />
                        <input type="text" :name="valueFieldName" class="form-control" v-if="row.option.autocompleteUrl == undefined && row.option.choices == undefined">
                    </div>
                    <div v-if="row.option.type == 'select'"  class="col-11">
                        <select :name="valueFieldName" class="form-control" multiple>
                            <option  v-for="(opt, i) in row.option.choices">{{opt}}</option>
                        </select>
                    </div>
                    <div v-if="row.option.type == 'integer'"  class="col-11">
                        <input type="number" :name="valueFieldName" class="form-control col-3">
                    </div>
                    <div v-if="row.option.type == 'date'"  class="col-11">
                        <div class="form-row">
                            <input type="date" :name="fieldName + '.start'" class="form-control col-4">
                            <input type="date" :name="fieldName + '.end'" class="form-control col-4 ml-2">
                        </div>
                    </div>
                    <div v-if="row.option.type == 'checkbox'" class="col-11">
                        <value :choices="row.option.choices" ref="valuepart"  v-model="row.value" :idOnly="true" :numcols="2" :fieldName="valueFieldName"/>
                        <input type="hidden" :name="fieldName + '.singleToken'" :value="false" />
                    </div>
                    <div v-if="!!row.option.columnType"  class="col-11">
                        <input type="hidden" :name="fieldName + '.columnId'" :value="row.option.id" />
                        <input type="hidden" :name="fieldName + '.singleToken'" :value="false" />
                    </div>
                    <div id="latlongoptions" v-if="row.option.type == 'map'"  class="col-11 leaflet-map-editable"  data-search="true" style="height:300px">
               <span class="latlong-fields">
                    <input type="hidden" :name="fieldName + '.east'" id="maxx" class="ne-lng latLongInput maxx" />
                    <input type="hidden" :name="fieldName + '.south'"  id="miny" class="sw-lat latLongInput miny" />
                    <input type="hidden" :name="fieldName + '.west'" id="minx" class="sw-lng latLongInput minx" />
                    <input type="hidden" :name="fieldName + '.north'"  id="maxy" class="ne-lat latLongInput maxy" />
               </span>
                        <div class="mapdiv"></div>
                    </div>
                    <div class="col-1">
                        <span v-if="infoLink != undefined"><a :href="infoLink" class="btn  btn-sm " tabindex="-1" target="_blank"><i class="fas fa-info-circle "></i></a></span>
                        <button class="btn  btn-sm " @click="clearRow()" type="button" tabindex="-1"><i class="fas fa-trash-alt "></i></button>
                    </div>
                </div>
            </div>
        </div>
    </script>
</#macro>


<#macro datamappedSearchLink path msg='Refine Search'>
    <a
            href="<@s.url escapeAmp="false" includeParams="all" value="${path}"><#if path?? && path!="results"><@s.param name="id" value=""/><@s.param name="keywordType" value=""/><@s.param name="slug" value=""/></#if><#nested></@s.url>"
            >${msg}</a>

</#macro>

</#escape>