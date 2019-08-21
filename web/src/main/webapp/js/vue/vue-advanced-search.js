TDAR.vuejs.advancedSearch = (function(console, ctx, Vue, axios, TDAR) {
    "use strict";
    var UNDEFINED = "undefined";

    /**
     * "Part" Vue Control.
     */
    Vue.component('part', {
        template : "#search-row-template",
        props : [ "row", "index", "options", "totalrows" ],
        data : function() {
            return {
                option : '',
                value : ''
            }
        },
        watch : {
            option : function(n, o) {
                this.reset();
                this.option = n;
                if (this.option.type === 'map') {
                    Vue.nextTick(function() {
                        TDAR.leaflet.initEditableLeafletMaps();
                    });
                }
            },
            value: function(n, o) {
                if (this.option.name === 'Collection') {
                    this.$emit("collection change,", n);
                }
            }
        },
        mounted : function() {
        },
        computed : {
            valueFieldName : function() {
                if (typeof this.option === UNDEFINED || typeof this.option.fieldName === UNDEFINED) {
                    return undefined;
                }
                var ret = "groups[0]." + this.option.fieldName.replace("[]", "[" + this.index + "]");
                if (typeof this.option.columnType !== UNDEFINED) {
                    return ret + ".value";
                }
                return ret;

            },
            fieldName : function() {
                if (typeof this.option === UNDEFINED || typeof this.option.fieldName === UNDEFINED) {
                    return undefined;
                }
                return "groups[0]." + this.option.fieldName.replace("[]", "[" + this.index + "]");
            },
            searchFieldName : function() {
                return "searchFieldName";
            },
            idName : function() {
                if (typeof this.option === UNDEFINED || typeof this.option.idName === UNDEFINED) {
                    return undefined;
                }
                return "groups[0]." + this.option.idName.replace("[]", "[" + this.index + "]");
            },
            infoLink: function() {
                if (typeof this.option === UNDEFINED || typeof this.option.infoLink === UNDEFINED) {
                    return undefined;
                }
                return this.option.infoLink;
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
        data : {
            columnMap : {},
            selectOptions : [ {
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
                value : ''
            } ]
        },
        mounted : function() {
            var self = this;
            axios({
                method : 'get',
                url : '/api/search/info',
            }).then(function(response) {
                console.log(response.data);
                Vue.set(self, "columnMap", response.data['datasetReferences']);
                self.selectOptions.forEach(function(opt) {
                    if (opt.name === 'Investigation Types') {
                        Vue.set(opt, "choices", response.data.investigationTypes);
                    }
                });
                // FIXME: datasetId should come from component property instead of <body> element
                var dsid = document.body.getAttribute("data-mapped-dataset-id");
                if (typeof dsid !== UNDEFINED) {
                    dsid = parseInt(dsid);
                    console.log("add mapped...", dsid);
                    self.addColumnInfo(dsid);
                }
            });
        },
        computed : {},
        methods : {
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
                   if(a.name > b.name) {return 1;}
                   if(a.name < b.name) {return -1;}
                   return 0;
                });

                dataset.columns.forEach(function(field) {
                    var values = [];
                    var type = "basic";
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

                        //FIXME: magic numbers
                        // For smaller range of values, render as checkbox.
                        if (values.length < 20) {
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

            }
        }
    });
})(console, window, Vue, axios, TDAR);