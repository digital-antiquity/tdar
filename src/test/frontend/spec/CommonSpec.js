/* global describe, it, expect, loadFixtures */
describe("TDAR.common: edit page tests", function() {  

   it("initializes the edit page", function() {
      var form = null;
      var props = {
               formSelector: "#metadataForm",
               includeInheritance : true,
               acceptFileTypes : /\.(pdf|doc|docx|rtf|txt)$/i,
               multipleUpload : true,
               validExtensions : "pdf|doc|docx|rtf|txt",
               validExtensionsWarning : "Please enter a valid file (pdf, doc, docx, rtf, txt)",
               ableToUpload : true,
                dataTableEnabled : false
            };
      
      loadFixtures("document-add-form.html", "fileupload-templates.html");
      expect($j("#template-upload")).toHaveLength(1);
      expect($j("#metadataForm")).toHaveLength(1);
      form = document.getElementById('metadataForm');
      var result = TDAR.common.initEditPage(form, props);

   });

   it("initializes form validation", function() {
      loadFixtures("document-add-form.html", "fileupload-templates.html");
      form = document.getElementById('metadataForm');
      var result = TDAR.common.initFormValidation(form);
   });


})

describe("TDAR.common: miscellaneaous tests", function() {


xit("should work when we call applyTreeviews", function() {
   var expectedVal = null;

   //var result = TDAR.common.applyTreeviews();
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call initializeView", function() {
   var expectedVal = null;

   //var result = TDAR.common.initializeView();
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call initRegformValidation", function() {
   var form = null;
   var expectedVal = null;

   //var result = TDAR.common.initRegformValidation(form);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call determineResponsiveClass", function() {
   var width = null;
   var expectedVal = null;

   //var result = TDAR.common.determineResponsiveClass(width);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call elipsify", function() {
   var text = null;
   var n = null;
   var useWordBoundary = null;
   var expectedVal = null;

   //var result = TDAR.common.elipsify(text, n, useWordBoundary);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call populateTarget", function() {
   var obj = null;
   var expectedVal = null;

   //var result = TDAR.common.populateTarget(obj);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call prepareDateFields", function() {
   var selectElem = null;
   var expectedVal = null;

   //var result = TDAR.common.prepareDateFields(selectElem);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call setAdhocTarget", function() {
   var elem = null;
   var selector = null;
   var expectedVal = null;

   //var result = TDAR.common.setAdhocTarget(elem, selector);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call changeSubcategory", function() {
   var categoryIdSelect = null;
   var subCategoryIdSelect = null;
   var expectedVal = null;

   //var result = TDAR.common.changeSubcategory(categoryIdSelect, subCategoryIdSelect);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call registerDownload", function() {
   var url = null;
   var tdarId = null;
   var expectedVal = null;

   //var result = TDAR.common.registerDownload(url, tdarId);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call registerShare", function() {
   var service = null;
   var url = null;
   var tdarId = null;
   var expectedVal = null;

   //var result = TDAR.common.registerShare(service, url, tdarId);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call gaevent", function() {
   var expectedVal = null;

   //var result = TDAR.common.gaevent();
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call outboundLink", function() {
   var elem = null;
   var expectedVal = null;

   //var result = TDAR.common.outboundLink(elem);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call setupSupportingResourceForm", function() {
   var totalNumberOfFiles = null;
   var rtype = null;
   var expectedVal = null;

   //var result = TDAR.common.setupSupportingResourceForm(totalNumberOfFiles, rtype);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call switchType", function() {
   var radio = null;
   var container = null;
   var expectedVal = null;

   //var result = TDAR.common.switchType(radio, container);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call setupDocumentEditForm", function() {
   var expectedVal = null;

   //var result = TDAR.common.setupDocumentEditForm();
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call sessionTimeoutWarning", function() {
   var expectedVal = null;

   //var result = TDAR.common.sessionTimeoutWarning();
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call applyBookmarks", function() {
   var expectedVal = null;

   //var result = TDAR.common.applyBookmarks();
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call sprintf", function() {
   var expectedVal = null;

   //var result = TDAR.common.sprintf();
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call htmlDecode", function() {
   var value = null;
   var expectedVal = null;

   //var result = TDAR.common.htmlDecode(value);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call htmlEncode", function() {
   var value = null;
   var expectedVal = null;

   //var result = TDAR.common.htmlEncode(value);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call htmlDoubleEncode", function() {
   var value = null;
   var expectedVal = null;

   //var result = TDAR.common.htmlDoubleEncode(value);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call applyWatermarks", function() {
   var context = null;
   var expectedVal = null;

   //var result = TDAR.common.applyWatermarks(context);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call coordinatesCheckboxClicked", function() {
   var elem = null;
   var expectedVal = null;

   //var result = TDAR.common.coordinatesCheckboxClicked(elem);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call refreshInputDisplay", function() {
   var expectedVal = null;

   //var result = TDAR.common.refreshInputDisplay();
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call tmpl", function() {
   var a = null;
   var c = null;
   var expectedVal = null;

   //var result = TDAR.common.tmpl(a, c);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call validateProfileImage", function() {
   var expectedVal = null;

   //var result = TDAR.common.validateProfileImage();
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call collectionTreeview", function() {
   var expectedVal = null;

   //var result = TDAR.common.collectionTreeview();
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call humanFileSize", function() {
   var bytes = null;
   var si = null;
   var expectedVal = null;

   //var result = TDAR.common.humanFileSize(bytes, si);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call initImageGallery", function() {
   var expectedVal = null;

   //var result = TDAR.common.initImageGallery();
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call formatNumber", function() {
   var num = null;
   var expectedVal = null;

   //var result = TDAR.common.formatNumber(num);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call registerAjaxStatusContainer", function() {
   var expectedVal = null;

   //var result = TDAR.common.registerAjaxStatusContainer();
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call suppressKeypressFormSubmissions", function() {
   var $form = null;
   var expectedVal = null;

   //var result = TDAR.common.suppressKeypressFormSubmissions($form);
   expect(true).toBe(false); //fixme: implement this test
});

});
