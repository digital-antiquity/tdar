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
            },
            data: function() {
                return {
                roles: ['AUTHOR','EDITOR'],
                showEditInstitution: false,
                showEditPerson: false,
                prefix: "authorishipProxies",
                toggle: "PERSON"
                }
            },
            created: function() {
                if (this.resourcecreator.creator.firstName == undefined) {
                    this.toggle = 'INSTITUTION';
                    Vue.set(this, "toggle", 'INSTITUTION');
                }
            },
            watch: {
                resourcecreator: function(n, o) {
//                    console.log("creatorchange:", n, o);
                }
            },
            computed: {
                creatorId: function() {
                    if (this.resourcecreator.creator == undefined || this.resourcecreator.creator.id == undefined || this.resourcecreator.creator.id == -1) {
                        return -1;
                    }
                    return this.resourcecreator.creator.id;  
                },
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
                    Vue.set(this,"toggle",key);
                    this.reset();
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
                reset: function() {
                    Vue.set(this.resourcecreator,"creator", { institution:{ name: undefined}});
                    Vue.set(this,"showEditPerson", false);
                    Vue.set(this,"showEditInstitution", false);
                    if (this.$refs.inputperson != undefined) {
                        this.$refs.inputperson.clear();
                    }
                    if (this.$refs.inputinstitution != undefined) {
                        this.$refs.inputinstitution.clear();
                    }
                },
                addAutocompleteValue: function(result) {
                    if (result != undefined && result.id != undefined) {
                        Vue.set(this.resourcecreator,"creator", result);
                    }
                },
                clickEdit: function() {
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
            },
            data: function() {
                return {
                roles: ['AUTHOR','EDITOR'],
                showEditPerson: false,
                prefix: "authorishipProxies",
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
                        Vue.set(this,"creator", result);
                    }
                },
                clickEdit: function() {
                        Vue.set(this, "showEditPerson",true);
                },
                clickNew: function() {
                    this.reset();
                    this.clickEdit();
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
            },
            data: function() {
                return {
                roles: ['AUTHOR','EDITOR'],
                showEditInstitution: false,
                prefix: "authorishipProxies",
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
                        Vue.set(this,"creator", result);
                    }
                },
                clickEdit: function() {
                        Vue.set(this, "showEditInstitution",true);
                },
                clickNew: function() {
                    this.reset();
                    this.clickEdit();
                }
            }
        });

    }
    
    
    
})(console, window, Vue, axios);
