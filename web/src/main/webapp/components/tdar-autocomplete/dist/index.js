(function webpackUniversalModuleDefinition(root, factory) {
	if(typeof exports === 'object' && typeof module === 'object')
		module.exports = factory();
	else if(typeof define === 'function' && define.amd)
		define([], factory);
	else if(typeof exports === 'object')
		exports["tdar-autocomplete"] = factory();
	else
		root["tdar-autocomplete"] = factory();
})(typeof self !== 'undefined' ? self : this, function() {
return /******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId]) {
/******/ 			return installedModules[moduleId].exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, {
/******/ 				configurable: false,
/******/ 				enumerable: true,
/******/ 				get: getter
/******/ 			});
/******/ 		}
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = 1);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ (function(module, exports, __webpack_require__) {

var __WEBPACK_AMD_DEFINE_FACTORY__, __WEBPACK_AMD_DEFINE_ARRAY__, __WEBPACK_AMD_DEFINE_RESULT__;(function (global, factory) {
    if (true) {
        !(__WEBPACK_AMD_DEFINE_ARRAY__ = [exports], __WEBPACK_AMD_DEFINE_FACTORY__ = (factory),
				__WEBPACK_AMD_DEFINE_RESULT__ = (typeof __WEBPACK_AMD_DEFINE_FACTORY__ === 'function' ?
				(__WEBPACK_AMD_DEFINE_FACTORY__.apply(exports, __WEBPACK_AMD_DEFINE_ARRAY__)) : __WEBPACK_AMD_DEFINE_FACTORY__),
				__WEBPACK_AMD_DEFINE_RESULT__ !== undefined && (module.exports = __WEBPACK_AMD_DEFINE_RESULT__));
    } else if (typeof exports !== "undefined") {
        factory(exports);
    } else {
        var mod = {
            exports: {}
        };
        factory(mod.exports);
        global.Autocomplete = mod.exports;
    }
})(this, function (exports) {
    "use strict";

    Object.defineProperty(exports, "__esModule", {
        value: true
    });
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //

    var autocomplete = Vue.component('autocomplete', {
        name: "autocomplete",
        template: "#autocomplete",
        props: {
            items: {
                type: Array,
                required: false,
                default: function _default() {
                    return new Array();
                }
            },
            isAsync: {
                type: Boolean,
                required: false,
                default: true
            },
            url: {
                type: String
            },
            suffix: { type: String },
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
                type: Boolean,
                default: true
            },
            resultsuffix: {
                type: String
            },
            span: { type: String },
            idname: { type: String },
            name: { type: String },
            disabled: { type: Boolean },
            deletekey: { type: Function },
            enterkey: { type: Function },
            anykey: { type: Function },
            customcreatenew: { type: Function, default: function _default() {
                    this.$emit("autocompletevalueset", this.search);
                } },
            createnewtext: { type: String, default: 'Create New' },
            initial_id: { type: Number },
            initial_value: { type: String }
        },

        data: function data() {
            return {
                isOpen: false,
                results: [],
                search: "",
                id: -1,
                searchObj: {},
                hasFocus: false,
                mouseFocus: false,
                cursorFocus: false,
                isLoading: false,
                width: 100,
                top: '10',
                arrowCounter: -1,
                totalRecords: 0,
                recordsPerPage: 25,
                cancelToken: undefined
            };
        },

        methods: {
            createNew: function createNew() {
                this.customcreatenew();
                this.isOpen = false;
                // this.reset();
            },
            getValue: function getValue() {
                return this.search;
            },
            setValue: function setValue(val) {
                Vue.set(this, "search", val);
            },
            addFocus: function addFocus(type) {
                if (type == 'mouse') {
                    this.mouseFocus = true;
                }
                if (type == 'cursor') {
                    this.cursorFocus = true;
                }
            },
            deleteKey: function deleteKey() {
                if (this.deletekey != undefined) {
                    this.deletekey(this.$refs.searchfield);
                }
            },
            enterKey: function enterKey() {
                if (this.enterkey != undefined && this.arrowCounter < 0) {
                    this.enterkey(this.search);
                }
            },
            anyKey: function anyKey() {
                if (this.anykey != undefined) {
                    this.anykey(this.$refs.searchfield);
                }
            },
            removeFocus: function removeFocus(type) {
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
            reset: function reset() {
                this.clear();
                this.isOpen = false;
                this.results = [];
            },
            getStyleWidth: function getStyleWidth() {
                return "width: " + (this.width - 8) + "px;";
            },
            getStyleTop: function getStyleTop() {
                return "top:" + this.top + "px; ";
            },
            fieldName: function fieldName() {
                return this.name;
            },
            isCustomRender: function isCustomRender() {
                if (this.render != undefined && typeof this.render === 'function') {
                    return true;
                }
                return false;
            },
            getDisplay: function getDisplay(obj) {
                if (obj == undefined) {
                    return '';
                }
                if (this.valueprop != undefined) {
                    return obj['valueprop'];
                }
                var ret = "";
                if (obj.name != undefined) {
                    ret = obj.name;
                } else if (obj.title != undefined) {
                    ret = obj.title;
                } else if (obj.label != undefined) {
                    ret = obj.label;
                } else if (obj.properName != undefined) {
                    ret = obj.properName;
                };
                return ret;
            },
            onChange: function onChange() {
                // Let's warn the parent that a change was made
                this.$emit("input", this.search);
                Vue.set(this, 'width', this.$refs['searchfield'].offsetWidth);
                Vue.set(this, 'top', this.$refs['searchfield'].offsetHeight + this.$refs['searchfield'].offsetTop);

                // Is the data given by an outside ajax request?
                if (this.isAsync) {
                    this._setResult();
                    var self = this;
                    if (this.search != undefined && this.search.length > 0) {
                        this.isLoading = true;
                        if (this.cancelToken != undefined) {
                            this.cancelToken.cancel();
                        }

                        Vue.set(this, "cancelToken", axios.CancelToken.source());

                        var searchUrl = this.url + "?" + this.field + "=" + this.search + "&" + this.suffix;
                        Vue.set(self, "totalRecords", 0);
                        Vue.set(self, "recordsPerPage", 25);
                        axios.get(searchUrl, { cancelToken: self.cancelToken.token }).then(function (res) {
                            Vue.set(self, "isLoading", false);
                            Vue.set(self, 'results', res.data[self.resultsuffix]);
                            console.log(res);
                            Vue.set(self, "totalRecords", res.data.status.totalRecords);
                            if (res.data.status.totalRecords < res.data.status.recordsPerPage) {
                                Vue.set(self, "recordsPerPage", self.totalRecords);
                            } else {
                                Vue.set(self, "recordsPerPage", res.data.status.recordsPerPage);
                            }
                        }).catch(function (thrown) {
                            if (!axios.isCancel(thrown)) {
                                console.error(thrown);
                            }
                        });
                        this.isOpen = true;
                    } else {
                        this.isOpen = false;
                        Vue.set(self, "isLoading", false);
                        Vue.set(self, 'results', []);
                    }
                } else {
                    // Let's search our flat array
                    this.filterResults();
                    this.isOpen = true;
                }
            },
            filterResults: function filterResults() {
                // first uncapitalize all the things
                this.results = new Array();
                var self = this;
                this.items.foreach(function (item) {
                    if (item.toLowerCase().indexOf(self.search.toLowerCase()) > -1) {
                        self.results.add(item);
                    }
                });
            },
            focus: function focus() {
                this.$refs.searchfield.focus();
            },
            _setResult: function _setResult(result) {
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
            clear: function clear() {
                this.search = '';
                this._setResult();
            },
            setResult: function setResult(result) {
                this.search = this.getDisplay(result);
                this._setResult(result);
                console.log(this.search, result);
                this.isOpen = false;
            },
            onArrowDown: function onArrowDown(evt) {
                if (this.arrowCounter < this.results.length) {
                    this.arrowCounter = this.arrowCounter + 1;
                }
            },
            onArrowUp: function onArrowUp() {
                if (this.arrowCounter > 0) {
                    this.arrowCounter = this.arrowCounter - 1;
                }
            },
            onEnter: function onEnter(e) {
                // make sure you can clear the value with setting
                if (this.arrowCounter > -1 || this.search == '' || this.search == undefined) {
                    this.setResult(this.results[this.arrowCounter]);
                }
                this.isOpen = false;
                this.arrowCounter = -1;
            },
            setId: function setId(id) {
                console.log('setid', id);
            },
            handleClickOutside: function handleClickOutside(evt) {
                if (!this.$el.contains(evt.target)) {
                    this.isOpen = false;
                    this.arrowCounter = -1;
                }
            }
        },
        watch: {
            items: function items(val, oldValue) {
                // actually compare them
                if (val.length !== oldValue.length) {
                    this.results = val;
                    this.isLoading = false;
                }
            }
        },
        mounted: function mounted() {
            Vue.set(this, "search", this.initial_value);
            Vue.set(this, "id", this.initial_id);
            document.addEventListener("click", this.handleClickOutside);
        },
        destroyed: function destroyed() {
            document.removeEventListener("click", this.handleClickOutside);
        }
    });
    exports.autocomplete = autocomplete;
});

/***/ }),
/* 1 */
/***/ (function(module, exports, __webpack_require__) {

var __WEBPACK_AMD_DEFINE_FACTORY__, __WEBPACK_AMD_DEFINE_ARRAY__, __WEBPACK_AMD_DEFINE_RESULT__;(function (global, factory) {
    if (true) {
        !(__WEBPACK_AMD_DEFINE_ARRAY__ = [exports, __webpack_require__(2)], __WEBPACK_AMD_DEFINE_FACTORY__ = (factory),
				__WEBPACK_AMD_DEFINE_RESULT__ = (typeof __WEBPACK_AMD_DEFINE_FACTORY__ === 'function' ?
				(__WEBPACK_AMD_DEFINE_FACTORY__.apply(exports, __WEBPACK_AMD_DEFINE_ARRAY__)) : __WEBPACK_AMD_DEFINE_FACTORY__),
				__WEBPACK_AMD_DEFINE_RESULT__ !== undefined && (module.exports = __WEBPACK_AMD_DEFINE_RESULT__));
    } else if (typeof exports !== "undefined") {
        factory(exports, require('./components/Autocomplete'));
    } else {
        var mod = {
            exports: {}
        };
        factory(mod.exports, global.Autocomplete);
        global.index = mod.exports;
    }
})(this, function (exports, _Autocomplete) {
    'use strict';

    Object.defineProperty(exports, "__esModule", {
        value: true
    });
    exports.Autocomplete = undefined;

    var _Autocomplete2 = _interopRequireDefault(_Autocomplete);

    function _interopRequireDefault(obj) {
        return obj && obj.__esModule ? obj : {
            default: obj
        };
    }

    exports.default = {
        install: function install(Vue) {
            Vue.component('autocomplete', _Autocomplete2.default);
        }
    };
    exports.Autocomplete = _Autocomplete2.default;
});

/***/ }),
/* 2 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
Object.defineProperty(__webpack_exports__, "__esModule", { value: true });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__babel_loader_node_modules_vue_loader_lib_selector_type_script_index_0_Autocomplete_vue__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__babel_loader_node_modules_vue_loader_lib_selector_type_script_index_0_Autocomplete_vue___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_0__babel_loader_node_modules_vue_loader_lib_selector_type_script_index_0_Autocomplete_vue__);
/* harmony namespace reexport (unknown) */ for(var __WEBPACK_IMPORT_KEY__ in __WEBPACK_IMPORTED_MODULE_0__babel_loader_node_modules_vue_loader_lib_selector_type_script_index_0_Autocomplete_vue__) if(__WEBPACK_IMPORT_KEY__ !== 'default') (function(key) { __webpack_require__.d(__webpack_exports__, key, function() { return __WEBPACK_IMPORTED_MODULE_0__babel_loader_node_modules_vue_loader_lib_selector_type_script_index_0_Autocomplete_vue__[key]; }) }(__WEBPACK_IMPORT_KEY__));
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__node_modules_vue_loader_lib_template_compiler_index_id_data_v_70fcc9bd_hasScoped_false_optionsId_0_buble_transforms_node_modules_vue_loader_lib_selector_type_template_index_0_Autocomplete_vue__ = __webpack_require__(8);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__node_modules_vue_loader_lib_runtime_component_normalizer__ = __webpack_require__(9);
function injectStyle (context) {
  __webpack_require__(3)
}
/* script */


/* template */

/* template functional */
var __vue_template_functional__ = false
/* styles */
var __vue_styles__ = injectStyle
/* scopeId */
var __vue_scopeId__ = null
/* moduleIdentifier (server only) */
var __vue_module_identifier__ = null

var Component = Object(__WEBPACK_IMPORTED_MODULE_2__node_modules_vue_loader_lib_runtime_component_normalizer__["a" /* default */])(
  __WEBPACK_IMPORTED_MODULE_0__babel_loader_node_modules_vue_loader_lib_selector_type_script_index_0_Autocomplete_vue___default.a,
  __WEBPACK_IMPORTED_MODULE_1__node_modules_vue_loader_lib_template_compiler_index_id_data_v_70fcc9bd_hasScoped_false_optionsId_0_buble_transforms_node_modules_vue_loader_lib_selector_type_template_index_0_Autocomplete_vue__["a" /* render */],
  __WEBPACK_IMPORTED_MODULE_1__node_modules_vue_loader_lib_template_compiler_index_id_data_v_70fcc9bd_hasScoped_false_optionsId_0_buble_transforms_node_modules_vue_loader_lib_selector_type_template_index_0_Autocomplete_vue__["b" /* staticRenderFns */],
  __vue_template_functional__,
  __vue_styles__,
  __vue_scopeId__,
  __vue_module_identifier__
)

/* harmony default export */ __webpack_exports__["default"] = (Component.exports);


/***/ }),
/* 3 */
/***/ (function(module, exports, __webpack_require__) {

// style-loader: Adds some css to the DOM by adding a <style> tag

// load the styles
var content = __webpack_require__(4);
if(typeof content === 'string') content = [[module.i, content, '']];
if(content.locals) module.exports = content.locals;
// add the styles to the DOM
var add = __webpack_require__(6).default
var update = add("c7187bb4", content, true, {});

/***/ }),
/* 4 */
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__(5)(false);
// imports


// module
exports.push([module.i, ".autocomplete{position:relative;width:130px}.autocomplete-results{padding:0;margin:0;z-index:100000;border:1px solid #eee;line-height:normal;weight:normal;font-weight:400;overflow:auto;position:absolute;background:#fff}.autocomplete-result{list-style:none;text-align:left;padding:4px 2px;cursor:pointer;line-height:normal;display:inline-block;width:intrinsic}.autocomplete-result.is-active,.autocomplete-result:hover,.createnew:hover{background-color:#4aae9b;color:#fff}.autocomplete .status{font-variant:small-caps;font-weight:700;font-size:90%}input[autocomplete=off]::-webkit-autofill,input[autocomplete=off]::-webkit-contacts-auto-fill-button,input[autocomplete=off]::-webkit-credentials-auto-fill-button{visibility:hidden;display:none!important;pointer-events:none;height:0;width:0;margin:0}", ""]);

// exports


/***/ }),
/* 5 */
/***/ (function(module, exports) {

/*
	MIT License http://www.opensource.org/licenses/mit-license.php
	Author Tobias Koppers @sokra
*/
// css base code, injected by the css-loader
module.exports = function(useSourceMap) {
	var list = [];

	// return the list of modules as css string
	list.toString = function toString() {
		return this.map(function (item) {
			var content = cssWithMappingToString(item, useSourceMap);
			if(item[2]) {
				return "@media " + item[2] + "{" + content + "}";
			} else {
				return content;
			}
		}).join("");
	};

	// import a list of modules into the list
	list.i = function(modules, mediaQuery) {
		if(typeof modules === "string")
			modules = [[null, modules, ""]];
		var alreadyImportedModules = {};
		for(var i = 0; i < this.length; i++) {
			var id = this[i][0];
			if(typeof id === "number")
				alreadyImportedModules[id] = true;
		}
		for(i = 0; i < modules.length; i++) {
			var item = modules[i];
			// skip already imported module
			// this implementation is not 100% perfect for weird media query combinations
			//  when a module is imported multiple times with different media queries.
			//  I hope this will never occur (Hey this way we have smaller bundles)
			if(typeof item[0] !== "number" || !alreadyImportedModules[item[0]]) {
				if(mediaQuery && !item[2]) {
					item[2] = mediaQuery;
				} else if(mediaQuery) {
					item[2] = "(" + item[2] + ") and (" + mediaQuery + ")";
				}
				list.push(item);
			}
		}
	};
	return list;
};

function cssWithMappingToString(item, useSourceMap) {
	var content = item[1] || '';
	var cssMapping = item[3];
	if (!cssMapping) {
		return content;
	}

	if (useSourceMap && typeof btoa === 'function') {
		var sourceMapping = toComment(cssMapping);
		var sourceURLs = cssMapping.sources.map(function (source) {
			return '/*# sourceURL=' + cssMapping.sourceRoot + source + ' */'
		});

		return [content].concat(sourceURLs).concat([sourceMapping]).join('\n');
	}

	return [content].join('\n');
}

// Adapted from convert-source-map (MIT)
function toComment(sourceMap) {
	// eslint-disable-next-line no-undef
	var base64 = btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap))));
	var data = 'sourceMappingURL=data:application/json;charset=utf-8;base64,' + base64;

	return '/*# ' + data + ' */';
}


/***/ }),
/* 6 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
Object.defineProperty(__webpack_exports__, "__esModule", { value: true });
/* harmony export (immutable) */ __webpack_exports__["default"] = addStylesClient;
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__listToStyles__ = __webpack_require__(7);
/*
  MIT License http://www.opensource.org/licenses/mit-license.php
  Author Tobias Koppers @sokra
  Modified by Evan You @yyx990803
*/



var hasDocument = typeof document !== 'undefined'

if (typeof DEBUG !== 'undefined' && DEBUG) {
  if (!hasDocument) {
    throw new Error(
    'vue-style-loader cannot be used in a non-browser environment. ' +
    "Use { target: 'node' } in your Webpack config to indicate a server-rendering environment."
  ) }
}

/*
type StyleObject = {
  id: number;
  parts: Array<StyleObjectPart>
}

type StyleObjectPart = {
  css: string;
  media: string;
  sourceMap: ?string
}
*/

var stylesInDom = {/*
  [id: number]: {
    id: number,
    refs: number,
    parts: Array<(obj?: StyleObjectPart) => void>
  }
*/}

var head = hasDocument && (document.head || document.getElementsByTagName('head')[0])
var singletonElement = null
var singletonCounter = 0
var isProduction = false
var noop = function () {}
var options = null
var ssrIdKey = 'data-vue-ssr-id'

// Force single-tag solution on IE6-9, which has a hard limit on the # of <style>
// tags it will allow on a page
var isOldIE = typeof navigator !== 'undefined' && /msie [6-9]\b/.test(navigator.userAgent.toLowerCase())

function addStylesClient (parentId, list, _isProduction, _options) {
  isProduction = _isProduction

  options = _options || {}

  var styles = Object(__WEBPACK_IMPORTED_MODULE_0__listToStyles__["a" /* default */])(parentId, list)
  addStylesToDom(styles)

  return function update (newList) {
    var mayRemove = []
    for (var i = 0; i < styles.length; i++) {
      var item = styles[i]
      var domStyle = stylesInDom[item.id]
      domStyle.refs--
      mayRemove.push(domStyle)
    }
    if (newList) {
      styles = Object(__WEBPACK_IMPORTED_MODULE_0__listToStyles__["a" /* default */])(parentId, newList)
      addStylesToDom(styles)
    } else {
      styles = []
    }
    for (var i = 0; i < mayRemove.length; i++) {
      var domStyle = mayRemove[i]
      if (domStyle.refs === 0) {
        for (var j = 0; j < domStyle.parts.length; j++) {
          domStyle.parts[j]()
        }
        delete stylesInDom[domStyle.id]
      }
    }
  }
}

function addStylesToDom (styles /* Array<StyleObject> */) {
  for (var i = 0; i < styles.length; i++) {
    var item = styles[i]
    var domStyle = stylesInDom[item.id]
    if (domStyle) {
      domStyle.refs++
      for (var j = 0; j < domStyle.parts.length; j++) {
        domStyle.parts[j](item.parts[j])
      }
      for (; j < item.parts.length; j++) {
        domStyle.parts.push(addStyle(item.parts[j]))
      }
      if (domStyle.parts.length > item.parts.length) {
        domStyle.parts.length = item.parts.length
      }
    } else {
      var parts = []
      for (var j = 0; j < item.parts.length; j++) {
        parts.push(addStyle(item.parts[j]))
      }
      stylesInDom[item.id] = { id: item.id, refs: 1, parts: parts }
    }
  }
}

function createStyleElement () {
  var styleElement = document.createElement('style')
  styleElement.type = 'text/css'
  head.appendChild(styleElement)
  return styleElement
}

function addStyle (obj /* StyleObjectPart */) {
  var update, remove
  var styleElement = document.querySelector('style[' + ssrIdKey + '~="' + obj.id + '"]')

  if (styleElement) {
    if (isProduction) {
      // has SSR styles and in production mode.
      // simply do nothing.
      return noop
    } else {
      // has SSR styles but in dev mode.
      // for some reason Chrome can't handle source map in server-rendered
      // style tags - source maps in <style> only works if the style tag is
      // created and inserted dynamically. So we remove the server rendered
      // styles and inject new ones.
      styleElement.parentNode.removeChild(styleElement)
    }
  }

  if (isOldIE) {
    // use singleton mode for IE9.
    var styleIndex = singletonCounter++
    styleElement = singletonElement || (singletonElement = createStyleElement())
    update = applyToSingletonTag.bind(null, styleElement, styleIndex, false)
    remove = applyToSingletonTag.bind(null, styleElement, styleIndex, true)
  } else {
    // use multi-style-tag mode in all other cases
    styleElement = createStyleElement()
    update = applyToTag.bind(null, styleElement)
    remove = function () {
      styleElement.parentNode.removeChild(styleElement)
    }
  }

  update(obj)

  return function updateStyle (newObj /* StyleObjectPart */) {
    if (newObj) {
      if (newObj.css === obj.css &&
          newObj.media === obj.media &&
          newObj.sourceMap === obj.sourceMap) {
        return
      }
      update(obj = newObj)
    } else {
      remove()
    }
  }
}

var replaceText = (function () {
  var textStore = []

  return function (index, replacement) {
    textStore[index] = replacement
    return textStore.filter(Boolean).join('\n')
  }
})()

function applyToSingletonTag (styleElement, index, remove, obj) {
  var css = remove ? '' : obj.css

  if (styleElement.styleSheet) {
    styleElement.styleSheet.cssText = replaceText(index, css)
  } else {
    var cssNode = document.createTextNode(css)
    var childNodes = styleElement.childNodes
    if (childNodes[index]) styleElement.removeChild(childNodes[index])
    if (childNodes.length) {
      styleElement.insertBefore(cssNode, childNodes[index])
    } else {
      styleElement.appendChild(cssNode)
    }
  }
}

function applyToTag (styleElement, obj) {
  var css = obj.css
  var media = obj.media
  var sourceMap = obj.sourceMap

  if (media) {
    styleElement.setAttribute('media', media)
  }
  if (options.ssrId) {
    styleElement.setAttribute(ssrIdKey, obj.id)
  }

  if (sourceMap) {
    // https://developer.chrome.com/devtools/docs/javascript-debugging
    // this makes source maps inside style tags work properly in Chrome
    css += '\n/*# sourceURL=' + sourceMap.sources[0] + ' */'
    // http://stackoverflow.com/a/26603875
    css += '\n/*# sourceMappingURL=data:application/json;base64,' + btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap)))) + ' */'
  }

  if (styleElement.styleSheet) {
    styleElement.styleSheet.cssText = css
  } else {
    while (styleElement.firstChild) {
      styleElement.removeChild(styleElement.firstChild)
    }
    styleElement.appendChild(document.createTextNode(css))
  }
}


/***/ }),
/* 7 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (immutable) */ __webpack_exports__["a"] = listToStyles;
/**
 * Translates the list format produced by css-loader into something
 * easier to manipulate.
 */
function listToStyles (parentId, list) {
  var styles = []
  var newStyles = {}
  for (var i = 0; i < list.length; i++) {
    var item = list[i]
    var id = item[0]
    var css = item[1]
    var media = item[2]
    var sourceMap = item[3]
    var part = {
      id: parentId + ':' + i,
      css: css,
      media: media,
      sourceMap: sourceMap
    }
    if (!newStyles[id]) {
      styles.push(newStyles[id] = { id: id, parts: [part] })
    } else {
      newStyles[id].parts.push(part)
    }
  }
  return styles
}


/***/ }),
/* 8 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return render; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "b", function() { return staticRenderFns; });
var render = function () {var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{ref:"autocompleteroot",staticClass:"autocomplete",on:{"mouseover":function($event){_vm.addFocus('mouse')},"mouseout":function($event){_vm.removeFocus('mouse')}}},[(_vm.idname != undefined)?_c('input',{directives:[{name:"model",rawName:"v-model",value:(_vm.id),expression:"id"}],attrs:{"type":"hidden","name":_vm.idname},domProps:{"value":(_vm.id)},on:{"input":function($event){if($event.target.composing){ return; }_vm.id=$event.target.value}}}):_vm._e(),_vm._v(" "),_c('input',{directives:[{name:"model",rawName:"v-model",value:(_vm.search),expression:"search"}],ref:"searchfield",class:_vm.span,attrs:{"type":"text","autocomplete":"off","disabled":_vm.disabled,"name":_vm.fieldname},domProps:{"value":(_vm.search)},on:{"input":[function($event){if($event.target.composing){ return; }_vm.search=$event.target.value},_vm.onChange],"keyup":[function($event){if(!('button' in $event)&&_vm._k($event.keyCode,"down",40,$event.key,["Down","ArrowDown"])){ return null; }return _vm.onArrowDown($event)},function($event){if(!('button' in $event)&&_vm._k($event.keyCode,"up",38,$event.key,["Up","ArrowUp"])){ return null; }return _vm.onArrowUp($event)},function($event){if(!('button' in $event)&&_vm._k($event.keyCode,"enter",13,$event.key,"Enter")){ return null; }if($event.target !== $event.currentTarget){ return null; }$event.stopPropagation();return _vm.onEnter($event)},function($event){if(!('button' in $event)&&_vm._k($event.keyCode,"enter",13,$event.key,"Enter")){ return null; }return _vm.enterKey($event)},_vm.anyKey],"keydown":function($event){if(!('button' in $event)&&_vm._k($event.keyCode,"delete",[8,46],$event.key,["Backspace","Delete"])){ return null; }return _vm.deleteKey($event)},"focus":function($event){_vm.addFocus('cursor')},"blur":function($event){_vm.removeFocus('cursor')}}}),_vm._v(" "),_c('ul',{directives:[{name:"show",rawName:"v-show",value:(_vm.isOpen),expression:"isOpen"}],ref:"autoresults",staticClass:"autocomplete-results",style:(_vm.getStyleTop()),attrs:{"id":"autocomplete-results"}},[_vm._l((_vm.results),function(result,i){return (!_vm.isLoading)?_c('li',{key:i,staticClass:"autocomplete-result",class:{ 'is-active': i === _vm.arrowCounter },style:(_vm.getStyleWidth()),on:{"click":function($event){_vm.setResult(result)}}},[(_vm.isCustomRender())?_c('span',{domProps:{"innerHTML":_vm._s(render(result))}}):_vm._e(),_vm._v(" "),(!_vm.isCustomRender())?_c('span',[_vm._v(_vm._s(_vm.getDisplay(result))+"  ("+_vm._s(result.id)+")")]):_vm._e()]):_vm._e()}),_vm._v(" "),_c('li',{staticClass:"status text-center center",style:(_vm.getStyleWidth())},[(_vm.isLoading)?_c('span',[_vm._v("Loading results...")]):_vm._e(),_vm._v(" "),(!_vm.isLoading)?_c('span',{staticClass:"createnew",staticStyle:{"display":"block"},on:{"click":_vm.createNew}},[_vm._v(_vm._s(_vm.createnewtext))]):_vm._e(),_vm._v(" "),(!_vm.isLoading)?_c('span',[_vm._v(" Showing 1-"+_vm._s(_vm.recordsPerPage)+" of "+_vm._s(_vm.totalRecords)+" ")]):_vm._e()])],2)])}
var staticRenderFns = []


/***/ }),
/* 9 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (immutable) */ __webpack_exports__["a"] = normalizeComponent;
/* globals __VUE_SSR_CONTEXT__ */

// IMPORTANT: Do NOT use ES2015 features in this file (except for modules).
// This module is a runtime utility for cleaner component module output and will
// be included in the final webpack user bundle.

function normalizeComponent (
  scriptExports,
  render,
  staticRenderFns,
  functionalTemplate,
  injectStyles,
  scopeId,
  moduleIdentifier, /* server only */
  shadowMode /* vue-cli only */
) {
  scriptExports = scriptExports || {}

  // ES6 modules interop
  var type = typeof scriptExports.default
  if (type === 'object' || type === 'function') {
    scriptExports = scriptExports.default
  }

  // Vue.extend constructor export interop
  var options = typeof scriptExports === 'function'
    ? scriptExports.options
    : scriptExports

  // render functions
  if (render) {
    options.render = render
    options.staticRenderFns = staticRenderFns
    options._compiled = true
  }

  // functional template
  if (functionalTemplate) {
    options.functional = true
  }

  // scopedId
  if (scopeId) {
    options._scopeId = scopeId
  }

  var hook
  if (moduleIdentifier) { // server build
    hook = function (context) {
      // 2.3 injection
      context =
        context || // cached call
        (this.$vnode && this.$vnode.ssrContext) || // stateful
        (this.parent && this.parent.$vnode && this.parent.$vnode.ssrContext) // functional
      // 2.2 with runInNewContext: true
      if (!context && typeof __VUE_SSR_CONTEXT__ !== 'undefined') {
        context = __VUE_SSR_CONTEXT__
      }
      // inject component styles
      if (injectStyles) {
        injectStyles.call(this, context)
      }
      // register component module identifier for async chunk inferrence
      if (context && context._registeredComponents) {
        context._registeredComponents.add(moduleIdentifier)
      }
    }
    // used by ssr in case component is cached and beforeCreate
    // never gets called
    options._ssrRegister = hook
  } else if (injectStyles) {
    hook = shadowMode
      ? function () { injectStyles.call(this, this.$root.$options.shadowRoot) }
      : injectStyles
  }

  if (hook) {
    if (options.functional) {
      // for template-only hot-reload because in that case the render fn doesn't
      // go through the normalizer
      options._injectStyles = hook
      // register for functioal component in vue file
      var originalRender = options.render
      options.render = function renderWithStyleInjection (h, context) {
        hook.call(context)
        return originalRender(h, context)
      }
    } else {
      // inject component registration as beforeCreate hook
      var existing = options.beforeCreate
      options.beforeCreate = existing
        ? [].concat(existing, hook)
        : [hook]
    }
  }

  return {
    exports: scriptExports,
    options: options
  }
}


/***/ })
/******/ ]);
});