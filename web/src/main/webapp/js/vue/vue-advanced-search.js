TDAR.vuejs.advancedSearch = (function(console, ctx, Vue, axios, TDAR) {
    "use strict";
    var UNDEFINED = "undefined";
    var MAXLEN_CHECKBOXLIST = 50;

    /**
     * "Part" Vue Control.
     */
    Vue.component('part', {
        template : "#search-row-template",
        props : [ "row", "index", "options", "totalrows" ],
        data : function() {
            return {
                // option : '',
                // value : []
            }
        },
        watch : {
        },
        mounted : function() {
        },
        computed : {
            valueFieldName : function() {
                if (typeof this.row.option === UNDEFINED || typeof this.row.option.fieldName === UNDEFINED) {
                    return undefined;
                }
                var ret = "groups[0]." + this.row.option.fieldName.replace("[]", "[" + this.index + "]");
                if (typeof this.row.option.columnType !== UNDEFINED) {
                    return ret + ".value";
                }
                return ret;

            },
            fieldName : function() {
                if (typeof this.row.option === UNDEFINED || typeof this.row.option.fieldName === UNDEFINED) {
                    return undefined;
                }
                return "groups[0]." + this.row.option.fieldName.replace("[]", "[" + this.index + "]");
            },
            searchFieldName : function() {
                return "searchFieldName";
            },
            idName : function() {
                if (typeof this.row.option === UNDEFINED || typeof this.row.option.idName === UNDEFINED) {
                    return undefined;
                }
                return "groups[0]." + this.row.option.idName.replace("[]", "[" + this.index + "]");
            },
            infoLink: function() {
                if (typeof this.row.option === UNDEFINED || typeof this.row.option.infoLink === UNDEFINED) {
                    return undefined;
                }
                return this.row.option.infoLink;
            }
        },
        methods : {
            reset : function() {
                if (typeof this.$refs.autocomplete !== UNDEFINED) {
                    this.$refs.autocomplete.clear();
                }
            },
            getOptionsFor : function(group) {
                var ret = [];
                this.options.forEach(function(e) {
                    if (group == e.group) {
                        ret.push(e);
                    }
                });
                return ret;
            },
            getOptionGroups : function() {
                var ret = {};
                this.options.forEach(function(e) {
                    ret[e.group] = 1;
                });
                return Object.keys(ret);
            },
            clearRow : function() {
                if (this.index == 0 && this.totalrows == 1) {
                    this.reset();
                } else {
                    this.$emit("removerow", this.index);
                }
            },
            optionChanged: function (event) {
                console.log("searchFieldChanged:", event);
            }


        }

    });

    /**
     * Toplevel app for the search.
     */
    var app = new Vue({
        el : "#advancedsearch",
        props: {
            "mapped-dataset-id": Number

        },
        watch: {
        },

        data : function() { return {
            termOperator: 'AND',
            columnMap : {},
            selectOptions : [
                {
                    name : 'All Fields',
                    group : 'general',
                    type : 'basic',
                    fieldName : "allFields[]",
                    index : [ 'resource', 'collection', "integration" ]
                }, {
                    name : 'Title',
                    group : 'general',
                    type : 'basic',
                    fieldName : "titles[]",
                    index : [ 'resource', 'collection', "integration" ]
                }, {
                    name : 'Description',
                    group : 'general',
                    type : 'basic',
                    fieldName : "descriptions[]",
                    index : [ 'resource', 'collection', 'integration' ]
                }, {
                    name : 'Full-Text',
                    group : 'general',
                    type : 'basic',
                    fieldName : "contents[]",
                    index : [ 'resource' ]
                }, {
                    name : 'Date',
                    group : 'general',
                    type : 'integer',
                    fieldName : "createdDates[]",
                    index : [ 'resource', 'collection' ]
                }, {
                    name : 'Id',
                    group : 'general',
                    type : 'integer',
                    fieldName : "ids",
                    index : [ 'resource', 'collection', "integration", "person", "institution" ]
                }, {
                    name : 'Date Added',
                    group : 'general',
                    type : 'date',
                    fieldName : "registeredDates[]",
                    index : [ 'resource', 'collection', "integration", "person", "institution" ]
                }, {
                    name : 'Date Updated',
                    group : 'general',
                    type : 'date',
                    fieldName : "updatedDates[]",
                    index : [ 'resource', 'collection', "integration", "person", "institution" ]
                }, {
                    name : 'Map',
                    group : 'general',
                    type : 'map',
                    fieldName : "latitudeLongitudeBoxes[]",
                    index : [ 'resource' ]
                }, {
                    name : 'Project',
                    group : 'general',
                    type : 'basic',
                    autocompleteUrl : '/api/lookup/resource',
                    autocompleteSuffix : 'resourceTypes[0]=PROJECT',
                    searchFieldName : 'term',
                    resultSuffix : 'resources',
                    fieldName : "projects[].title",
                    idName : "projects[].id",
                    index : [ 'resource' ]
                }, {
                    name : 'Collection',
                    group : 'general',
                    type : 'basic',
                    autocompleteUrl : '/api/lookup/collection',
                    searchFieldName : 'term',
                    resultSuffix : 'collections',
                    fieldName : "collections[].name",
                    idName : "collections[].id",
                    index : [ 'resource' ]
                }, {
                    name : 'Person',
                    group : 'general',
                    type : 'basic',
                    autocompleteUrl : '/api/lookup/person',
                    searchFieldName : 'term',
                    resultSuffix : 'people',
                    fieldName : "resourceCreatorProxies[].person.name",
                    idName : "resourceCreatorProxies[].person.id",
                    index : [ 'resource' ]

                }, {
                    name : 'Institution',
                    group : 'general',
                    type : 'basic',
                    autocompleteUrl : '/api/lookup/institution',
                    searchFieldName : 'name',
                    resultSuffix : 'institutions',
                    fieldName : "resourceCreatorProxies[].institution.name",
                    idName :    "resourceCreatorProxies[].institution.id",
                    index : [ 'resource' ]
                }, {
                    name : 'Site Name',
                    group : 'keywords',
                    type : 'basic',
                    autocompleteUrl : '/api/lookup/keyword',
                    autocompleteSuffix : 'keywordType=SiteNameKeyword',
                    searchFieldName : 'term',
                    resultSuffix : 'items'
                }, {
                    name : 'Site Type',
                    group : 'keywords',
                    type : 'basic',
                    autocompleteUrl : '/api/lookup/keyword',
                    autocompleteSuffix : 'keywordType=SiteTypeKeyword',
                    searchFieldName : 'term',
                    resultSuffix : 'items'
                }, {
                    name : 'Geographic Keywords',
                    group : 'keywords',
                    type : 'basic',
                    autocompleteUrl : '/api/lookup/keyword',
                    autocompleteSuffix : 'keywordType=GeographicKeyword',
                    searchFieldName : 'term',
                    resultSuffix : 'items'
                }, {
                    name : 'Culture Keywords',
                    group : 'keywords',
                    type : 'basic',
                    autocompleteUrl : '/api/lookup/keyword',
                    autocompleteSuffix : 'keywordType=CultureKeyword',
                    searchFieldName : 'term',
                    resultSuffix : 'items'
                }, {
                    name : 'Material Keywords',
                    group : 'keywords',
                    type : 'basic',
                    autocompleteUrl : '/api/lookup/keyword',
                    autocompleteSuffix : 'keywordType=MaterialKeyword',
                    searchFieldName : 'term',
                    resultSuffix : 'items'
                }, {
                    name : 'Temporal Keywords',
                    group : 'keywords',
                    type : 'basic',
                    autocompleteUrl : '/api/lookup/keyword',
                    autocompleteSuffix : 'keywordType=TemporalKeyword',
                    searchFieldName : 'term',
                    resultSuffix : 'items'
                }, {
                    name : 'Other Keywords',
                    group : 'keywords',
                    type : 'basic',
                    autocompleteUrl : '/api/lookup/keyword',
                    autocompleteSuffix : 'keywordType=OtherKeyword',
                    searchFieldName : 'term',
                    resultSuffix : 'items'
                }, {
                    name : 'Investigation Types',
                    group : 'keywords',
                    type : 'checkbox',
                    choices : []
                }, ],
            rows : [ {
                option : '',
                value : []
            } ],
            jsondata: ''
        }},
        mounted : function() {
            var self = this;

            // Look for search info in document data first,
            var documentData  = TDAR.loadDocumentData();
            if(!!documentData['searchInfo']){
                self.processSearchInfo(documentData['searchinfo']);
            } else {
                axios({
                    method : 'get',
                    url : '/api/search/info',
                }).then(function(response) {
                    console.log("received search info via ajax");
                    self.processSearchInfo(response.data)
                });

            }

            // filter the available fields if groups property specified
            // FIXME: temp hack: forcing groups to "custom"
            this.groups = "custom"
            this.allSelectOptions = this.selectOptions.slice();
            var groups = this.groups.split(this.groups, ",");
            if(this.groups !== "all") {
                this.selectOptions = this.selectOptions.filter(function(opt){
                    return groups.indexOf(opt.group) > -1;
                });
            }
        },
        computed : {},
        methods : {

            processSearchInfo: function(data) {
                var self = this;
                console.log(data);
                Vue.set(self, "columnMap", data['datasetReferences']);
                self.selectOptions.forEach(function(opt) {
                    if (opt.name === 'Investigation Types') {
                        Vue.set(opt, "choices", data.investigationTypes);
                    }
                });
                // FIXME: datasetId should come from component property instead of <body> element
                var dsid = document.body.getAttribute("data-mapped-dataset-id");
                if (typeof dsid !== UNDEFINED) {
                    dsid = parseInt(dsid);
                    console.log("add mapped...", dsid);
                    self.addColumnInfo(dsid);
                }
            },

            processRefineSearch: function(data) {
            },


            addRow : function() {
                this.rows.push({
                    option : '',
                    value : ''
                });
            },
            submit : function() {
                console.log(this.$refs.form);
                this.$refs.form.submit();
            },
            removeRow : function(idx) {
                this.rows.splice(idx, 1);
            },
            addColumnInfo : function(datasetId) {
                var self = this;
                var dataset = this.columnMap.filter(function(ds){return ds.id === datasetId;})[0];

                if (!dataset) {
                    console.warn("addColumnInfo:: datasetId not found:" + datasetId);
                    return;
                }

                console.log("adding column info for:", datasetId, dataset);

                // sort column names in place
                dataset.columns.sort(function(a, b) {
                    var sortprop = "displayName";
                    if(a[sortprop] > b[sortprop]) {return 1;}
                    if(a[sortprop] < b[sortprop]) {return -1;}
                    return 0;
                });

                dataset.columns.forEach(function(field) {
                    var values = [];
                    //var type = "basic";
                    var type = "select";
                    if (field.intValues.length > 0) {
                        values = field.intValues;
                    } else if (field.floatValues.length > 0) {
                        values = field.floatValues;
                    } else if (field.values.length > 0) {

                        // FIXME: magic numbers
                        // Perform case-insensitive dedupe if list isn't too big (
                        console.log("field:%s\t values:%s", field.name, field.values.length);
                        if(field.values.length > 100) {
                            values = field.values;
                        } else {
                            values = [];
                            var lcvalues = [];
                            field.values.forEach(function(val, idx){
                                var lcval = val.toLowerCase();
                                if(lcvalues.indexOf(lcval) === -1) {
                                    lcvalues.push(lcval);
                                    values.push(val);
                                }
                            });
                        }
                        values.sort(function(a,b){
                            var ret = 0;
                            if(a.toLowerCase() < b.toLowerCase()) {
                                ret = -1;
                            } else if(a.toLowerCase() > b.toLowerCase()) {
                                ret = 1;
                            }
                            return ret;
                        });

                        // For smaller range of values, render as checkbox.
                        if (values.length < MAXLEN_CHECKBOXLIST) {
                            type = "checkbox";
                        }
                    }
                    self.selectOptions.push({
                        name : field.displayName,
                        fieldName : "dataValues[]",
                        type : type,
                        group : 'custom',
                        id : field.id,
                        // FIXME: I have no idea why fieldValues exists apart from "choices".
                        fieldValues : values,
                        infoLink : '/dataset/column/' + datasetId + "/" + field.id,
                        choices : values,
                        columnType : field.columnDataType
                    })
                });
            },

            /**
             * Serialize the current state of the form as an array of name-value pairs (excluding blank and/or invalid
             * rows.
             *
             * FIXME:  Currently we only serialize dataValues fields (aka custom data fields)
             */
            serializeState: function() {
                console.log('serialize state called');
                var formdata = (this.$refs.parts
                // only include valid, non-blank form rows
                    .filter(function(part, i){
                        var row = part.row;
                        return (
                            !!row.option.fieldName && (
                            (typeof row.value === 'object' && row.value.length > 0) ||
                            (typeof row.value === 'string' && row.value.trim().length > 0)))
                    })
                    // transform into array of name/value pairs.
                    .map(function(part, i){
                        return {f: part.row.option.name, v:part.row.value}
                    }));
                this.jsondata = JSON.stringify(formdata);
            },


            /**
             * Remove form rows and bring the app to an initial state similar to if had just been mounted
             */
            clearState: function() {
                this.rows.splice(0, this.rows.length);
            },


            /**
             * Parse the serialized search  and then rebuild the form rows + values
             */
            deserializeState: function() {
                var self = this;
                console.log("native splice: %s", Array.prototype.splice);
                console.log("   Vue splice: %s", this.rows.splice.toString() );
                this.clearState();
                // Clear current form state
                var data =[];
                if(!!this.jsondata) {
                    data = JSON.parse(this.jsondata);
                }

                data.map(function(v, i){
                    return {option: '', value: ''}
                }).forEach(function(v, i) {
                    self.rows.push(v);
                })
            },

            getSelectOptionByName(name) {
                var ret;
                for(var i= 0; i < this.selectOptions.length; i++) {
                    var opt = this.selectOptions[i];
                    if(opt.name === name) {
                        ret = opt;
                        break;
                    }
                }

                return ret;
            },

            setCheckboxRow: function(){
                console.log('method called: setCheckboxRow');
                var row = this.rows[0];
                row.value.push('Indeterminate');
                row.option = this.getSelectOptionByName("condition");

            },

            setSelectRow: function(){
                console.log('method called: setSelectRow')
            }


        }
    });
})(console, window, Vue, axios, TDAR);
