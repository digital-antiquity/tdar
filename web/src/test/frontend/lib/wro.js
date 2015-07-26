/* simple library for reading WRO4J configuration */
(function(){
    'use strict';

    var fs = require('fs');
    var xml2js = require('xml2js');
    var path = require('path');
    var deasync = require('deasync');

    //transform the parsed object from list<group> into map<groupName, group>
    function _transform(obj) {
         var wro = obj.groups.group.reduce(function(prev, current){
             var group = {
                 name: current["$"].name, //xml2js places attribute values in  the "$" field
                 cssFiles: current.css,
                 jsFiles: current.js,
                 groupRef: current["group-ref"]
             };
             prev[group.name]=group;
             return prev;
         }, {});
         
         //handle groupRefs
         Object.keys(wro).forEach(function(groupName) {
             var group = wro[groupName];
             if(group.groupRef) {
                 group.groupRef = wro[group.groupRef];
             }
         });
        return wro;
    }
    
    function _parse(data, cb){
        var parser = xml2js.Parser();
        parser.parseString(data, function(err, obj){
            if(err) {cb(err); return}

            cb(null, _transform(obj));
        });
    }
    
    module.exports = {
        parse: _parse,
        parseSync: deasync(_parse)
    }

})();