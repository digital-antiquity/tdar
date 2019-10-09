<#escape _untrusted as _untrusted?html>


<#macro datamappedSearchForm  collectionId>

    <!-- FIXME: feels weird to #include other templates and then define two more templates inline -->
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
    <script type="text/x-template" id="dataset-search-row-template">
        <div class="row pb-2">
            <!-- fixme: consider binding to row.option instead of option? -->

            <select v-model="row.option"  class="col-2 col-form-label form-control" ref='fieldselect' >
                <option v-for="(opt, index) in getOptionsFor('custom')" v-bind:value="opt" :selected="row.option.id == opt.id">{{opt.type.substr(0,3)}}  |  {{ opt.name }} </option>
            </select>
            <div class="col-10" ref='valuearea' v-if="!!row.option.type">
                <div class="row">
                    <div v-if="row.option.type == 'select'"  class="col-11">
                        <select :name="valueFieldName" class="form-control" multiple>
                            <option  v-for="(opt, i) in row.option.choices">{{opt}}</option>
                        </select>
                    </div>
                    <div v-if="row.option.type == 'checkbox'" class="col-11">
                        <value :choices="row.option.choices" ref="valuepart"  v-model="row.value" :idOnly="true" :numcols="2" :fieldName="valueFieldName"/>
                        <input type="hidden" :name="fieldName + '.singleToken'" :value="false" />
                    </div>
                    <span v-if="!!row.option.columnType" >
                        <input type="hidden" :name="fieldName + '.columnId'" :value="row.option.id" />
                        <input type="hidden" :name="fieldName + '.singleToken'" :value="false" />
                    </span>
                    <div class="col-1">
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