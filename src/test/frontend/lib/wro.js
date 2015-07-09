/* simple library for reading WRO4J configuration */
(function(){
    'use strict';

    var fs = require('fs');
    var xml2js = require('xml2js');
    var path = require('path');

    function _test() {
        var resourcesPath = path.resolve(__dirname, '../../../main/resources');
        var wroPath = path.resolve(resourcesPath, 'wro.xml');
        var xmldata = fs.readFileSync(wroPath, 'utf-8');
        var parser = xml2js.Parser();
        parser.on('end', onparse);
        parser.on('error', onerror);
        parser.parseString(xmldata);

        function onerror(err) {
            console.log('oops:%s', err);
        }

        function onparse(xmlObj) {
            console.log(JSON.stringify(xmlObj, null, 4));
        }
    }

    function _transform(parsedObject, group) {
        //todo:  transform into something more usable
        console.debug(JSON.stringify(parsedObject));
        return parsedObject;
    }

    module.exports = {
        //fixme:  this isn't functional (yet)
        parse: function(data, group, cb){
            var parser = xml2js.Parser();
            parser.parseString(data, function(err, obj){
                if(err) {cb(err); return}

                cb(null, _transform(obj, group));
            });
        },

    }

    //todo: remove this eventually
    _test();
})();