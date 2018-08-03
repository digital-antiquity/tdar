const TDAR  = require("JS/tdar.core");
TDAR.common = require("JS/tdar.common");
TDAR.upload = require("JS/tdar.upload");
TDAR.auth   = require("JS/tdar.auth");

window.TDAR = TDAR;
if (TDAR['vuejs'] == undefined) {
	TDAR['vuejs'] = {};
}

module.exports = TDAR;