TDAR.vuejs.advancedSearch = (function(console, ctx, Vue, axios, TDAR) {
    "use strict";
    var UNDEFINED = "undefined";
    var MAXLEN_CHECKBOXLIST = 50;

    /**
     * "Part" Vue Control.
     */
    Vue.component('part', {
        template : "#dataset-search-row-template",
        props : [ "row", "index", "options" ],
        data : function() {
            return {
                //todo: move this to property (or maybe just remove debugmode code outright, due to FOUC
                debugMode: true
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
        },
        methods : {
            reset : function() {
                // Todo: do whatever is appropriate to represent "clearing" the underlying control's value
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

            clearRow : function() {
                this.reset();
                this.$emit("removerow", this.index);
            },

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
            //todo: move this to property (or maybe just remove debugmode code outright, due to FOUC
            debugMode: true,
            termOperator: 'AND',
            columnMap : {},
            selectOptions : [],
            rows : [ {
                option : [],
                value : []
            } ],
            jsondata: ''
        }},
        mounted : function() {
            var self = this;

            // Look for search info in document data first,
            var documentData  = TDAR.loadDocumentData();
            if(!!documentData['searchinfo']){
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
        },
        computed : {},
        methods : {

            processSearchInfo: function(data) {
                var self = this;
                console.log(data);
                // Vue.set(self, "columnMap", data['datasetReferences']);
                self.columnMap = data['datasetReferences'];

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

            /**
             * Remove a row (if there are two or more rows)
             * @param idx
             */
            removeRow : function(idx) {
                if(this.rows.length > 1){
                    this.rows.splice(idx, 1);
                }
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
                        //console.log("field:%s\t values:%s", field.name, field.values.length);
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

            getSelectOptionByName:  function(name) {
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


        },
        components: {
            checkboxlist: TDAR.vuejs.checkboxlist,
            selectlist: TDAR.vuejs.selectlist
        }
    });
})(console, window, Vue, axios, TDAR);
