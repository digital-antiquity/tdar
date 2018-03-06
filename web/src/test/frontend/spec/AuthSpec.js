/* global describe, it, expect */
describe("AuthSpec.js: tests for TDAR.auth methods", function() {
    beforeEach(function() {
//        spyOn(window.location, 'reload');
        spyOn(window, 'setTimeout');
        spyOn(window, 'alert');
    });

    it("should work when we call initLogin", function() {
        loadFixtures("login-form.html");
        TDAR.auth.initLogin();
        var validator = $('form').data().validator;
        expect(validator).toBeDefined();
        $('form').valid();
        expect(validator.errorList.length).toBeGreaterThan(0);
    });

    it("should work when we call initRegister", function() {
        loadFixtures("registration-form.html");
        var form = document.getElementById('accountForm');
        var result = TDAR.auth.initRegister(form);
        expect(window.setTimeout).toHaveBeenCalled();

        //call the setTimeout callback, which we anticipate will call window.alert
        var cb = window.setTimeout.calls.mostRecent().args[0];
        cb();
        expect(window.alert).toHaveBeenCalled();
    });

});
