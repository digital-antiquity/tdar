TDAR.vuejs.tagging= (function(console, ctx, Vue, axios, TDAR) {
    "use strict";

    if (document.getElementById("sel") != undefined) {


    var app = new Vue({
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
                epochtime: -1,
                submitterId: undefined,
                projectId: undefined,
                inheritanceDisabled: true,
                investigationTypes: [],
                project: {},
                creditRoles: ["ANALYST","COLLABORATOR","CONTACT","CONTRIBUTOR","FIELD_DIRECTOR","LAB_DIRECTOR","LANDOWNER","PERMITTER","PREPARER","PRINCIPAL_INVESTIGATOR","PROJECT_DIRECTOR","PUBLISHER","REPOSITORY","SPONSOR","SUBMITTED_TO","TRANSLATOR"],
                noteTypes: [
                    {value:"GENERAL", name:"General Note"},
                    {value:"REDACTION", name:"Redaction Note"},
                    {value:"RIGHTS_ATTRIBUTION", name:"Rights & Attribution"},
                    {value:"ADMIN", name:"Administration Note"}
                    ],
                resource: { otherKeywords:[], siteNameKeywords:[], siteTypeKeywords:[], materialKeywords:[], geographicKeywords:[], investigationTypes:[], cultureKeywords:[], temporalKeywords:[],
                    resourceNotes: [{}],  latitudeLongitudeBoxes: [{north: undefined, south: undefined, east: undefined,west: undefined } ],
                    individualInstitutionalRoles: [{id: undefined, role: undefined , creator: {institution:{ name: undefined}}, id: undefined}] }
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
                        this.inherit(o,b,'material', 'activeMaterialKeywords');
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
                            _app.resource.resourceNotes.push(JSON.parse(JSON.stringify(n)) );
                        });
                        if (this.resource.resourceNotes.length == 0) {
                            this.resource.resourceNotes.push({});
                        }
                        }
                    }
                },
                computed: {
                    inheritanceDisabledClass: function() {
                        if (this.inheritanceDisabled == true) {
                            return "disabled";
                            }
                        return "";
                    }
                },
                created: function() {
                    if (document.getElementById('json') != undefined) {
                        var json = JSON.parse(document.getElementById('json').innerText );
                        // json.submitter = {id:135028};
                        Vue.set(this,'resource',json);
                         if (json.submitterRef != undefined && json.submitterRef.indexOf(":") != -1) {
                            Vue.set(this,'submitterId',parseInt(json.submitterRef.substring(json.submitterRef.indexOf(":") + 1)));
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

                        Vue.set(this, "investigationTypes", JSON.parse(document.getElementById('investigationTypes').innerText ));
                    }
                    Vue.set(this,"epochtime", Date.now() + 15000);
                    Vue.set(this,"accountId",220);
                    // Vue.set("resource","submitter",{});
                    // this.resource.submitter = {id:8344, properName:'adam brin'};
                    var self = this.resource.activeLatitudeLongitudeBoxes[0];
                    Vue.nextTick(function() {
                        TDAR.leaflet.initEditableMap($('#vueeditablemap'), function(e){
                            Vue.set(self, "west", e.minx);
                            Vue.set(self, "east", e.maxx);
                            Vue.set(self, "north", e.maxy);
                            Vue.set(self, "south", e.miny);
                            });
                    });
                },
                methods: {
                    removeNote: function(idx) {
                        if (this.resource.resourceNotes.length > 1) {
                            this.resource.resourceNotes.splice(idx,1);
                        }
                        if (this.resource.resourceNotes.length == 0) {
                            this.resource.resourceNotes.push({});
                        }
                        if (this.resource.resourceNotes.length == 1) {
                            this.resource.resourceNotes[0]= {};
                        }
                    },
                    setsubmitter: function(submitter) {
                        console.log("setsubmitter", submitter);
                        Vue.set(this.resource,"submitter",submitter );
                    },
                    inherit: function(o, b, ref, vals) {
                        if (this.project[vals] != undefined && o == true) {
                            this.$refs[ref].setValues(this.project[JSON.parse(JSON.stringify(vals)) ]); 
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
    });
    }
})(console, window, Vue, axios, TDAR);