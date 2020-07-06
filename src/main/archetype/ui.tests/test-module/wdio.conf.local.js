/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * WDIO Testrunner Configuration - See https://webdriver.io/docs/configurationfile.html
 */
const conf = require('./lib/config');
const commons = require('./lib/commons');
const HtmlReporter = require('@rpii/wdio-html-reporter').HtmlReporter;
const path = require('path');
const log4js = require('log4js');

exports.config = {
    runner: 'local',

    hostname: 'localhost',
    // Tests
    specs: [
        './specs/**/*.js'
    ],

    services: [
        ['selenium-standalone', {
            logPath: conf.reports_path}
        ]
    ],

    logLevel: 'debug',

    bail: 0,

    baseUrl: conf.aem.author.base_url,

    sync: true,

    waitforTimeout: 10000,
    connectionRetryTimeout: 90000,
    connectionRetryCount: 3,

    framework: 'mocha',

    // Location of the WDIO/Selenium logs 
    outputDir: conf.reports_path,

    // Reporters
    reporters: [
        'spec',
        ['junit', {
            outputDir: path.join(conf.reports_path, 'junit'),
            outputFileFormat: function(options) {
                return `results-${options.cid}.${options.capabilities.browserName}.xml`
            }
        }],
        [HtmlReporter, {
            debug: true,
            outputDir: path.join(path.relative(process.cwd(), conf.reports_path), 'html/'),
            filename: 'report.html',
            reportTitle: 'EaaS UI Testing Docker example',
            showInBrowser: false,
            useOnAfterCommandForScreenshot: true,
            LOG: log4js.getLogger('default')
        }],
    ],

    // Mocha parameters
    mochaOpts: {
        ui: 'bdd',
        timeout: 60000
    },

    // WDIO Hook executed after each test
    afterTest: function(test, context, { error, result, duration, passed, retries }) {
        // Take a screenshot that will be attached in the HTML report
        commons.takeScreenshot(browser);
    }

}
