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

### HOWTO: write a new Karma test
### HOWTO: run the Karma test suite
### HOWTO: install & update Karma's dependencies using NPM and npm-shrinkwrap

### HOWTO: Add 3rd-party javascript libraries via Bower



