/* simple library for reading WRO4J configuration */
(function(){
    'use strict';

    var fs = require('fs');
    var xml2js = require('xml2js');
    var path = require('path');
    var xmldoc = require('xmldoc');

    //transform the parsed object from list<group> into map<groupName, group>
    function _transform(xdoc) {
        // we need to make two passes through the groups
        // pass 1: transform each group into new data structure that is easier to work with
        var groupDict = xdoc.children.reduce(function(prev, group, i){
            console.log('encountered child: %s', group.attr.name);

            //tranform each group into object w/ three 'buckets' that hold list of js/css/group-ref names.
            var obj = {
                jsFiles:[],
                cssFiles:[],
                groupRef:[]
            };

            var transformedGroup = group.children
                .reduce(function(prev, item, i){
                    //divvy up each entry in the group into one of the three buckets
                    ({
                        'js': prev.jsFiles,
                        'css': prev.cssFiles,
                        'group-ref': prev.groupRef
                    })[item.name].push(item.val);
                    return prev;
                }, obj);

            prev[group.attr.name] = transformedGroup;
            return prev;
        }, {});

        //pass 2: resolve any group-ref references
        Object.keys(groupDict).forEach(function(groupName){
            var group = groupDict[groupName]
            if(group.groupRef.length === 1) {
                var refName = group.groupRef[0];
                group.groupRef = groupDict[refName];
            } else {
                delete group.groupRef
            }
        });
        return groupDict;
    }

    function _parse(str) {
        var doc = new xmldoc.XmlDocument(str);
        return _transform(doc);
    }
    
    module.exports = {
        parse: _parse
    }

})();