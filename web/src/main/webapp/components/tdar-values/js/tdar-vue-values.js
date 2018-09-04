var _value = Vue.component('value', {
    template : "#value-template",
    props: {
        values: {
            type: Array,
            required: false,
            default: function() {return [];}
        },
        labelField: {
            type:String,
            default:  'label'
        },
        fieldName: {
            type:String,
            default:  'cb'
        },
        valueField: {
            type:String,
            default:  'id'
        },
        numcols: {
            type: Number,
            default: 2
        },
        type:{
            type:String,
            default:  'checkbox'
        },
        choices: {
            type: Array,
            required: true,
            default: function() {return new Array();}
        },
        idOnly : {
            type:Boolean,
            default:false
        }
    },
    data: function() {
        return {
            internalvalues: []
        }
    },
    watch: {
        internalvalues: function(n, o) {
            this.values.length =0;
            var self = this;
            if (self.idOnly) {
                self.values.concat(n);
            } else {
                self.choices.forEach(function(c) {
                    n.forEach(function(v){
                        if (c[self.valueField] == v) {
                            self.values.push(JSON.parse(JSON.stringify(c)));
                        }
                    });
                    
                });
            }
        }
    },
    methods: {
        subarray: function(start) {
        var num = Math.ceil(this.choices.length / this.numcols);
        var init = start * num;
        var ret =  this.choices.slice(init , init + num);
        return ret;
        },
        collist: function() {
            var ret = [];
            for (var i=0; i < this.numcols; i++) {
                ret.push(i);
            }
            return ret;
        },
        enable: function() {
            
        },
        disable: function() {
            
        }
    }
});