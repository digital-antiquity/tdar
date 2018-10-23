<template id="autocomplete">
  <div class="autocomplete" ref="autocompleteroot" @mouseover="addFocus('mouse')" @mouseout="removeFocus('mouse')">
    <input type="hidden" v-model="id"  :name="idname" v-if="idname != undefined" />
    <input type="text" @input="onChange" v-model="search" @keyup.down="onArrowDown" @keyup.up="onArrowUp" v-on:keyup.enter.self.stop="onEnter"
		@keydown.delete="deleteKey" @keyup.enter="enterKey" @keyup="anyKey" autocomplete="off" :disabled="disabled" 
		 ref="searchfield" :class="span" :name="fieldname" @focus="addFocus('cursor')" @blur="removeFocus('cursor')"/>
    <ul id="autocomplete-results" v-show="isOpen" class="autocomplete-results"  :style="getStyleTop()" ref="autoresults">
      <li v-if="!isLoading" v-for="(result, i) in results" :key="i" @click="setResult(result)" class="autocomplete-result" :class="{ 'is-active': i === arrowCounter }" :style="getStyleWidth()">
          <span v-html="render(result)" v-if="isCustomRender()"></span>
          <span v-if="!isCustomRender()">{{ getDisplay(result) }}  ({{ result.id}})</span>
      </li>
      <li class="status text-center center" :style="getStyleWidth()">
        <span v-if="isLoading">Loading results...</span>
        <span style='display:block' v-if="!isLoading" @click="createNew" class="createnew">{{createnewtext}}</span> 
        <span v-if="!isLoading"> Showing 1-{{recordsPerPage}} of {{totalRecords}} </span>
      </li>
    </ul>

  </div>
</template>

<script>
    var autocomplete = Vue.component('autocomplete', {
        name: "autocomplete",
        template: "#autocomplete",
        props: {
          items: {
            type: Array,
            required: false,
            default: function() {return new Array();}
          },
          isAsync: {
            type: Boolean,
            required: false,
            default: true
          },
          url: {
              type: String
          },
          suffix: {type: String},
          field: {
              type: String,
              required: true
          },
          render: {
              type: Object
          },
          fieldname: {
              type: String
          },
          allowCreate: {
              type:Boolean,
              default: true
          },
          resultsuffix: {
              type: String
          },
          span:{ type:String},
          idname: {type:String},
          name: {type:String},
          disabled: {type:Boolean},
          deletekey: {type: Function},
          enterkey: {type: Function},
          anykey: {type: Function},
          customcreatenew: {type:Function, default: function() {
              this.$emit("autocompletevalueset", this.search);              
          }},
          createnewtext: {type:String, default:'Create New'},
          initial_id: {type:Number},
          initial_value:{type:String}
        },
    
        data: function() {
          return {
            isOpen: false,
            results: [],
            search: "",
            id:- 1 ,
            searchObj: {},
            hasFocus: false,
            mouseFocus: false,
            cursorFocus: false,
            isLoading: false,
            width: 100,
            top:'10',
            arrowCounter: -1,
            totalRecords:0,
            recordsPerPage: 25,
            cancelToken: undefined
          }
        },
        
        methods: {
            createNew: function() {
                this.customcreatenew();
                this.isOpen = false;
                // this.reset();
            },
            getValue: function() {
                return this.search;
            },
            setValue: function(val) {
                Vue.set(this,"search", val);
            },
            addFocus: function (type) {
                if (type == 'mouse') {
                    this.mouseFocus = true;
                }
                if (type == 'cursor') {
                    this.cursorFocus = true;
                }
            },
            deleteKey: function() {
                if (this.deletekey != undefined) {
                    this.deletekey(this.$refs.searchfield);
                }
            },
            enterKey: function() {
                if (this.enterkey != undefined && this.arrowCounter < 0) {
                    this.enterkey(this.search);
                }
            },
            anyKey: function() {
                if (this.anykey != undefined) {
                    this.anykey(this.$refs.searchfield);
                }
            },
            removeFocus: function (type) {
                if (type == 'mouse') {
                    this.mouseFocus = false;
                }
                if (type == 'cursor') {
                    this.cursorFocus = false;
                }
                
                if (this.mouseFocus == false && this.cursorFocus == false) {
                    
                    if (this.allowCreate == false && this.searchObj == undefined) {
                        console.log("clearing ...", this.search, this.searchObj);
                        this.reset();
                    }
                }
            },
            reset: function() {
                this.clear();
                this.isOpen = false;
                this.results = [];
            },
          getStyleWidth: function() {
            return "width: " + (this.width - 8)+ "px;";
          }, 
          getStyleTop: function() {
              return "top:" + (this.top) + "px; ";
          }, 
          fieldName: function() {
              return this.name;
          },
          isCustomRender: function() {
            if (this.render != undefined && typeof this.render  === 'function') {
              return true;
            }
            return false;
          },
          getDisplay: function(obj) {
          if (obj == undefined) { return ''; }
            if (this.valueprop != undefined) {
              return obj['valueprop'];
            }
            var ret =  "";
              if (obj.name != undefined) {
                  ret =  obj.name;
              } else if (obj.title != undefined) {
                  ret = obj.title;
              } else if (obj.label != undefined) {
                  ret = obj.label;
              } else if (obj.properName != undefined) {
                  ret = obj.properName;
              };
              return ret;
                  
          },
          onChange: function() { // Let's warn the parent that a change was made
            this.$emit("input", this.search);
            Vue.set(this, 'width',this.$refs['searchfield'].offsetWidth);
            Vue.set(this, 'top',this.$refs['searchfield'].offsetHeight + this.$refs['searchfield'].offsetTop );

            // Is the data given by an outside ajax request?
            if (this.isAsync) {
              this._setResult();
              var self = this;
             if (this.search != undefined && this.search.length > 0) {
              this.isLoading = true;
              if (this.cancelToken != undefined) {
                  this.cancelToken.cancel();
              }
              
              Vue.set(this, "cancelToken" ,axios.CancelToken.source());
    
              var searchUrl = this.url + "?" + this.field + "=" + this.search + "&" + this.suffix;
              Vue.set(self, "totalRecords", 0);
              Vue.set(self, "recordsPerPage", 25);
              axios.get(searchUrl, { cancelToken: self.cancelToken.token }).then(function(res) {
                  Vue.set(self, "isLoading",false);
                  Vue.set(self, 'results',res.data[self.resultsuffix]);
                  console.log(res);
                  Vue.set(self, "totalRecords", res.data.status.totalRecords);
                  if (res.data.status.totalRecords < res.data.status.recordsPerPage) {
                      Vue.set(self, "recordsPerPage", self.totalRecords);
                  } else {
                      Vue.set(self, "recordsPerPage", res.data.status.recordsPerPage);
                  }
              }).catch(function(thrown) {
                  if (!axios.isCancel(thrown)) {
                      console.error(thrown);
                  }
              });
              this.isOpen = true;
              } else {
              this.isOpen = false;
              Vue.set(self, "isLoading",false);
              Vue.set(self, 'results',[]);
              
              }
            } else {
              // Let's search our flat array
              this.filterResults();
              this.isOpen = true;
            }
          },
          filterResults: function() {
            // first uncapitalize all the things
            this.results = new Array();
            var self = this;
            this.items.foreach(function(item){
              if (item.toLowerCase().indexOf(self.search.toLowerCase()) > -1) {
                  self.results.add(item);
              }
            });
          },
          focus: function() {
              this.$refs.searchfield.focus();
          },
          _setResult: function(result) {
            this.searchObj = result;
            this.$emit("autocompletevalueset", result);
            if (result != undefined && result.id != undefined) {
                this.$emit("setvalueid", result.id);
                this.id = result.id;
            } else {
                this.$emit("setvalueid", '');
                this.id = '';
            }
          },
          clear: function() {
              this.search= '';
              this._setResult();
          },
          setResult: function(result) {
            this.search = this.getDisplay(result);
            this._setResult(result);
            console.log(this.search, result);
            this.isOpen = false;
          },
          onArrowDown: function(evt) {
            if (this.arrowCounter < this.results.length) {
              this.arrowCounter = this.arrowCounter + 1;
            }
          },
          onArrowUp: function() {
            if (this.arrowCounter > 0) {
              this.arrowCounter = this.arrowCounter - 1;
            }
          },
          onEnter: function(e) {
            // make sure you can clear the value with setting
            if (this.arrowCounter > -1 || (this.search == '' || this.search == undefined)) {
              this.setResult( this.results[this.arrowCounter]);
            }
            this.isOpen = false;
            this.arrowCounter = -1;
          },
          setId: function(id) {
              console.log('setid',id);
          },
          handleClickOutside: function(evt) {
            if (!this.$el.contains(evt.target)) {
              this.isOpen = false;
              this.arrowCounter = -1;
            }
          }
        },
        watch: {
          items: function(val, oldValue) {
            // actually compare them
            if (val.length !== oldValue.length) {
              this.results = val;
              this.isLoading = false;
            }
          }
        },
        mounted: function() {
          Vue.set(this,"search", this.initial_value);
          Vue.set(this,"id", this.initial_id);
          document.addEventListener("click", this.handleClickOutside);
        },
        destroyed: function() {
          document.removeEventListener("click", this.handleClickOutside);
        }
    });
export { autocomplete}
</script>
<style>
.autocomplete {
    position: relative;
    width: 130px;
}

.autocomplete-results {
    padding: 0;
    margin: 0;
    z-index:100000;
    border: 1px solid #eeeeee;
    line-height: initial;
    weight: normal;
    font-weight: normal;
    overflow: auto;
    position: absolute;
    background: white; 
}

.autocomplete-result {
    list-style: none;
    text-align: left;
    padding: 4px 2px;
    cursor: pointer;
    line-height: initial;
    display: inline-block;
    width: intrinsic;
}

.autocomplete-result.is-active, .autocomplete-result:hover , .createnew:hover {
    background-color: #4aae9b;
    color: white;
}

.autocomplete .status {
    font-variant: small-caps;
    font-weight:700;
    font-size:90%;
}

input[autocomplete="off"]::-webkit-autofill,
input[autocomplete="off"]::-webkit-contacts-auto-fill-button,
input[autocomplete="off"]::-webkit-credentials-auto-fill-button {
visibility: hidden;
display: none !important;
pointer-events: none;
height: 0;
width: 0;
margin: 0;
}
</style>