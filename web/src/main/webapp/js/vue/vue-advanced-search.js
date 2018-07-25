TDAR.vuejs.advancedSearch = (function(console, $, ctx, Vue, axios, TDAR) {
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
                if (n.type == 'map') {
                    Vue.nextTick(function() {
                        TDAR.leaflet.initEditableLeafletMaps();
                    });
                }
            }
        },
        mounted : function() {
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
    /**
     * 
     * <!-- <optgroup label="Basic Fields"> <option value="TITLE">Title</option> <option value="DESCRIPTION">Description</option> <option
     * value="CONTENTS">Full-Text</option> <option value="RESOURCE_CREATOR_PERSON">Person</option> <option value="RESOURCE_CREATOR_INSTITUTION">Institution</option>
     * <option value="TDAR_ID">Id</option> <option value="COVERAGE_DATE_CALENDAR">Calendar Dates</option> <option
     * value="COVERAGE_DATE_RADIOCARBON">RadioCarbon Dates</option> <option value="PROJECT">Project</option> <option value="COLLECTION">Collection</option>
     * <option value="FILENAME">File Name</option> <option value="DATE_CREATED">Year</option> <option value="DATE_REGISTERED">Date Created</option> <option
     * value="DATE_UPDATED">Date Updated</option> </optgroup> <optgroup label="Controlled Keywords"> <option value="KEYWORD_INVESTIGATION">Investigation Types</option>
     * <option value="KEYWORD_SITE">Site Type(Controlled)</option> <option value="KEYWORD_MATERIAL">Material Types</option> <option
     * value="KEYWORD_CULTURAL">Culture Keywords</option> </optgroup> <optgroup label="Freeform Keywords"> <option value="FFK_GEOGRAPHIC">Geographic Keywords</option>
     * <option value="FFK_SITE">Site Names</option> <option value="FFK_SITE_TYPE">Site Type</option> <option value="FFK_CULTURAL">Culture Keywords</option>
     * <option value="FFK_MATERIAL">Material Keywords</option> <option value="FFK_TEMPORAL">Temporal Keywords</option> <option value="FFK_GENERAL">General
     * Keywords</option>
     */

    var app = new Vue({
        el : "#advancedsearch",
        data : {
            selectOptions : [ {
                name : 'Title',
                group : 'general',
                type : 'basic'
            }, {
                name : 'Description',
                group : 'general',
                type : 'basic'
            }, {
                name : 'Full-Text',
                group : 'general',
                type : 'basic'
            }, {
                name : 'Date',
                group : 'general',
                type : 'integer'
            }, {
                name : 'Id',
                group : 'general',
                type : 'integer'
            }, {
                name : 'Date Added',
                group : 'general',
                type : 'date'
            }, {
                name : 'Date Updated',
                group : 'general',
                type : 'date'
            }, {
                name : 'Map',
                group : 'general',
                type : 'map'
            }, {
                name : 'Project',
                group : 'general',
                type : 'basic',
                autocompleteUrl : '/api/lookup/resource',
                autocompleteSuffix : 'resourceTypes[0]=PROJECT',
                fieldName : 'term',
                resultSuffix : 'resources'
            }, {
                name : 'Collection',
                group : 'general',
                type : 'basic',
                autocompleteUrl : '/api/lookup/collection',
                fieldName : 'term',
                resultSuffix : 'collections'
            }, {
                name : 'Person',
                group : 'general',
                type : 'basic',
                autocompleteUrl : '/api/lookup/person',
                fieldName : 'term',
                resultSuffix : 'people'
            }, {
                name : 'Institution',
                group : 'general',
                type : 'basic',
                autocompleteUrl : '/api/lookup/institution',
                fieldName : 'institution',
                resultSuffix : 'institutions'
            }, {
                name : 'Site Name',
                group : 'keywords',
                type : 'basic',
                autocompleteUrl : '/api/lookup/keyword',
                autocompleteSuffix : 'keywordType=SiteNameKeyword',
                fieldName : 'term',
                resultSuffix : 'items'
            }, {
                name : 'Site Type',
                group : 'keywords',
                type : 'basic',
                autocompleteUrl : '/api/lookup/keyword',
                autocompleteSuffix : 'keywordType=SiteTypeKeyword',
                fieldName : 'term',
                resultSuffix : 'items'
            }, {
                name : 'Geographic Keywords',
                group : 'keywords',
                type : 'basic',
                autocompleteUrl : '/api/lookup/keyword',
                autocompleteSuffix : 'keywordType=GeographicKeyword',
                fieldName : 'term',
                resultSuffix : 'items'
            }, {
                name : 'Culture Keywords',
                group : 'keywords',
                type : 'basic',
                autocompleteUrl : '/api/lookup/keyword',
                autocompleteSuffix : 'keywordType=CultureKeyword',
                fieldName : 'term',
                resultSuffix : 'items'
            }, {
                name : 'Material Keywords',
                group : 'keywords',
                type : 'basic',
                autocompleteUrl : '/api/lookup/keyword',
                autocompleteSuffix : 'keywordType=MaterialKeyword',
                fieldName : 'term',
                resultSuffix : 'items'
            }, {
                name : 'Temporal Keywords',
                group : 'keywords',
                type : 'basic',
                autocompleteUrl : '/api/lookup/keyword',
                autocompleteSuffix : 'keywordType=TemporalKeyword',
                fieldName : 'term',
                resultSuffix : 'items'
            }, {
                name : 'Other Keywords',
                group : 'keywords',
                type : 'basic',
                autocompleteUrl : '/api/lookup/keyword',
                autocompleteSuffix : 'keywordType=OtherKeyword',
                fieldName : 'term',
                resultSuffix : 'items'
            }, {
                name : 'Investigation Type',
                group : 'keywords',
                type : 'basic',
                autocompleteUrl : '/api/lookup/keyword',
                autocompleteSuffix : 'keywordType=InvestigationType',
                fieldName : 'term',
                resultSuffix : 'items'
            }, ],
            rows : [ {
                option : '',
                value : ''
            } ]
        },
        computed : {},
        methods : {
            addRow : function() {
                this.rows.push({
                    option : '',
                    value : ''
                });
            },
            removeRow : function(idx) {
                this.rows.splice(idx, 1);

            },
        },
    });
})(console, jQuery, window, Vue, axios, TDAR);