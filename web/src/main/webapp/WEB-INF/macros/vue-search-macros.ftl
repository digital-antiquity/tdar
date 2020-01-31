<#escape _untrusted as _untrusted?html>


<#macro datamappedSearchForm  collectionId>

    <!-- FIXME: feels weird to #include other templates and then define two more templates inline -->
    <#include "/components/tdar-values/template/values.html" />
    <#include "/components/vue-checkbox-list/templates/checkboxlist.html" />
    <#include "/components/vue-checkbox-list/templates/selectlist.html" />

    <!-- Template for top-level app -->
    <div class="row"  id="advancedsearch">
        <h3>Search Within This Collection</h3>
        <form onSubmit="return false" action="/search/results" ref="form" class="seleniumIgnoreForm col-12" id="frmDatasetSearch">
            <input type="hidden" name="dataMappedCollectionId" value="${collectionId?c}">


            <div class="form-row" >
                <div class="searchgroup col-12">
                    <div id="groupTable0" class="form-row">
                        <fieldset class="search-term col-12" v-for="(row,index) in rows">
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
                        <div class="col-auto offset-1">
                            <button class="btn btn-secondary btn-sm" id="groupTable0AddAnotherButton" type="button" @click="addRow()">
                                <i class="fas fa-plus-circle"> </i> add another search term
                            </button>


                        </div>
                        <div class="col-auto form-inline" v-if="rows.length > 1">
                            <label class="m-2">Match Behavior</label>
                            <select name="groups[0].operator" class="col-auto form-control form-control-sm" v-model="operator">
                                <option value="AND" selected="">AND - Match all search terms</option>
                                <option value="OR">OR - Match any search term</option>
                            </select>
                        </div>
                    </div>

                </div>
            </div>

            <div class="row mt-2">
                    <div class="col-12">
                        <p class="text-center">
                        <button type="button" class="button btn btn-primary btn-lg" @click="submit()">Search</button>
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
        <div class="form-row ">
            <!-- fixme: consider binding to row.option instead of option? -->
            <div class="col-11">
                <div class="form-row">
                    <div class="col-3 form-group">
                        <label>Search Field</label>
                        <select v-model="row.option"  class="form-control form-control-sm" ref='fieldselect' :id="'selField' + index" >
                            <option v-for="(opt, index) in columns" v-bind:value="opt" :selected="row.option.id == opt.id"> {{debugMode ? (opt.type.substr(0,3) +   ' ' + opt.name) : opt.name }} </option>
                        </select>
                    </div>

                    <div class="col-9 form-group" ref='valuearea' v-if="!!row.option.type">
                        <label>Selected Values</label>
                        <div class="">

                            <div v-if="row.option.type == 'select'"  class="">
                                <selectlist
                                        :name="valueFieldName"
                                        :options="optionsmap[row.option.name]"
                                        v-model="row.value"
                                        :size="10"
                                        :statusbar="true"
                                />
                            </div>

                            <div v-if="row.option.type == 'checkbox'">
                                <checkboxlist
                                        :name="valueFieldName"
                                        :choices="optionsmap[row.option.name]"
                                        v-model="row.value"
                                />
                            </div>

                            <div v-if="row.option.type == 'basic'">
                                <div class="form-group">
                                    <input type="search" :name="valueFieldName" v-model="row.value" />
                                </div>
                            </div>


                            <span v-if="!!row.option.columnType" >
                                <input type="hidden" :name="fieldName + '.columnId'" :value="row.option.id" />
                                <input type="hidden" :name="fieldName + '.singleToken'" :value="false" />
                            </span>
                        </div>
                    </div>
                    <div v-else class="col-auto form-group">
                        <label>Selected Values</label>
                        <div class="">
                            <em>Choose a form field on the left</em>
                        </div>

                    </div>


                </div>



            </div>


            <div class="col-1">
                <button class="btn  btn-sm btn-secondary" @click="clearRow()" type="button" tabindex="-1"><i class="fas fa-trash-alt "></i></button>
            </div>
        </div>
    </script>

<textarea id="formstate" cols="1" rows="1" style="visibility:hidden"></textarea>

</#macro>


<#macro datamappedSearchLink path msg='Refine Search'>
    <a
            href="<@s.url escapeAmp="false" includeParams="all" value="${path}"><#if path?? && path!="results"><@s.param name="id" value=""/><@s.param name="keywordType" value=""/><@s.param name="slug" value=""/></#if><#nested></@s.url>"
            ><#noescape>${msg}</#noescape></a>

</#macro>

</#escape>