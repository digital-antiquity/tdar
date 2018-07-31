TDAR.vuejs.tagging= (function(console, ctx, Vue, axios, TDAR) {
    "use strict";
    var app = new Vue({
        el : "#sel",
        data : {values: [ 
                {val: "science"} , 
                {val: "biology"} , 
                {val: "chemistry"},
                {val: "physics"} ],
                separator: ";" },
        mounted: function() {
        },
        methods: {
            remove: function(index) {
                 this.values.splice(index,1);
            },
            addEntry: function() {
                this._addEntry(this.$refs.input.value);
                this.$refs.input.value = "";
            },
            addAutocompleteValue: function(result) {
                if (result != undefined) {
                    console.log(result);
                    this._addEntry(result.title, result.id);
                    this.$refs.input.clear();
                }
            },
            _addEntry: function(entry, id) {
                seen = false;
                this.values.forEach(function(val) {
                    if (val.val == entry) {
                        seen = true;
                    }
                });
                if (!seen) {
                    this.values.push({val: entry.trim(), id: id});
                }
            },
            watchEntry: function() {
                var val = this.$refs.input.value;
                var sepPos = val.indexOf(this.separator);
                while (sepPos > -1) {
                    var part = val.substring(0, sepPos);
                    var quotes = part.split("\"");
                    console.log(part, quotes, (quotes.length -1 ) % 2);
                    if ((quotes.length -1 ) % 2 == 0) {
                        this._addEntry(part);
                        this.$refs.input.value = val.substring(sepPos + 1).trim();
                        sepPos = this.$refs.input.value.indexOf(this.separator );
                    } else {
                        sepPos = this.$refs.input.value.indexOf(this.separator, sepPos + 1 );
                    }
                }
            },
            deleteLast: function() {
                if (this.$refs.input.selectionStart == 0 && this.$refs.input.selectionEnd == 0) {
                    this.remove(this.values.length -1);
                }
            }
        }                   
    });
    
});