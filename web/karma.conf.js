// Karma configuration
var wro = require('./src/test/frontend/lib/wro');
var fs = require('fs');
var process = require('process');

/**
 * @Param {string} [profile=default]
 */
function buildFilesFromWro(profile) {
    if(!profile) profile = 'default'
    var xmldata = fs.readFileSync('src/main/resources/wro.xml', 'utf-8');
    var wroconfig = wro.parse(xmldata);
    var files = ( 
            wroconfig[profile].cssFiles
            .concat(wroconfig[profile].jsFiles)
            .map(function(file){return 'src/main/webapp' + file;}));
    return files;
}



module.exports = function(config) {
    var wroFiles = buildFilesFromWro('default');
    config.set({

        // base path that will be used to resolve all patterns (eg. files, exclude)
        basePath: '',

        // frameworks to use
        // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
        frameworks: ['jasmine-ajax', 'jasmine-jquery', 'jasmine'],

        // list of files / patterns to load in the browser (we aren't using requireJS, so order matters)
        files: [].concat(
            [
                // app dependencies  (included in DOM served by karma, but not monitored for changes)
                {pattern: 'src/main/webapp/components/jquery/dist/jquery.js', watched: false},
                {pattern: 'src/main/webapp/includes/jquery-ui-1.11.4/jquery-ui.min.js', watched: false},
                {pattern: 'src/main/webapp/includes/jquery-ui-1.11.4/jquery-ui.min.css', watched: false},
                {pattern: 'src/main/webapp/includes/modernizr-custom-2.6.2.min.js', watched: false},
                {pattern: 'src/main/webapp/includes/jquery.validate-1.13.1/jquery.validate.js', watched: false},
                {pattern: 'src/main/webapp/includes/jquery.validate-1.13.1/additional-methods.js', watched: false},
                {pattern: 'src/main/webapp/includes/bootstrap-2.32/js/bootstrap.js', watched: false},
                {pattern: 'src/main/webapp/includes/bootstrap-2.32/css/bootstrap.css', watched: false}


            ],
            //files specified in wro.xml 
            wroFiles,
            [
                // specs
                'src/test/frontend/spec/**/*.js',

                // jasmine fixtures - added to DOM when you call loadFixtures(filename) in your test
                {pattern:'src/test/frontend/fixtures/**/*.html', watched:true, served:true, included:false},

                // html2js fixtures - globally accessible via  window.__html__[filepath]
                'src/test/frontend/html2js/**/*.html',

                //static files: served by karma webserver but not included on page
                {pattern: 'src/main/webapp/images/**/*', served:true, included:false, watched:false},


                //static files: images used by js libraries, e.g. jquery-ui, jquery-file-upload
                {pattern: 'src/main/webapp/includes/**/images/**/*', served:true, included:false, watched:false},
                {pattern: 'src/main/webapp/includes/**/img/**/*', served:true, included:false, watched:false},
                {pattern: 'src/main/webapp/components/**/*.*', served:true, included:false, watched:false},
                {pattern: 'src/main/webapp/js/maps/**/*.*', served:true, included:false, watched:false},

            ]),

        // certain html and css files may expect static resources at specific urls (e.g. /images/foo.gif)
        proxies: {
            '/images/': '/base/src/main/webapp/images/',
            '/includes/': '/base/src/main/webapp/includes/',
            '/js/maps/': '/base/src/main/webapp/js/maps/'
        },
            ///Users/jimdevos/develop/tdar.src/src/main/webapp/js/maps/world.json

        // list of files to exclude that would otherwise get picked up by the config.files patterns
        exclude: [],

        // preprocess matching files before serving them to the browser
        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors: {
            //converts html to js strings and stores them in window.__html__
			
            //caveat: files deeclared here cannot also be used as jasmine fixtures (known bug)
            //TODO: do we need both jasmine + htmljs fixtures? Figure out advantages/disadvantages of each
            'src/test/frontend/html2js/*.html': ['html2js'],
			'src/main/webapp/js/**/*.js': ['coverage']
        },

        //  Note:  'coverage' will obfuscate stack traces in failed tests.  Omit by declaring KARMA_SKIP_COVERAGE environment variable w/ non-empty value.
        reporters: (function(){
            var reporters = ['progress', 'coverage', 'junit'];
            if(process.env.hasOwnProperty('KARMA_SKIP_COVERAGE')) {
                reporters.splice(reporters.indexOf('coverage'), 1);
            }
            return reporters;
        })(),
        junitReporter: {
            outputDir: 'target/karma/reports/',
            suite: 'models'
        },

	    coverageReporter: {
	        type : 'html',
	        dir : 'target/karma/coverage/'
	    },
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
        singleRun: true
    });
};
