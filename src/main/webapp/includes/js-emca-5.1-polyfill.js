//https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/Reduce
// Production steps of ECMA-262, Edition 5, 15.4.4.21
// Reference: http://es5.github.io/#x15.4.4.21
if (!Array.prototype.reduce) {
  Array.prototype.reduce = function(callback /*, initialValue*/) {
    'use strict';
    if (this == null) {
      throw new TypeError('Array.prototype.reduce called on null or undefined');
    }
    if (typeof callback !== 'function') {
      throw new TypeError(callback + ' is not a function');
    }
    var t = Object(this), len = t.length >>> 0, k = 0, value;
    if (arguments.length == 2) {
      value = arguments[1];
    } else {
      while (k < len && ! k in t) {
        k++; 
      }
      if (k >= len) {
        throw new TypeError('Reduce of empty array with no initial value');
      }
      value = t[k++];
    }
    for (; k < len; k++) {
      if (k in t) {
        value = callback(value, t[k], k, t);
      }
    }
    return value;
  };
}

//https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/map

//Production steps of ECMA-262, Edition 5, 15.4.4.19
//Reference: http://es5.github.io/#x15.4.4.19
if (!Array.prototype.map) {

Array.prototype.map = function(callback, thisArg) {

 var T, A, k;

 if (this == null) {
   throw new TypeError(' this is null or not defined');
 }

 // 1. Let O be the result of calling ToObject passing the |this| 
 //    value as the argument.
 var O = Object(this);

 // 2. Let lenValue be the result of calling the Get internal 
 //    method of O with the argument "length".
 // 3. Let len be ToUint32(lenValue).
 var len = O.length >>> 0;

 // 4. If IsCallable(callback) is false, throw a TypeError exception.
 // See: http://es5.github.com/#x9.11
 if (typeof callback !== 'function') {
   throw new TypeError(callback + ' is not a function');
 }

 // 5. If thisArg was supplied, let T be thisArg; else let T be undefined.
 if (arguments.length > 1) {
   T = thisArg;
 }

 // 6. Let A be a new array created as if by the expression new Array(len) 
 //    where Array is the standard built-in constructor with that name and 
 //    len is the value of len.
 A = new Array(len);

 // 7. Let k be 0
 k = 0;

 // 8. Repeat, while k < len
 while (k < len) {

   var kValue, mappedValue;

   // a. Let Pk be ToString(k).
   //   This is implicit for LHS operands of the in operator
   // b. Let kPresent be the result of calling the HasProperty internal 
   //    method of O with argument Pk.
   //   This step can be combined with c
   // c. If kPresent is true, then
   if (k in O) {

     // i. Let kValue be the result of calling the Get internal 
     //    method of O with argument Pk.
     kValue = O[k];

     // ii. Let mappedValue be the result of calling the Call internal 
     //     method of callback with T as the this value and argument 
     //     list containing kValue, k, and O.
     mappedValue = callback.call(T, kValue, k, O);

     // iii. Call the DefineOwnProperty internal method of A with arguments
     // Pk, Property Descriptor
     // { Value: mappedValue,
     //   Writable: true,
     //   Enumerable: true,
     //   Configurable: true },
     // and false.

     // In browsers that support Object.defineProperty, use the following:
     // Object.defineProperty(A, k, {
     //   value: mappedValue,
     //   writable: true,
     //   enumerable: true,
     //   configurable: true
     // });

     // For best browser support, use the following:
     A[k] = mappedValue;
   }
   // d. Increase k by 1.
   k++;
 }

 // 9. return A
 return A;
};
}

//https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/some
//Production steps of ECMA-262, Edition 5, 15.4.4.17
//Reference: http://es5.github.io/#x15.4.4.17
if (!Array.prototype.some) {
Array.prototype.some = function(fun /*, thisArg*/) {
 'use strict';

 if (this == null) {
   throw new TypeError('Array.prototype.some called on null or undefined');
 }

 if (typeof fun !== 'function') {
   throw new TypeError();
 }

 var t = Object(this);
 var len = t.length >>> 0;

 var thisArg = arguments.length >= 2 ? arguments[1] : void 0;
 for (var i = 0; i < len; i++) {
   if (i in t && fun.call(thisArg, t[i], i, t)) {
     return true;
   }
 }

 return false;
};
}

//https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/filter
if (!Array.prototype.filter) {
    Array.prototype.filter = function(fun/*, thisArg*/) {
      'use strict';

      if (this === void 0 || this === null) {
        throw new TypeError();
      }

      var t = Object(this);
      var len = t.length >>> 0;
      if (typeof fun !== 'function') {
        throw new TypeError();
      }

      var res = [];
      var thisArg = arguments.length >= 2 ? arguments[1] : void 0;
      for (var i = 0; i < len; i++) {
        if (i in t) {
          var val = t[i];

          // NOTE: Technically this should Object.defineProperty at
          //       the next index, as push can be affected by
          //       properties on Object.prototype and Array.prototype.
          //       But that method's new, and collisions should be
          //       rare, so use the more-compatible alternative.
          if (fun.call(thisArg, val, i, t)) {
            res.push(val);
          }
        }
      }

      return res;
    };
  }

//https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/every
if (!Array.prototype.every) {
    Array.prototype.every = function(callbackfn, thisArg) {
      'use strict';
      var T, k;

      if (this == null) {
        throw new TypeError('this is null or not defined');
      }

      // 1. Let O be the result of calling ToObject passing the this 
      //    value as the argument.
      var O = Object(this);

      // 2. Let lenValue be the result of calling the Get internal method
      //    of O with the argument "length".
      // 3. Let len be ToUint32(lenValue).
      var len = O.length >>> 0;

      // 4. If IsCallable(callbackfn) is false, throw a TypeError exception.
      if (typeof callbackfn !== 'function') {
        throw new TypeError();
      }

      // 5. If thisArg was supplied, let T be thisArg; else let T be undefined.
      if (arguments.length > 1) {
        T = thisArg;
      }

      // 6. Let k be 0.
      k = 0;

      // 7. Repeat, while k < len
      while (k < len) {

        var kValue;

        // a. Let Pk be ToString(k).
        //   This is implicit for LHS operands of the in operator
        // b. Let kPresent be the result of calling the HasProperty internal 
        //    method of O with argument Pk.
        //   This step can be combined with c
        // c. If kPresent is true, then
        if (k in O) {

          // i. Let kValue be the result of calling the Get internal method
          //    of O with argument Pk.
          kValue = O[k];

          // ii. Let testResult be the result of calling the Call internal method
          //     of callbackfn with T as the this value and argument list 
          //     containing kValue, k, and O.
          var testResult = callbackfn.call(T, kValue, k, O);

          // iii. If ToBoolean(testResult) is false, return false.
          if (!testResult) {
            return false;
          }
        }
        k++;
      }
      return true;
    };
  }

//https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/forEach
//Production steps of ECMA-262, Edition 5, 15.4.4.18
//Reference: http://es5.github.io/#x15.4.4.18
if (!Array.prototype.forEach) {

Array.prototype.forEach = function(callback, thisArg) {

 var T, k;

 if (this == null) {
   throw new TypeError(' this is null or not defined');
 }

 // 1. Let O be the result of calling ToObject passing the |this| value as the argument.
 var O = Object(this);

 // 2. Let lenValue be the result of calling the Get internal method of O with the argument "length".
 // 3. Let len be ToUint32(lenValue).
 var len = O.length >>> 0;

 // 4. If IsCallable(callback) is false, throw a TypeError exception.
 // See: http://es5.github.com/#x9.11
 if (typeof callback !== "function") {
   throw new TypeError(callback + ' is not a function');
 }

 // 5. If thisArg was supplied, let T be thisArg; else let T be undefined.
 if (arguments.length > 1) {
   T = thisArg;
 }

 // 6. Let k be 0
 k = 0;

 // 7. Repeat, while k < len
 while (k < len) {

   var kValue;

   // a. Let Pk be ToString(k).
   //   This is implicit for LHS operands of the in operator
   // b. Let kPresent be the result of calling the HasProperty internal method of O with argument Pk.
   //   This step can be combined with c
   // c. If kPresent is true, then
   if (k in O) {

     // i. Let kValue be the result of calling the Get internal method of O with argument Pk.
     kValue = O[k];

     // ii. Call the Call internal method of callback with T as the this value and
     // argument list containing kValue, k, and O.
     callback.call(T, kValue, k, O);
   }
   // d. Increase k by 1.
   k++;
 }
 // 8. return undefined
};
}

// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/lastIndexOf
//Production steps of ECMA-262, Edition 5, 15.4.4.15
//Reference: http://es5.github.io/#x15.4.4.15
if (!Array.prototype.lastIndexOf) {
Array.prototype.lastIndexOf = function(searchElement /*, fromIndex*/) {
 'use strict';

 if (this === void 0 || this === null) {
   throw new TypeError();
 }

 var n, k,
   t = Object(this),
   len = t.length >>> 0;
 if (len === 0) {
   return -1;
 }

 n = len - 1;
 if (arguments.length > 1) {
   n = Number(arguments[1]);
   if (n != n) {
     n = 0;
   }
   else if (n != 0 && n != (1 / 0) && n != -(1 / 0)) {
     n = (n > 0 || -1) * Math.floor(Math.abs(n));
   }
 }

 for (k = n >= 0 ? Math.min(n, len - 1) : len - Math.abs(n); k >= 0; k--) {
   if (k in t && t[k] === searchElement) {
     return k;
   }
 }
 return -1;
};
}

//https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/indexOf
//Production steps of ECMA-262, Edition 5, 15.4.4.14
//Reference: http://es5.github.io/#x15.4.4.14
if (!Array.prototype.indexOf) {
Array.prototype.indexOf = function(searchElement, fromIndex) {

 var k;

 // 1. Let O be the result of calling ToObject passing
 //    the this value as the argument.
 if (this == null) {
   throw new TypeError('"this" is null or not defined');
 }

 var O = Object(this);

 // 2. Let lenValue be the result of calling the Get
 //    internal method of O with the argument "length".
 // 3. Let len be ToUint32(lenValue).
 var len = O.length >>> 0;

 // 4. If len is 0, return -1.
 if (len === 0) {
   return -1;
 }

 // 5. If argument fromIndex was passed let n be
 //    ToInteger(fromIndex); else let n be 0.
 var n = +fromIndex || 0;

 if (Math.abs(n) === Infinity) {
   n = 0;
 }

 // 6. If n >= len, return -1.
 if (n >= len) {
   return -1;
 }

 // 7. If n >= 0, then Let k be n.
 // 8. Else, n<0, Let k be len - abs(n).
 //    If k is less than 0, then let k be 0.
 k = Math.max(n >= 0 ? n : len - Math.abs(n), 0);

 // 9. Repeat, while k < len
 while (k < len) {
   // a. Let Pk be ToString(k).
   //   This is implicit for LHS operands of the in operator
   // b. Let kPresent be the result of calling the
   //    HasProperty internal method of O with argument Pk.
   //   This step can be combined with c
   // c. If kPresent is true, then
   //    i.  Let elementK be the result of calling the Get
   //        internal method of O with the argument ToString(k).
   //   ii.  Let same be the result of applying the
   //        Strict Equality Comparison Algorithm to
   //        searchElement and elementK.
   //  iii.  If same is true, return k.
   if (k in O && O[k] === searchElement) {
     return k;
   }
   k++;
 }
 return -1;
};
}