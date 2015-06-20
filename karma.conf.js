// Karma configuration
// Generated on Fri May 22 2015 16:00:39 GMT-0700 (MST)
module.exports = function(config) {
    config.set({

        // base path that will be used to resolve all patterns (eg. files, exclude)
        basePath: '',

        // frameworks to use
        // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
        frameworks: ['jasmine-jquery', 'jasmine'],

        // list of files / patterns to load in the browser (we aren't using requireJS, so order matters)
        files: [
            // app dependencies  (included in DOM served by karma, but not monitored for changes)
            {pattern: "src/main/webapp/components/jquery/jquery.js", watched: false},
            {pattern: "src/main/webapp/includes/jquery-ui-1.8.23/ui/minified/jquery-ui.min.js", watched: false},
            {pattern: "src/main/webapp/includes/jquery-ui-1.8.23/themes/base/minified/jquery-ui.min.css", watched: false},
            {pattern: "src/main/webapp/includes/modernizr-custom-2.6.2.min.js", watched: false},
            {pattern: "src/main/webapp/includes/modernizr-custom-2.6.2.min.js", watched: false},
            {pattern: "src/main/webapp/includes/jquery.validate-1.13.1/jquery.validate.js", watched: false},
            
            {pattern:"src/main/webapp/includes/blueimp-javascript-templates/tmpl.min.js", watched: false},
            {pattern:"src/main/webapp/includes/blueimp-jquery-file-upload-5.31.6/js/vendor/jquery.ui.widget.js", watched: false},
            {pattern:"src/main/webapp/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.iframe-transport.js", watched: false},
            {pattern:"src/main/webapp/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.fileupload.js", watched: false},
            {pattern:"src/main/webapp/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.fileupload-process.js", watched: false},
            {pattern:"src/main/webapp/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.fileupload-validate.js", watched: false},
            {pattern:"src/main/webapp/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.fileupload-ui.js", watched: false},            
            
            // app files (included in DOM, monitored for changes)
            "src/main/webapp/includes/blueimp-javascript-templates/tmpl.min.js",
            "src/main/webapp/js/tdar.core.js",
            "src/main/webapp/js/tdar.repeatrow.js",
            "src/main/webapp/js/tdar.autocomplete.js", //fixme: undeclared dependency in TDAR.common
            "src/main/webapp/js/tdar.heightevents.js", //fixme: undeclared dependency in TDAR.common
            "src/main/webapp/js/tdar.contexthelp.js", //fixme: undeclared dependency in TDAR.common
            "src/main/webapp/js/tdar.upload.js", //fixme: undeclared dependency in TDAR.common
            "src/main/webapp/js/tdar.jquery-upload-validation.js", //fixme: undeclared dependency in TDAR.common
            "src/main/webapp/js/tdar.common.js",

            // specs
            "src/test/frontend/spec/**/*.js",

            // jasmine fixtures - added to DOM when you call loadFixtures(filename) in your test
            {pattern:"src/test/frontend/fixtures/**/*.html", watched:true, served:true, included:false},

            // html2js fixtures - globally accessible via  window.__html__[filepath]
            "src/test/frontend/html2js/**/*.html"
        ],

        // list of files to exclude that would otherwise get picked up by the config.files patterns
        exclude: [],

        // preprocess matching files before serving them to the browser
        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors: {
            //converts html to js strings and stores them in window.__html__

            //caveat: files deeclared here cannot also be used as jasmine fixtures (known bug)
            //TODO: do we need both jasmine + htmljs fixtures? Figure out advantages/disadvantages of each
            'src/test/frontend/html2js/*.html': ['html2js']
        },

        // test results reporter to use
        // possible values: 'dots', 'progress'
        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters: ['progress'],

        // web server port
        port: 9876,

        // enable / disable colors in the output (reporters and logs)
        colors: true,

        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,

        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,

        // start these browsers
        // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
        browsers: ['PhantomJS'],

        // Continuous Integration mode
        // if true, Karma captures browsers, runs the tests and exits
        singleRun: false
    });
};
