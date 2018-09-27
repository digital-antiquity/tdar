TDAR.vuejs.creatorwidget = (function(console, ctx, Vue, axios) {
    "use strict";

    if (document.getElementById("creatorwidget-template") != undefined ) {
        var creatorlookup = Vue.component('creatorrolelookup', {
            name: "creatorrolelookup",
            template: "#creatorwidget-template",
            props: {
                resourcecreator : {
                    type: Object,
                    default:  function() {return { creator: { institution:{ name: undefined}}}} 
                },
                row: {
                    type:Number,
                    default: 0
                },
                roles: {
                    type:Object,
                    default: function() { return {'INSTITUTION':[], 'PERSON':[]}; }
                },
                prefix: {
                    type:String,
                    default: 'authorshipProxies'
                }
            },
            data: function() {
                return {
                showEditInstitution: false,
                showEditPerson: false,
                actualRoles:[],
                creatorId:-1,
                toggle: "PERSON"
                }
            },
            created: function() {
                if (this.resourcecreator.creator.firstName == undefined) {
                    this.toggle = 'PERSON';
                    Vue.set(this, "toggle", 'PERSON');
                    Vue.set(this,"actualRoles", this.roles[this.toggle]);
                    Vue.set(this,"creatorId", -1);
                }
            },
            beforeUpdate: function(e) {
                var creator = this.resourcecreator.creator;
                if (this.resourcecreator.init == undefined) {
                    return;
                } 
                console.log("updated", creator);
                this.resourcecreator.init = undefined;
                if (creator.firstName == undefined) {
                    this.toggle = 'INSTITUTION';
                    Vue.set(this, "toggle", 'INSTITUTION');
                    if (this.$refs.inputinstitution != undefined) {
                        this.$refs.inputinstitution.setValue(creator.properName);
                        this.$refs.inputinstitution.setId(creator.id);
                    }
                } else {
                    this.toggle = 'PERSON';
                    if (creator.institution == undefined || creator.institution == null) {
                        creator.institution = {};
                    }
                    Vue.set(this, "toggle", 'PERSON');
                    if (this.$refs.inputperson != undefined) {
                        this.$refs.inputperson.setValue(creator.properName);
                        this.$refs.inputperson.setId(creator.id);
                    }
                }
                Vue.set(this,"creatorId", creator.id);
                Vue.set(this,"actualRoles", this.roles[this.toggle]);
            },
            watch: {
                toggle: function(n, a) {
                    Vue.set(this,"actualRoles", this.roles[n]);
                    Vue.set(this,"creatorId", -1);
                    console.log(n, this.roles[n]);
                }
            },
            computed: {
                personClass: function() {
                    var ret = "btn btn-small personButton";
                    if (this.toggle == 'PERSON') {
                        ret = ret+ " btn-active active"
                    };
                    return ret;
                },
                institutionClass: function() {
                    var ret = "btn btn-small institutionButton";
                    if (this.toggle == 'INSTITUTION') {
                        ret = ret+ " btn-active active"
                    };
                    return ret;
                },
                editClass: function() {
                    if (this.resourcecreator.creator != undefined && this.resourcecreator.creator.id != undefined) {
                        return "";
                    }
                    return "disabled";
                },
                deleteClass: function() {
                    if (this.resourcecreator.creator != undefined) {
                        return "";
                    }
                    return "disabled";
                }
            },
            methods: {
                toggleValue: function(key) {
                    this.reset(key);
                    Vue.set(this,"toggle",key);
                },
                updatecreator: function(result) {
                    Vue.set(this.resourcecreator,"creator", result);
                    this.resourcecreator.creator = result;
                    console.log("setting resourcecreator to", this.resourcecreator.creator);
                    console.log(this.resourcecreator.creator.id)
                    Vue.set(this,"creatorId", this.resourcecreator.creator.id);
                },
                getPrefix: function(part) {
                    var ret = this.prefix;
                    if (this.row != undefined) {
                        ret = ret + "[" + this.row + "]";
                    }
                    ret = ret + "." + this.toggle.toLowerCase() + "." + part;
                    return ret;
                },
                getRootPrefix: function(part) {
                    var ret = this.prefix;
                    if (this.row != undefined) {
                        ret = ret + "[" + this.row + "]";
                    }
                    ret = ret + "." + part;
                    return ret;
                },
                deleteRow: function() {
                    if (this.row == 0) {
                        this.reset();
                        this.$forceUpdate()
                    } else {
                        this.$emit("deleterow",this.prefix, this.row);
                    }
                },
                reset: function(type) {
                    if (type == undefined) {
                        type = this.toggle;
                    }
                    if (type == 'PERSON') {
                        Vue.set(this.resourcecreator,"creator", { institution:{ name: undefined}});
                    } else {
                        Vue.set(this.resourcecreator,"creator", {});
                    }
                    Vue.set(this,"showEditPerson", false);
                    Vue.set(this,"showEditInstitution", false);
                    if (this.$refs.inputperson != undefined) {
                        this.$refs.inputperson.clear();
                    }
                    if (this.$refs.inputinstitution != undefined) {
                        this.$refs.inputinstitution.clear();
                    }
                    Vue.set(this,"creatorId", -1);
                },
                addAutocompleteValue: function(result) {
                    if (result != undefined && result.id != undefined) {
                        Vue.set(this.resourcecreator,"creator", result);
                        Vue.set(this,"creatorId", result.id);
                    }
                },
                clickEdit: function() {
                    Vue.set(this,"creatorId", -1);
                    if (this.toggle == 'PERSON') {
                        Vue.set(this, "showEditPerson",true);
                    } 
                    if (this.toggle == 'INSTITUTION') {
                        Vue.set(this, "showEditInstitution",true);
                    } 
                },
                clickNew: function() {
                    this.reset();
                    this.clickEdit();
                }
            }
        });
        
        

        var creatorlookup = Vue.component('person', {
            name: "person",
            template: "#person-template",
            props: {
                creator : {
                    type: Object,
                    default:  function() {return { institution:{ name: undefined}}} 
                },
                row: {
                    type:Number,
                    default: 0
                },
                roles: {
                    type: Array,
                    default: function() { return []; }
                },
                prefix: {
                    type: String,
                    default: "authorshipProxies"
                }
            },
            data: function() {
                return {
                showEditPerson: false,
                }
            },
            beforeMount: function() {
                if (this.creator.institution == undefined) {
                    this.creator.institution  ={};
                }
            },
            computed: {
                deleteClass: function() {
                    if (this.creator != undefined) {
                        return "";
                    }
                    return "disabled";
                }
            },
            methods: {
                getPrefix: function(part) {
                    var ret = this.prefix;
                    if (this.row != undefined) {
                        ret = ret + "[" + this.row + "]";
                    }
                    ret = ret + ".person." + part;
                    return ret;
                },
                getRootPrefix: function(part) {
                    var ret = this.prefix;
                    if (this.row != undefined) {
                        ret = ret + "[" + this.row + "]";
                    }
                    ret = ret + "." + part;
                    return ret;
                },
                addAutocompleteValue: function(result) {
                    if (result != undefined && result.id != undefined) {
//                        Vue.set(this,"creator", result);
                        this.$emit("autocompleteset", result);
                    }
                },
                clickEdit: function() {
                        Vue.set(this, "showEditPerson",true);
                },
                clickNew: function() {
                    this.clear();
                    this.clickEdit();
                },
                clear: function() {
                    if (this.$refs.input != undefined) {
                        this.$refs.input.clear();
                        this.$emit("autocompleteset",  { institution:{ name: undefined}});
                    }
                },
                setValue: function(val) {
                    this.$refs.input.setValue(val);
                },
                setId: function(val) {
                    this.$refs.input.setId(val);
                }
            }
        });


        var creatorlookup = Vue.component('institution', {
            name: "institution",
            template: "#institution-template",
            props: {
                creator : {
                    type: Object,
                    default:  function() {return { }} 
                },
                row: {
                    type:Number,
                    default: 0
                },
                roles: {
                    type: Array,
                    default: function() { return []; }
                },
                prefix: {
                    type: String,
                    default: "authorshipProxies"
                }
            },
            data: function() {
                return {
                    showEditInstitution: false,
                }
            },
            beforeMount: function() {
//                console.log('beforeMount:',this.creator);
            },
            computed: {
                deleteClass: function() {
                    if (this.creator != undefined) {
                        return "";
                    }
                    return "disabled";
                }
            },
            methods: {
                getPrefix: function(part) {
                    var ret = this.prefix;
                    if (this.row != undefined) {
                        ret = ret + "[" + this.row + "]";
                    }
                    ret = ret + ".institution." + part;
                    return ret;
                },
                getRootPrefix: function(part) {
                    var ret = this.prefix;
                    if (this.row != undefined) {
                        ret = ret + "[" + this.row + "]";
                    }
                    ret = ret + "." + part;
                    return ret;
                },
                addAutocompleteValue: function(result) {
                    if (result != undefined && result.id != undefined) {
//                        Vue.set(this,"creator", result);
                        this.$emit("autocompleteset", result);
                    }
                },
                clickEdit: function() {
                        Vue.set(this, "showEditInstitution",true);
                },
                clickNew: function() {
                    this.clear();
                    this.clickEdit();
                },
                clear: function() {
                    if (this.$refs.input != undefined) {
                        this.$refs.input.clear();
                        this.$emit("autocompleteset", {});
                    }
                },
                setValue: function(val) {
                    this.$refs.input.setValue(val);
                },
                setId: function(val) {
                    this.$refs.input.setId(val);
                }
            }
        });

    }
    
    
    
})(console, window, Vue, axios);
