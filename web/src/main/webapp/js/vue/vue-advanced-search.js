TDAR.vuejs.advancedSearch = (function(console, ctx, Vue, axios, TDAR) {
    "use strict";

    var _part = Vue.component('part', {
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
                if (this.option.type == 'map') {
                    Vue.nextTick(function() {
                        TDAR.leaflet.initEditableLeafletMaps();
                    });
                }
            },
            value: function(n, o) {
                if (this.option.name == 'Collection') {
                    this.$emit("collection change,", n);
                }
            }
        },
        mounted : function() {
        },
        computed : {
            valueFieldName : function() {
                if (this.option == undefined || this.option.fieldName == undefined) {
                    return undefined;
                }
                var ret = "groups[0]." + this.option.fieldName.replace("[]", "[" + this.index + "]");
                if (this.option.columnType != undefined) {
                    return ret + ".value";
                }
                return ret;

            },
            fieldName : function() {
                if (this.option == undefined || this.option.fieldName == undefined) {
                    return undefined;
                }
                return "groups[0]." + this.option.fieldName.replace("[]", "[" + this.index + "]");
            },
            searchFieldName : function() {
                return "searchFieldName";
            },
            idName : function() {
                if (this.option == undefined || this.option.idName == undefined) {
                    return undefined;
                }
                return "groups[0]." + this.option.idName.replace("[]", "[" + this.index + "]");
            },
            infoLink: function() {
                if (this.option == undefined || this.option.infoLink == undefined) {
                    return undefined;
                }
                return this.option.infoLink;
            }
        },
        methods : {
            reset : function() {
                if (this.$refs.autocomplete != undefined) {
                    this.$refs.autocomplete.clear();
                }
            },
            getOptionsFor : function(group) {
                var ret = new Array();
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

    var app = new Vue({
        el : "#advancedsearch",
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
                searchFieldName : 'institution',
                resultSuffix : 'institutions',
                fieldName : "resourceCreatorProxies[].institution.id",
                idName : "resourceCreatorProxies[].institution.id",
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
                Vue.set(self, "columnMap", response.data.datasetReferences);
                self.selectOptions.forEach(function(opt) {
                    if (opt.name == 'Investigation Types') {
                        Vue.set(opt, "choices", response.data.investigationTypes);
                    }
                });

                var dsid = document.body.getAttribute("data-mapped-dataset-id");
                if (dsid != undefined) {
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
                var dataset = undefined;
                this.columnMap.forEach(function(ds) {
                    console.log(ds, datasetId);
                    if (ds.id == datasetId) {
                        dataset = ds;
                    }
                    
                });

                if (dataset == undefined) {
                    return;
                }

                var self = this;
                console.log("adding column info for:", datasetId, dataset);
                dataset.columns.forEach(function(field) {
                    var values = [];
                    var type = "basic";
                    if (field.intValues.length > 0) {
                        values = field.intValues;
                    }
                    if (field.floatValues.length > 0) {
                        values = field.floatValues;
                    }
                    if (field.values.length > 0) {
                        values = field.values;
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
                        fieldValues : values,
                        infoLink : '/dataset/column/' + datasetId + "/" + field.id,
                        choices : values,
                        columnType : field.columnDataType
                    })

                });

            }
        },
    });
})(console, window, Vue, axios, TDAR);