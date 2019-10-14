<#escape _untrusted as _untrusted?html>


<#macro datamappedSearchForm  collectionId>

    <!-- FIXME: feels weird to #include other templates and then define two more templates inline -->
    <#include "/components/tdar-values/template/values.html" />
    <#include "/components/vue-checkbox-list/templates/checkboxlist.html" />
    <#include "/components/vue-checkbox-list/templates/selectlist.html" />

    <!-- Template for top-level app -->
    <div class=""  id="advancedsearch">
        <form onSubmit="return false" action="/search/results" ref="form" class="seleniumIgnoreForm" id="frmDatasetSearch">
            <input type="hidden" name="dataMappedCollectionId" value="${collectionId?c}">

            <div class=" advancedSearchbox" >

                <div class="searchgroup">
                    <div id="groupTable0" class="condensed">
                        <fieldset class="condensed" v-for="(row,index) in rows">
                            <part
                                    :index="index"
                                    :row="row"
                                    :optionsmap="optionsByName"
                                    :columns="selectOptions"
                                    @removerow="removeRow($event)"
                                    ref="parts"
                            />
                        </fieldset>
                    </div>

                    <div class="form-row">
                        <div class="col-auto">
                            <button class="btn btn-primary" id="groupTable0AddAnotherButton" type="button" @click="addRow()">
                                + add another search term
                            </button>
                        </div>
                    </div>
                    <div class="form-row" v-if="rows.length > 1">
                        <label class="control-label">Include in results</label>
                        <div class="controls controls-row condensed">
                            <select name="groups[0].operator" class="">
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

            <!-- DEBUG STUFF -->
            <div class="row" v-if="debugMode">
                <div class="">
                    <h2>debug stuff</h2>
                </div>
            </div>
            <div class="row" v-if="debugMode">
                <div class="">
                    <div class="btn-group">
                        <button type="button" id="btnSerialize"   class="btn btn-sm btn-secondary" @click="serializeState" >Serialize Form</button>
                        <button type="button" id="btnDeserialize" class="btn btn-sm btn-secondary" @click="deserializeState" >Load Form</button>
                        <button type="button" id="btnSetCheckboxRow" class="btn btn-sm btn-secondary" @click="setCheckboxRow" >setCheckboxRow</button>
                        <button type="button" id="btnSetSelectRow" class="btn btn-sm btn-secondary" @click="setSelectRow" >setSelectRow</button>

                    </div>
                </div>
            </div>
            <!-- END: DEBUG STUFF -->


        </form>
    </div>


    <!-- Template for 'Part' component -->
    <script type="text/x-template" id="dataset-search-row-template">
        <div class="">
            <!-- fixme: consider binding to row.option instead of option? -->
            <div class="form-group">
                <label>Search Field</label>
                <select v-model="row.option"  class="form-control" ref='fieldselect' :id="'selField' + index" >
                    <option v-for="(opt, index) in columns" v-bind:value="opt" :selected="row.option.id == opt.id"> {{debugMode ? (opt.type.substr(0,3) +   ' ' + opt.name) : opt.name }} </option>
                </select>
            </div>

            <div class="form-group" v-if="!!row.option.type">

                <div class="" ref='valuearea' v-if="!!row.option.type">
                    <div class="">

                        <div v-if="row.option.type == '_select'"  class="">
                            <select :name="valueFieldName" class="form-control" multiple>
                                <option  v-for="(opt, i) in optionsmap[row.option.name]">{{opt}}</option>
                            </select>
                        </div>

                        <div v-if="row.option.type == 'select'"  class="">
                            <selectlist
                                    :name="valueFieldName"
                                    :options="optionsmap[row.option.name]"
                                    v-model="row.value"
                                    :size="10"
                            />
                        </div>

                        <div v-if="row.option.type == 'checkbox'">
                            <checkboxlist
                                    :name="valueFieldName"
                                    :choices="optionsmap[row.option.name]"
                                    v-model="row.value"
                            />
                        </div>
                        <span v-if="!!row.option.columnType" >
                            <input type="hidden" :name="fieldName + '.columnId'" :value="row.option.id" />
                            <input type="hidden" :name="fieldName + '.singleToken'" :value="false" />
                        </span>
                        <div class="">
                            <button class="btn  btn-sm " @click="clearRow()" type="button" tabindex="-1"><i class="fas fa-trash-alt "></i></button>
                        </div>

                    </div>
                </div>

            </div>
        </div>
    </script>

<textarea id="formstate" cols="1" rows="1" style="visibility:hidden"></textarea>

</#macro>


<#macro datamappedSearchLink path msg='Refine Search'>
    <a
            href="<@s.url escapeAmp="false" includeParams="all" value="${path}"><#if path?? && path!="results"><@s.param name="id" value=""/><@s.param name="keywordType" value=""/><@s.param name="slug" value=""/></#if><#nested></@s.url>"
            >${msg}</a>

</#macro>

</#escape>