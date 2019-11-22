TDAR.vuejs.advancedSearch = (function(console, ctx, Vue, axios, TDAR, formstate) {
    "use strict";
    var UNDEFINED = "undefined";
    var MAXLEN_CHECKBOXLIST = 50;
    var COLUMN_DEDUPE_LIMIT = 500;

    /**
     * "Part" Vue Control.
     */
    Vue.component('part', {
        template : "#dataset-search-row-template",
        props : { "row":{required: false},
            "index":{required: false},
            "optionsmap":{required: false},
            "columns":{required: false},
            debugMode: {
                type: Boolean,
                required: false,
                default: false
            }
        },

        data : function() {
            return {
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
            "mapped-dataset-id": Number,
            debugMode: {
                type: Boolean,
                required: false,
                default: false
            },

        },
        watch: {
        },

        data : function() { return {
            //todo: move this to property (or maybe just remove debugmode code outright, due to FOUC
            termOperator: 'AND',
            columnMap : {},
            selectOptions : [],
            rows : [ {
                option : [],
                value : []
            } ],
            jsondata: '',
            columnsByName: {},
            columnsById: {},
            optionsByName: {},
            documentData: {},
            operator: "AND"
        }},
        mounted : function() {
            var self = this;

            // Look for search info in document data first,
            var documentData  = TDAR.loadDocumentData();
            this.documentData  = documentData;
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

            // build map of columns, keyed by column display name (todo: maybe ID instead)
            this.columnsByName =  this.selectOptions.reduce((obj, opt) => {
                obj[opt.name] = opt;
                self.columnsById[opt.id] = opt;
                return obj;
            }, {});


            // reconstitute form state saved from either "refine search" parameters, or if we came from a back button
            this.rebuildState();

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

            addRow : function(row) {
                var _row = row;
                if(!_row) {
                    _row = {option: '', value: []}
                }
                this.rows.push(_row);
            },

            submit : function() {
                console.log(this.$refs.form);
                this.jsondata = this.serializeState();
                formstate(this.jsondata);
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
                        console.log(" dedupe section:: field:%s value.length:%s", field.name, field.values.length);
                        // FIXME: magic numbers
                        // Perform case-insensitive dedupe if list isn't too big (
                        //console.log("field:%s\t values:%s", field.name, field.values.length);
                        if(field.values.length > COLUMN_DEDUPE_LIMIT) {
                            values = field.values;
                        } else {
                            var oldlen = field.values.length;
                            field.values.sort(function(a,b){
                                //sort by lowercase w/ secondary sort on case so that we favor title-case dupes
                                var ret = 0;
                                if(a.toLowerCase() < b.toLowerCase()) {
                                    ret = -1;
                                } else if(a.toLowerCase() > b.toLowerCase()) {
                                    ret = 1;
                                } else {
                                    ret = a.localeCompare(b)
                                }
                                return ret;
                            });
                            var lcvalues = [];
                            values = [];
                            field.values.forEach(function(val, idx){
                                var lcval = val.toLowerCase();
                                if(lcvalues.indexOf(lcval) === -1) {
                                    lcvalues.push(lcval);
                                    values.push(val);
                                }
                            });
                            var newlen = field.values.length;
                            console.log("field deduped: %s  oldlen:%s newlen:%s (%s removed)", field.name, oldlen, newlen, oldlen-newlen);
                        }

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
                        columnType : field.columnDataType
                    });

                    self.optionsByName[field.displayName] = values;

                });
            },


            /**
             *
             * TODO: docment me
             */
            rebuildState: function() {
                var self = this;


                // back button state trumps refine data
                var stateJson = formstate();

                if(stateJson) {
                    this.rows.splice(0, this.rows.length);
                    var state = JSON.parse(stateJson);
                    var staterows = state.rows;
                    staterows.filter(function(_row){
                        return !!_row.name;
                    }).forEach(function(_row){
                        self.addRow({
                            option: self.columnsByName[_row.name],
                            value: _row.values
                        });
                    });
                    self.operator = state.operator || "AND";
                } else if(this.documentData['refinesearchinfo']){
                    self.deserializeState(this.documentData['refinesearchinfo'][0]);
                }
            },

            /**
             * Serialize the current state of the form as an array of name-value pairs (excluding blank and/or invalid
             * rows.
             *
             * FIXME:  Currently we only serialize dataValues fields (aka custom data fields)
             */

            serializeState: function() {
                // we only care about the column name and chosen values for each row
                var state = {
                    rows: [],
                    operator: this.operator
                };
                state.rows = this.rows.map(function(row, idx){
                    return {name: row.option.name, values: row.value}
                });
                //don't forget the group operator
                state.operator = this.operator;
                var jsonstate = JSON.stringify(state);
                return jsonstate;
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
            deserializeState: function(strutsdata) {
                if(!strutsdata) {return;}
                if(strutsdata.length == 0) {return;}
                var self  = this;
                self.clearState();
                var datavalues = strutsdata.dataValues;
                var _rows = datavalues.map(function(dv){
                    return {option: self.columnsById[dv.columnId], value:  dv.value}
                }).forEach(function(_row){
                   self.addRow(_row);
                });
                self.operator = strutsdata.operator || "AND"
            },


            setCheckboxRow: function(){
                console.log('method called: setCheckboxRow');
                var row = this.rows[0];
                row.value.push('Indeterminate');
                row.option = this.columnsByName["condition"];

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
})(console, window, Vue, axios, TDAR, TDAR.formstate);
