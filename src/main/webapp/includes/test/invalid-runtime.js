//this script contains intentionally contains bugs such to invoke runtime errors
function doNullReference() {
    var foo = window;
    console.log("about to read property of undefined");
    console.log("this shouldn't have a value: %s", foo.abc.def.ghi.j++);
    console.log("you shouldn't be seeing this line");
}

function throwError(msg) {
    throw new Error(msg);
}

doNullReference();