/*
 *  Copyright 2021 Adobe Systems Incorporated
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

const config = require('../../lib/config');
const commons = require('../../lib/commons');
const selectors = require('../../lib/util/forms.selectors.js');

describe('AEM Forms Reference Artifacts', () => {

    let onboardingHdler,
        testName,
        consoleErrors = [];

    // AEM Login
    beforeEach(() => {
        // Logout/Login
        browser.AEMForceLogout();
        browser.url(config.aem.author.base_url);
        browser.AEMLogin(config.aem.author.username, config.aem.author.password);
        consoleErrors = commons.trackConsoleErrors(browser);
    });

    afterEach(() => {
        //Print console error logs for diagnosis in case test fails before console log verification steps
        console.log('Console error logs during test execution of [' + testName + '] : \n' + consoleErrors);
    });

    before(function() {
        // Enable helper to handle onboarding dialog popup
        onboardingHdler = new commons.OnboardingDialogHandler(browser);
        onboardingHdler.enable();
    });

    after(function() {
        // Disable helper to handle onboarding dialog popup
        onboardingHdler.disable();
    });


    describe('AEM Forms Theme', () => {

        it('Basic Testing of Canvas Theme', function () {
            testName = this.test.parent.title + '.' + this.test.title;
            let themePath = '/content/dam/formsanddocuments-themes/${appId}/canvas-3-0/jcr:content';

            //Open Canvas theme in editing mode
            browser.url(`${config.aem.author.base_url}/editor.html${themePath}`);

            //Verify Selectors list is visible
            expect($(selectors.editor.themeEditor.themeEditorObjSelector).waitForDisplayed()).toBe(true);

            //Verify no console error present in edit mode so far
            expect(consoleErrors).toHaveLength(0);

            //Click preview layer button
            expect($(selectors.editor.previewButtonSelector)).toBeDisplayed();
            browser.$(selectors.editor.previewButtonSelector).click();

            //Switch to content frame and verify preview layer mode
            browser.switchToFrame(0);
            expect($(selectors.editor.previewLayerSelector).waitForDisplayed()).toBe(true);

            //Verify no console error present in preview mode
            expect(consoleErrors).toHaveLength(0);

        });

    });

    describe('AEM Forms Templates', () => {
        let templates = [
                {path : '/conf/${appId}/settings/wcm/templates/basic-af', isBlankForm : false},
                {path : '/conf/${appId}/settings/wcm/templates/blank-af', isBlankForm : true}
            ],
            getContainerSelector = (templateContentPath, container) => {
                return `div[data-path="${templateContentPath}/${container}"]`;
            },
            verifyTemplate = (templatePath, isBlankForm) => {
                let templateStructure = templatePath + '/structure/jcr:content',
                    templateInitialContent = templatePath + '/initial/jcr:content',
                    afContainerOverlaySelector = '#OverlayWrapper ' + getContainerSelector(templateInitialContent,'guideContainer');

                //Open template in editing mode
                browser.url(`${config.aem.author.base_url}/editor.html${templatePath}/structure.html`);

                //First open structure layer
                expect($(selectors.editor.layerSwitcher).waitForDisplayed()).toBe(true);
                $(selectors.editor.layerSwitcher).click();
                expect($(selectors.editor.layerSelector.structure).waitForDisplayed()).toBe(true);
                $(selectors.editor.layerSelector.structure).click();

                //Verify all three containers are visible
                expect($(getContainerSelector(templateStructure,'parsys1'))).toBeDisplayed();
                expect($(getContainerSelector(templateStructure,'parsys2'))).toBeDisplayed();
                expect($(getContainerSelector(templateStructure,'guideContainer'))).toBeDisplayed();

                //Verify Applied Policy Content
                $(getContainerSelector(templateStructure,'guideContainer')).click();
                expect($(selectors.editor.templateEditor.structureLayer.policyButton).waitForDisplayed()).toBe(true);
                $(selectors.editor.templateEditor.structureLayer.policyButton).click();
                expect($(selectors.editor.templateEditor.structureLayer.policyPage.allowedComponent.adaptiveFormGroup + '[checked]').waitForDisplayed()).toBe(true);
                expect($(selectors.editor.templateEditor.structureLayer.policyPage.cancel).waitForDisplayed()).toBe(true);
                $(selectors.editor.templateEditor.structureLayer.policyPage.cancel).click();

                //Switch to initial content layer
                expect($(selectors.editor.layerSwitcher).waitForDisplayed()).toBe(true);
                $(selectors.editor.layerSwitcher).click();
                expect($(selectors.editor.layerSelector.initial).waitForDisplayed()).toBe(true);
                $(selectors.editor.layerSelector.initial).click();
                expect($(afContainerOverlaySelector).waitForDisplayed()).toBe(true);

                //Verify no console error present in structure and initial content layer
                expect(consoleErrors).toHaveLength(0);

                //Open Side panel, verify that no invalid item in properties tab and then close the side panel
                expect($(selectors.editor.sidePanelToggleButton).waitForDisplayed()).toBe(true);
                $(selectors.editor.sidePanelToggleButton).click();
                expect($(selectors.editor.sidePanelOpen).waitForDisplayed()).toBe(true);
                expect($(selectors.editor.sidePanel.propertiesTab + '.is-invalid').waitForDisplayed({reverse : true})).toBe(true);
                $(selectors.editor.sidePanelToggleButton).click();
                expect($(selectors.editor.sidePanelOpen).waitForDisplayed({reverse : true})).toBe(true);

                //Preview the template using url and verify AF field presence in the form
                browser.url(`${config.aem.author.base_url}/${templatePath}/initial.html?wcmmode=preview`);
                expect($(selectors.content.genericAFField).waitForDisplayed({ reverse : isBlankForm })).toBe(true);

                //Verify no console error present in preview mode
                expect(consoleErrors).toHaveLength(0);
            };

        templates.forEach(function (template) {
            it('Basic Testing of Template '+ template.path , function () {
                testName = this.test.parent.title + '.' + this.test.title;
                verifyTemplate(template.path, template.isBlankForm);
            });
        });

    });

});