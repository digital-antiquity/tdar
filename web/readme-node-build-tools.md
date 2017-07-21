# Readme: Node-based Build Tools

## Overview
Parts of the tDAR application build process are handled by node build tools.  This document lists which node build tools that we use, 
and describes the common tasks that involve them.

## What node build tools do we use?

- Bower - javascript framework dependency management.  We use Bower to track and install a subset of 
 the third-party javascript libraries that we use for the client.
- Karma - we use Karma.js to run unit tests for our own javascript libraries, and also provides code coverage reports.
By default, Maven fires up  and runs these unit tests as part of the tDAR application build process.
- NPM (Node Package Manager) - NPM is responsible for installing necessary dependencies used by the aforementioned
Karma tests. 


## Common Task HOWTO's

### Prerequisite:  Install Karma and NodeJS
- Install NodeJS first.   http://www.nodejs.org
- Then install Karma:  `npm install -g karma`
- Finally, navigate to tdar.src/web and execute `npm install` to install the dependencies required by Karma and our tests.


### HOWTO: write a new Karma test
- We use Jasmine as our JS testing framework.  You can learn more at https://jasmine.github.io
- Tests are located in `src/test/frontend/spec` 
- Data integration tests are located in `src/test/frontent/spec-integrate


### HOWTO: run the Karma test suite 

*Note:  Make sure you have Karma and NodeJS globally installed.

To run the tests manually, execute the following shell command: `karma start`

To have karma listen for changes and re-run tests: `karma start --singleRun=false`


### HOWTO: update the npm-shrinkwrap file

The shirnkwrap file exists to make sure that our Karma test results are consistent across dev environments and our build environment.
It's a good idea to keep these dependencies up to date, but the process is a bit of a pain. In general, what you need to do is:

1. delete the shrinkwrap file (do not remove it from source control - simply delete the file)
2. update your dependencies by executing:   `npm install`
3. create a new shrinkwrap file: `npm shrinkwrap`
4. commit the updated file to your repo
5. IMPORTANT: make sure the npm-shrinkwrap file works on your build server.  NPM is supposed to omit platform-specific dependencies, but sometimes (due to bugs or poorly configured 3rd-party JS libraries) they slip through.  A good quick/dirty way to check for issues is to copy the `npm-shrinkwrap.json`  and `package.json` file to a temp folder on the build server and execute `npm install`.   If NPM fails,  find the offending dependencies and manually remove them from the XML file.  Then repeat 

