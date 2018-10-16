TDAR.vuejs.resourceEdit= (function(console, ctx, Vue, axios, TDAR) {
    "use strict";

    
    Vue.component('gauge', {
        template: '<div id="chart"/>',
        props: [ 'value' ],
        mounted: function() {
            var chart = c3.generate({
                data: {
                    columns: [
                        ['data', this.value]
                    ],
                    type: 'gauge',
                    onclick: function (d, i) { console.log("onclick", d, i); },
                    onmouseover: function (d, i) { console.log("onmouseover", d, i); },
                    onmouseout: function (d, i) { console.log("onmouseout", d, i); }
                },
                gauge: {
//                    label: {
//                        format: function(value, ratio) {
//                            return value;
//                        },
//                        show: false // to turn off the min/max labels.
//                    },
//                min: 0, // 0 is default, //can handle negative min e.g. vacuum / voltage / current flow / rate of change
                max: 30, // 100 is default
//                units: ' %',
//                width: 39 // for adjusting arc thickness
                },
                color: {
                    pattern: ['#FF0000', '#F97600', '#F6C600', '#60B044'], // the three color levels for the percentage values.
                    threshold: {
//                        unit: 'value', // percentage is default
//                        max: 200, // 100 is default
                        values: [30, 60, 90, 100]
                    }
                },
                size: {
                    height: 180
                }
            });
            $(this.$el).data('chart', chart);
        },
        methods: {
            setValue: function( n ) {
                console.log('val', n);
                var chart = $(this.$el).data('chart');
                console.log(chart);
              if (chart != undefined) {
                    chart.load({columns: [['data',n]]});
                }
            },
        },
        beforeDestroy: function() {  }
      });
    

    
    
    Vue.component('editablemap', {
        template: '#map-template',
        props: [ 'north', 'south', 'east', 'west', 'showCoords' ],
        mounted: function() {
            var self = this;
            console.log(self.$el);
//            console.log($(".vue-editable-map",$(self.$el)));
            Vue.nextTick(function(){
                
            TDAR.leaflet.initEditableMap($(self.$el), function(e){
                console.log(e);
//                Vue.set(self, "west", e.minx);
//                Vue.set(self, "east", e.maxx);
//                Vue.set(self, "north", e.maxy);
//                Vue.set(self, "south", e.miny);
                $(".locateCoordsButton").click();
                    self.$emit("map-value-set", e);
                });
            });

        },
        methods: {
            setValue: function( n ) {
                console.log('val', n);
                var chart = $(this.$el).data('chart');
                console.log(chart);
              if (chart != undefined) {
                    chart.load({columns: [['data',n]]});
                }
            },
        },
        beforeDestroy: function() {  }
      });
    
    
    function dereference(obj) {
        return JSON.parse(JSON.stringify(obj));
    }
    Vue.use(VueObserveVisibility);
    Vue.directive('observe-visibility', VueObserveVisibility.ObserveVisibility);

    function countUsed(arr) {
        var used = 0;
        arr.forEach(function(itm) {
            if (itm != undefined && itm.length > 0) {
                var i = itm[0];
                if (itm['note'] != undefined && itm['note'] != '') {
                    used++;
                } 
                else if (itm['note'] == undefined) {
                    used++;
                }
            }
        });
        return used;
    }
    
    var __init = {
        el : "#sel",
        data : {
                separator: ";" ,
                autocomplete_url:'/api/lookup/resource',
                autocomplete_suffix:'',
                result_suffix:'resources',
                allow_create:true,
                nameField: 'title',
                idField: 'id',
                accountId: 219,
                accountName: "account name",
                accounts: [],
                epochtime: -1,
                submitterId: undefined,
                submitterName: undefined,
                projectId: undefined,
                inheritanceDisabled: true,
                showCoords:false,
                investigationTypes: [],
                materialTypes: [],
                project: {},
                fileUploadConfig: {
                    resourceId : -1,
                    userId : -1,
                    validFormats : ['pdf','doc','docx','png','gif','jpg','tif','xls'],
                    sideCarOnly : false,
                    ableToUpload : true,
                    maxNumberOfFiles : 50,
                    files: [],
                    requiredOptionalPairs:[]
                },
                showMaterialTypeSection: false,
                showInvestigationTypeSection: false,
                showCultureSection: false,
                showSiteSection: false,
                showWhereSection: false,
                showWhenSection: false,
                showIdentifierSection: false,
                showNotesSection: false,
                primaryRoles: [],
                creditRoles: ["ANALYST","COLLABORATOR","CONTACT","CONTRIBUTOR","FIELD_DIRECTOR","LAB_DIRECTOR","LANDOWNER","PERMITTER","PREPARER","PRINCIPAL_INVESTIGATOR","PROJECT_DIRECTOR","PUBLISHER","REPOSITORY","SPONSOR","SUBMITTED_TO","TRANSLATOR"],
                coverageDateTypes: [
                    {value:"CALENDAR_DATE", name:"Calendar Date"},
                    {value:"RADIOCARBON_DATE", name:"Radiocarbon Date"},
                ],
                noteTypes: [
                    {value:"GENERAL", name:"General Note"},
                    {value:"REDACTION", name:"Redaction Note"},
                    {value:"RIGHTS_ATTRIBUTION", name:"Rights & Attribution"},
                    {value:"ADMIN", name:"Administration Note"}
                    ],
                resource: { otherKeywords:[], siteNameKeywords:[], siteTypeKeywords:[], controlledMaterialKeywords:[], uncontrolledMaterialKeywords:[], geographicKeywords:[], investigationTypes:[], cultureKeywords:[], temporalKeywords:[],
                    latitudeLongitudeBoxes: [{north: undefined, south: undefined, east: undefined,west: undefined } ],
                    individualInstitutionalRoles: [ {id: undefined, role: undefined , creator: {institution:{ name: undefined}, id: undefined} } ] }
                },
                created: function() {
                    var self = this;
                    if (document.getElementById('json') != undefined) {
                        var json = JSON.parse(document.getElementById('json').innerText );
                        Vue.set(this,'resource',json);
                         if (json.submitterRef != undefined && json.submitterRef.indexOf(":") != -1) {
                            Vue.set(this,'submitterId',parseInt(json.submitterRef.substring(json.submitterRef.indexOf(":") + 1)));
                            Vue.set(this,"submitterName",JSON.parse(document.getElementById('submitter').innerText).properName );
                        }
                         Vue.set(this,"primaryRoles",JSON.parse(document.getElementById('primaryRoles').innerText));
                         Vue.set(this,"creditRoles",JSON.parse(document.getElementById('otherRoles').innerText));

                         if (this.resource.resourceNotes == undefined) {
                             this.resource.resourceNotes = [];
                         } 
                         if (this.resource.resourceNotes.length == 0) {
                             this.resource.resourceNotes.push(this.getBlankNote());
                         } else {
                             this.showNotesSection = true;
                         }
                         if (this.resource.coverageDates == undefined) {
                             this.resource.coverageDates = [];
                         }  
                         if (this.resource.coverageDates.length == 0) {
                             this.resource.coverageDates.push(this.getBlankDate());
                         } else {
                             showWhereSection = true;
                         }
                         var uploadConfig = document.getElementById('fileUploadSettings').innerText;
                         if (uploadConfig != undefined && uploadConfig.trim() != '') {
                             uploadConfig = JSON.parse(uploadConfig);
                             if (uploadConfig != undefined) {
                                 Vue.set(this,"fileUploadConfig", uploadConfig);
                             }
                         }
                         
                        if (json.activeIndividualAndInstitutionalCredit == undefined || json.activeIndividualAndInstitutionalCredit.length == 0) {
                            json.activeIndividualAndInstitutionalCredit = [{creator:{person:{institution:{}}}}];
                        }
                        if (json.contentOwners == undefined || json.contentOwners.length == 0) {
                            json.contentOwners = [{creator:{person:{institution:{}}}}];
                        }
                        if (json.activeLatitudeLongitudeBoxes == undefined || json.activeLatitudeLongitudeBoxes.length == 0) {
                            json.activeLatitudeLongitudeBoxes = [{north: undefined, south: undefined, east: undefined,west: undefined } ];
                        }

                        if (json.controlledMaterialKeywords == undefined) {
                            json.controlledMaterialKeywords = [];
                            json.uncontrolledMaterialKeywords = [];
                        }
                        if (json.id != undefined) {
                            Vue.set(this.fileUploadConfig,'resourceId', json.id);
                        }
                        Vue.set(this, "investigationTypes", JSON.parse(document.getElementById('investigationTypes').innerText ));
                        Vue.set(this, "materialTypes", JSON.parse(document.getElementById('materialTypes').innerText ));
                        this.applyMaterialKeywords(json.activeMaterialKeywords);
                    }
                    Vue.set(this,"epochtime", Date.now() + 15000);
                    Vue.set(this,"accountId",220);
                    Vue.set(this,"accounts",JSON.parse(document.getElementById('activeAccounts').innerText ));
                    this.accounts.forEach(function(act) {
                       if (act.id == self.accountId) {
                           Vue.set(self,"accountName",act.name);
                       } 
                    });
                    var self = this.resource.activeLatitudeLongitudeBoxes[0];
                },
                mounted: function() {
                },
                watch: {
                    "resource.inheritingInvestigationInformation": function(o, b) {
                        this.inherit(o,b,'investigationType', 'activeInvestigationTypes');
                    },
                    "resource.inheritingSiteInformation": function(o, b) {
                        this.inherit(o,b,'siteName', 'activeSiteNameKeywords');
                        this.inherit(o,b,'siteType', 'activeSiteTypeKeywords');
                    },
                    "resource.inheritingTemporalInformation": function(o, b) {
                        this.inherit(o,b,'temporal', 'activeTemporalKeywords');
                        if (this.project != undefined && this.project.activeCoverageDates != undefined) {
                            this.resource.coverageDates.length = 0;
                            var _app = this;
                            this.project.activeCoverageDates.forEach(function(n) {
                                console.log(n);
                                _app.resource.coverageDates.push(derefrence(n) );
                            });
                            if (this.resource.coverageDates.length == 0) {
                                this.resource.coverageDates.push(this.getBlankDate());
                            }
                        }

                    },
                    "resource.inheritingIndividualAndInstitutionalCredit": function(o, b) {
                        if (this.project != undefined && this.project.activeIndividualAndInstitutionalCredit != undefined) {
                        this.resource.activeIndividualAndInstitutionalCredit.length = 0;
                        var self = this;
                        this.project.activeIndividualAndInstitutionalCredit.forEach(function(role_) {
                            var role = JSON.parse(JSON.stringify(role_));
                            role.init = "";
                            role.id = undefined;
                            self.resource.activeIndividualAndInstitutionalCredit.push(role);
                        });
                        
                        }
                    },
                    "resource.inheritingMaterialInformation": function(o, b) {
                        if (this.project != undefined && this.project.activeMaterialKeywords != undefined) {
                            console.log("inherit:", this.project.activeMaterialKeywords);
                            this.resource.controlledMaterialKeywords.length = 0;
                            this.resource.uncontrolledMaterialKeywords.length = 0;
                            this.$refs.materialControlled.disable();
                            this.applyMaterialKeywords(this.project.activeMaterialKeywords);
                            this.$refs.materialControlled.refresh();
                        }
                    },
                    "resource.inheritingCulturalInformation": function(o, b) {
                        this.inherit(o,b,'culture', 'activeCultureKeywords');
                    },
                    "resource.inheritingSpatialInformation": function(o, b) {
                        this.inherit(o,b,'geographic', 'activeGeographicKeywords');
                        if (this.project != undefined && this.project.activeLatitudeLongitudeBoxes != undefined) {
                            this.resource.activeLatitudeLongitudeBoxes[0].west = this.project.activeLatitudeLongitudeBoxes[0].obfuscatedWest;
                            this.resource.activeLatitudeLongitudeBoxes[0].east = this.project.activeLatitudeLongitudeBoxes[0].obfuscatedEast;
                            this.resource.activeLatitudeLongitudeBoxes[0].south = this.project.activeLatitudeLongitudeBoxes[0].obfuscatedSouth;
                            this.resource.activeLatitudeLongitudeBoxes[0].north = this.project.activeLatitudeLongitudeBoxes[0].obfuscatedNorth;
                        console.log(this.resource.activeLatitudeLongitudeBoxes[0]);
                        Vue.nextTick(function() {
                            $(".locateCoordsButton").click();
                        });
                            }
                    },
                    "resource.inheritingOtherInformation": function(o, b) {
                        this.inherit(o,b,'other', 'activeOtherKeywords');
                    },
                    "resource.inheritingIdentifierInformation": function(o, b) {
                        // this.inherit(o,b,'culture', 'activeCultureKeywords');
                    },
                    "resource.inheritingCollectionInformation": function(o, b) {
                      // this.inherit(o,b,'culture', 'activeCultureKeywords');
                    },
                    "resource.inheritingNoteInformation": function(o, b) {
                        if (this.project != undefined && this.project.activeResourceNotes != undefined) {
                            this.resource.resourceNotes.length = 0;
                            var _app = this;
                            this.project.activeResourceNotes.forEach(function(n) {
                                console.log(n);
                                _app.resource.resourceNotes.push(derefrence(n) );
                            });
                            if (this.resource.resourceNotes.length == 0) {
                                this.resource.resourceNotes.push(this.getBlankNote());
                            }
                        }
                    }
                },
                computed: {
                    
                    quality: function() {
                        var ret = 0;
                        var r = this.resource;
                        if (r.title != undefined) { ret += 5; }
                        if (r.description != undefined) { ret += 5; }
                        if (r.date > 0) { ret += 5};
                        
                        countUsed([r.materialTypes,r.investigationTypes, r.siteTypeKeywords, r.siteNameKeywords, r.resourceNotes, r.geographicKeywords]);
                        
                        if (this.$refs['gauge'] != undefined) {
                            this.$refs['gauge'].setValue(ret);
                        }
                        return ret;
                        
                    },
                    inheritanceDisabledClass: function() {
                        if (this.inheritanceDisabled == true) {
                            return "disabled";
                            }
                        return "";
                    },
                    materialTypeEnabled: function() {
                        if (this.showMaterialTypeSection == true || this.resource.controlledMaterialKeywords.length + this.resource.uncontrolledMaterialKeywords.length > 0) {
                            return true;
                        }
                        return false;
                    },
                    investigationTypeEnabled: function() {
                        if (this.showInvestigationTypeSection == true || this.resource.investigationTypes.length > 0) {
                            return true;
                        }
                        return false;
                    },
                    notesEnabled: function() {
                        if (this.showNotesSection == true || this.resource.resourceNotes.length > 0 && this.resource.resourceNotes[0].note != undefined && this.resource.resourceNotes[0].note != '') {
                            console.log(this.resource.resourceNotes,this.showNotesSection);
                            return true;
                        }
                        return false;
                    },
                    identifiersEnabled: function() {
                        if (this.showIdentifierSection == true ) {
                            return true;
                        }
                        return false;
                    },
                    siteEnabled: function() {
                        if (this.showSiteSection == true || this.resource.siteNameKeywords.length + this.resource.siteTypeKeywords.length > 0) {
                            return true;
                        }
                        return false;
                    },
                    whereEnabled: function() {
                        if (this.showWhereSection == true || this.resource.geographicKeywords.length > 0 || this.resource.latitudeLongitudeBoxes.length > 0 && this.resource.latitudeLongitudeBoxes[0].west != undefined) {
                            return true;
                        }
                        return false;
                    },
                    whenEnabled: function() {
                        if (this.showWhenSection == true || this.resource.temporalKeywords.length > 0 || this.resource.coverageDates.length > 0  && this.resource.coverageDates[0].startDate != undefined) {
                            return true;
                        }
                        return false;
                    },
                    identifierEnabled: function() {
                        if (this.showNotesSection == true || this.resource.resourceNotes.length > 0) {
                            return true;
                        }
                        return false;
                    },
                    cultureEnabled: function() {
                        if (this.showCultureSection == true || this.resource.cultureKeywords.length > 0) {
                            return true;
                        }
                        return false;
                    }
                },
                methods: {
                    visibilityChanged (isVisible, entry) {
                        this.isVisible = isVisible;
                        console.log(entry, isVisible);
                        if (isVisible) {
                            TDAR.leaflet.initEditableMap($('#vueeditablemap'), function(e){
                            console.log(e);
                            Vue.set(self, "west", e.minx);
                            Vue.set(self, "east", e.maxx);
                            Vue.set(self, "north", e.maxy);
                            Vue.set(self, "south", e.miny);
                            });
                        }
                        
                    },
                    removeNote: function(idx) {
                        if (this.resource.resourceNotes.length > 1) {
                            this.resource.resourceNotes.splice(idx,1);
                        }
                        if (this.resource.resourceNotes.length == 0) {
                            this.resource.resourceNotes.push(this.getBlankNote());
                        }
                        if (this.resource.resourceNotes.length == 1) {
                            this.resource.resourceNotes[0]= this.getBlankNote();
                        }
                    },
                    removeDate: function(idx) {
                        if (this.resource.coverageDates.length > 1) {
                            this.resource.coverageDates.splice(idx,1);
                        }
                        if (this.resource.coverageDates.length == 0) {
                            this.resource.coverageDates.push(this.getBlankDate());
                        }
                        if (this.resource.coverageDates.length == 1) {
                            this.resource.coverageDates[0]= this.getBlankDate();
                        }
                    },
                    getBlankNote: function() {
                        return {type:"GENERAL"};
                    },
                    getBlankDate: function() {
                        return {type:"CALENDAR_DATE"};
                    },
                    setsubmitter: function(submitter) {
                        console.log("setsubmitter", submitter);
                        Vue.set(this.resource,"submitter",submitter );
                    },
                    submitForm:function(){
                        this.$refs['form'].submit();
                    },
                    applyMaterialKeywords: function(keywords) {
                        var controlled = this.resource.controlledMaterialKeywords;
                        var uncontrolled = this.resource.uncontrolledMaterialKeywords;
                        var self = this;
                        keywords.forEach(function(k){
                            var seen = false;
                            console.log(k);
                            self.materialTypes.forEach(function(c){
                                if (c.id == k.id) {
                                    controlled.push(k);
                                    seen = true;
                                }
                            });
                            if (seen == false) {
                                uncontrolled.push(k);
                            }
                        });
                    },
                    inherit: function(o, b, ref, vals) {
                        if (this.project[vals] != undefined && o == true) {
                            this.$refs[ref].setValues(this.project[dereference(vals) ]); 
                            this.$refs[ref].disable();
                        } else {
                            this.$refs[ref].enable();
                        }
                    },
                    addCreatorRow: function(el) {
                        el.push({creator:{person:{institution:{}}}});
                        this.$forceUpdate()
                    },
                    deleteCreatorRow: function(el,idx) {
                        var arr = this.resource.activeIndividualAndInstitutionalCredit;
                        if (el == 'authorshipProxies') {
                            arr = this.resource.contentOwners;
                        }
                        console.log(arr, idx);
                        if (arr.length > 1) {
                        arr.splice(idx,1);
                            }
                        this.$forceUpdate()
                    },
                    selectProject: function(val) {
                        console.log("project:", val);
                        if (val == undefined) {
                            return;
                        } 
                        this.projectId = val.id;
                        var self = this;
                        axios.get("/project/json/" + val.id).then(function(res) {
                            // Vue.set(self, "isLoading",false);
                            Vue.set(self, 'project',res.data);
                            Vue.set(self, 'inheritanceDisabled',false);
                        }).catch(function(thrown) {
                            if (!axios.isCancel(thrown)) {
                                console.error(thrown);
                            }
                        });
                    }
                    
                }
    };

    var _init = function(selector){
        if (selector != undefined){
            __init["el"] = selector;
        }
        var app = new Vue(__init);
        return app;
    }
    
    return {
        init : _init,
    }
})(console, window, Vue, axios, TDAR);