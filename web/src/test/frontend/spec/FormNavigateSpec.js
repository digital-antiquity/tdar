const TDAR = require("JS/tdar.root");

describe("FormNavigateSpec.js: form navigate tests", function() {
    function status() {
        var status = $('form').FormNavigate("status");
        return status;
    }

    beforeEach(function(){
        setFixtures('<form id="frm"><input type="text"><input type="submit" id="btn"></form>');
        spyOn(window, 'onbeforeunload');
        document.forms.frm.onsubmit = function(){return false;}
    });

    it('knows formnavigate status', function() {
        expect(status()).toBe('not initialized');

        $('form').FormNavigate();
        expect(status()).toBe('clean')

        $('form input').val('hi').change();
        expect(status()).toBe('dirty');

        $('form').FormNavigate('clean');
        expect(status()).toBe('clean');

        $('form input').val('hi').change();
        var evt = $.Event('beforeunload');
        $(window).trigger(evt);
        expect(window.onbeforeunload).toHaveBeenCalled();

        //submit button forces clean to allow the submit
        $('form input').val('hi').change();
        $('#btn').click();
        expect(status()).toBe('clean');
    });

    it('can force clean/dirty state programmatically', function(){
        expect(status()).toBe('not initialized');
        $('form').FormNavigate();
        expect(status()).toBe('clean');

        //now make it dirty by fiat
        $('form').FormNavigate('dirty');
        expect(status()).toBe('dirty');

        //now set back to clean
        $('form').FormNavigate('clean');
        expect(status()).toBe('clean');
    });


})