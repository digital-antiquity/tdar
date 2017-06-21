/* global process */
// Simple test that verifies our wro parser is working. Exits w/ error code if any tests fail.
"use strict";

function fail(msg) {
	console.error(msg);
	process.exit(1);
}

function testParseString(){
	var wro = require("./lib/wro");
	var fs = require("fs");
	var path = require("path");
	var data = fs.readFileSync(path.resolve(__dirname, "../../main/resources/wro.xml"), "utf8");
	// data = "<some>data</some>";

    try {
        var doc = wro.parse(data);
    } catch(err) {
        fail(err);
    }

}

testParseString();





