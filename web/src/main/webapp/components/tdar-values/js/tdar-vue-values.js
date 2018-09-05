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
            internalvalues: [],
            watchLocked: false,
            disabled: false
        }
    },
    watch: {
        internalvalues: function(n, o) {
            if (this.watchLocked) {
                return;
            }
            this._setValues(n);
        }
    },
    mounted: function() {
        this.refresh();
    },
    computed: {
        disabledClass: function() {
            if (this.disabled) {
                return " disabled ";
            }
            return;
        }
    },
    methods: {
        refresh: function() {
            this.watchLocked = true;
            this.internalvalues = this._toIdList(this.values);
            this.watchLocked = false;
        },
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
        _toIdList: function(list) {
            var ret = [];
            var self = this;
            list.forEach(function(v){
                ret.push(self._toId(v));
            });
            console.log(ret);
            return ret;
        },
        _toId: function(v) {
            if (typeof v == "object") {
                return v[this.valueField];
            }  
            return v;
        },
        setValues: function(n) {
            var vals = [];
            var self = this;
            n.forEach(function(v){
                vals.push(self._toId(v));
            });
            this.watchLocked = true;
            Vue.set(this, "internalvalues",vals);
            this.watchLocked = false;
            this._setValues(vals);
        },
        _setValues: function(n) {
            this.values.length = 0;
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
        },
        enable: function() {
            this.disabled = false;
        },
        disable: function() {
            this.disabled = true;
        }
    }
});