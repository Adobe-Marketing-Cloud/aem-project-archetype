@Grab(group="org.codehaus.groovy", module="groovy-all", version="2.4.8")
import static groovy.io.FileType.*
import groovy.util.XmlSlurper
import java.nio.file.Path
import java.util.regex.Pattern

def rootDir = new File(request.getOutputDirectory() + "/" + request.getArtifactId())
def uiAppsPackage = new File(rootDir, "ui.apps")
def uiContentPackage = new File(rootDir, "ui.content")
def uiConfigPackage = new File(rootDir, "ui.config")
def uiTestPackage = new File(rootDir, "ui.tests")
def coreBundle = new File(rootDir, "core")
def rootPom = new File(rootDir, "pom.xml")
def frontendModules = ["general", "angular", "react"]

def singleCountry = request.getProperties().get("singleCountry")
def appId =  request.getProperties().get("appId")
def javaPackage = request.getProperties().get("package")
def language = request.getProperties().get("language")
def country = request.getProperties().get("country")
def includeErrorHandler = request.getProperties().get("includeErrorHandler")
def frontendModule = request.getProperties().get("frontendModule")
def aemVersion = request.getProperties().get("aemVersion")
def sdkVersion = request.getProperties().get("sdkVersion")
def includeDispatcherConfig = request.getProperties().get("includeDispatcherConfig")
def includeCommerce = request.getProperties().get("includeCommerce")
def includeForms = request.getProperties().get("includeForms")
def enableSSR = request.getProperties().get("enableSSR");
def sdkFormsVersion = request.getProperties().get("sdkFormsVersion")

def appsFolder = new File("$uiAppsPackage/src/main/content/jcr_root/apps/$appId")
def configFolder = new File("$uiConfigPackage/src/main/content/jcr_root/apps/$appId/osgiconfig")
def confFolder = new File("$uiContentPackage/src/main/content/jcr_root/conf/$appId")
def contentFolder = new File("$uiContentPackage/src/main/content/jcr_root/content/$appId")
def varFolder = new File("$uiContentPackage/src/main/content/jcr_root/var")


if (aemVersion.startsWith("6.4")){
    // remove json config files with ~ in naming as they are not compatible with 6.4.8.2
    assert new File("$configFolder/config/org.apache.sling.commons.log.LogManager.factory.config~${appId}.cfg.json").delete()
    assert new File("$configFolder/config.author/com.day.cq.wcm.mobile.core.impl.MobileEmulatorProvider~${appId}.cfg.json").delete()
    assert new File("$configFolder/config.prod/org.apache.sling.commons.log.LogManager.factory.config~${appId}.cfg.json").delete()
    assert new File("$configFolder/config.stage/org.apache.sling.commons.log.LogManager.factory.config~${appId}.cfg.json").delete()
    assert new File("$configFolder/config.publish/org.apache.sling.jcr.resource.internal.JcrResourceResolverFactoryImpl.cfg.json").delete()
} else {
    // remove the old style config files
    assert new File("$configFolder/config/org.apache.sling.commons.log.LogManager.factory.config-${appId}.config").delete()
    assert new File("$configFolder/config.author/com.day.cq.wcm.mobile.core.impl.MobileEmulatorProvider-${appId}.config").delete()
    assert new File("$configFolder/config.prod/org.apache.sling.commons.log.LogManager.factory.config-${appId}.config").delete()
    assert new File("$configFolder/config.stage/org.apache.sling.commons.log.LogManager.factory.config-${appId}.config").delete()
    assert new File("$configFolder/config.publish/org.apache.sling.jcr.resource.internal.JcrResourceResolverFactoryImpl.config").delete()
}

if (amp == "n"){
    assert new File(uiAppsPackage, "src/main/content/jcr_root/apps/" + appId + "/components/page/customheadlibs.amp.html").delete()
    assert new File(uiAppsPackage, "src/main/content/jcr_root/apps/" + appId + "/components/image/clientlibs").deleteDir()
    assert new File(uiAppsPackage, "src/main/content/jcr_root/apps/" + appId + "/components/tabs/clientlibs").deleteDir()
}

if (includeErrorHandler == "n") {
    assert new File(uiAppsPackage, "src/main/content/jcr_root/apps/sling").deleteDir()
}

if (aemVersion == "cloud") {
    if (sdkVersion == "latest") {
        println "No SDK version specified, trying to fetch latest"
        sdkVersion = getLatestSDK(request.getArchetypeVersion())
    }
    println "Using AEM as a Cloud Service SDK version: " + sdkVersion
    rootPom.text = rootPom.text.replaceAll('SDK_VERSION', sdkVersion.toString())
}

buildContentSkeleton(uiContentPackage, uiAppsPackage, singleCountry, appId, language, country)
cleanUpFrontendModule(frontendModules, frontendModule, rootPom, rootDir, appsFolder, confFolder, configFolder, contentFolder,enableSSR)

if ( includeDispatcherConfig == "n"){
    // remove the unneeded config file
    if (aemVersion.startsWith("6.4")) {
        assert new File("$configFolder/config.publish/org.apache.sling.jcr.resource.internal.JcrResourceResolverFactoryImpl.config").delete()
    } else {
        assert new File("$configFolder/config.publish/org.apache.sling.jcr.resource.internal.JcrResourceResolverFactoryImpl.cfg.json").delete()
    }
} else {
    def source;
    if (aemVersion == 'cloud')   {
        source = new File(rootDir.getPath(),'dispatcher.cloud')
    } else {
        source = new File(rootDir.getPath(), 'dispatcher.ams')
    }
    assert source.renameTo(new File(rootDir.getPath(),'dispatcher'))
    new File(rootDir, 'dispatcher').traverse(type: DIRECTORIES, nameFilter: ~/enabled.+$/) { it ->
        it.traverse(type: FILES) { enabledFile ->
            File availableFile = new File(enabledFile.getPath().replace('enabled', 'available'))
            if (!availableFile.exists()) {
                return;
            }
            Path target = java.nio.file.Paths.get(availableFile.getPath())
            Path link = java.nio.file.Paths.get(enabledFile.getPath())
            enabledFile.delete()
            Path relativeSrc = link.getParent().relativize(target)
            java.nio.file.Files.createSymbolicLink(link, relativeSrc)
        }
    }
}
assert new File(rootDir, 'dispatcher.ams').deleteDir()
removeModule(rootPom, 'dispatcher.ams')
assert new File(rootDir, 'dispatcher.cloud').deleteDir()
removeModule(rootPom, 'dispatcher.cloud')

if (includeCommerce == "n") {
    assert new File(rootDir, "README-CIF.md").delete()
    assert new File("$appsFolder/components/commerce").deleteDir()
    assert new File("$appsFolder/clientlibs/clientlib-cif").deleteDir()
    assert new File("$configFolder/config/com.adobe.cq.commerce.graphql.client.impl.GraphqlClientImpl~default.cfg.json").delete()
    assert new File("$configFolder/config/com.adobe.cq.commerce.graphql.client.impl.GraphqlClientImpl-default.config").delete()
    assert new File("$configFolder/config/com.adobe.cq.commerce.core.components.internal.services.UrlProviderImpl.cfg.json").delete()
    assert new File("$configFolder/config/com.adobe.cq.commerce.core.components.internal.services.UrlProviderImpl.config").delete()
    assert new File("$configFolder/config/com.adobe.cq.commerce.core.components.internal.servlets.SpecificPageFilterFactory~default.cfg.json").delete()
    assert new File("$configFolder/config/com.adobe.cq.commerce.core.components.internal.servlets.SpecificPageFilterFactory-default.config").delete()
    assert new File("$configFolder/config.publish/com.adobe.cq.commerce.core.components.internal.services.UrlProviderImpl.cfg.json").delete()
    assert new File("$configFolder/config.publish/com.adobe.cq.commerce.core.components.internal.services.UrlProviderImpl.config").delete()
    assert new File("$appsFolder/components/header").deleteDir()
    assert new File("$confFolder/settings/cloudconfigs/commerce").deleteDir()
    assert new File("$varFolder").deleteDir();
    assert new File("$confFolder/settings/wcm/templates/catalog-page").deleteDir()
    assert new File("$confFolder/settings/wcm/templates/category-page").deleteDir()
    assert new File("$confFolder/settings/wcm/templates/landing-page").deleteDir()
    assert new File("$confFolder/settings/wcm/templates/product-page").deleteDir()
    assert new File("$confFolder/settings/wcm/templates/root-page").deleteDir()
    assert new File("$contentFolder/language-masters/en/my-account").deleteDir()
    assert new File("$contentFolder/language-masters/en/products").deleteDir()
    assert new File("$contentFolder/language-masters/en/reset-password").deleteDir()
    assert new File("$contentFolder/language-masters/en/search").deleteDir()
    assert new File("$contentFolder/us/en/my-account").deleteDir()
    assert new File("$contentFolder/us/en/products").deleteDir()
    assert new File("$contentFolder/us/en/reset-password").deleteDir()
    assert new File("$contentFolder/us/en/search").deleteDir()

    def packageFolder = javaPackage.replaceAll("\\.", "/")
    assert new File(coreBundle, "src/main/java/$packageFolder/core/models/commerce").deleteDir()
    assert new File(coreBundle, "src/test/java/$packageFolder/core/models/commerce").deleteDir()
    if (frontendModule == "general") {
        assert new File(rootDir, "ui.frontend/src/main/webpack/components/commerce").deleteDir()
    }
} else {
    if (aemVersion == "cloud") {
        assert new File("$configFolder/config/com.adobe.cq.commerce.graphql.client.impl.GraphqlClientImpl~default.cfg.json").delete()
        assert new File("$varFolder").deleteDir()
    }
    if (aemVersion.startsWith("6.4")){
        assert new File("$configFolder/config/com.adobe.cq.commerce.graphql.client.impl.GraphqlClientImpl~default.cfg.json").delete()
        assert new File("$configFolder/config/com.adobe.cq.commerce.core.components.internal.services.UrlProviderImpl.cfg.json").delete()
        assert new File("$configFolder/config.publish/com.adobe.cq.commerce.core.components.internal.services.UrlProviderImpl.cfg.json").delete()
        assert new File("$configFolder/config/com.adobe.cq.commerce.core.components.internal.servlets.SpecificPageFilterFactory~default.cfg.json").delete()
    } else {
        assert new File("$configFolder/config/com.adobe.cq.commerce.graphql.client.impl.GraphqlClientImpl-default.config").delete()
        assert new File("$configFolder/config/com.adobe.cq.commerce.core.components.internal.services.UrlProviderImpl.config").delete()
        assert new File("$configFolder/config.publish/com.adobe.cq.commerce.core.components.internal.services.UrlProviderImpl.config").delete()
        assert new File("$configFolder/config/com.adobe.cq.commerce.core.components.internal.servlets.SpecificPageFilterFactory-default.config").delete()
    }
}

// if forms flag is not set, forms specific components, template-types, templates, themes should be deleted
if (includeForms == "n") {
    assert new File("$appsFolder/components/aemformscontainer").deleteDir()
    assert new File("$confFolder/settings/wcm/template-types/af-page").deleteDir()
    assert new File("$confFolder/settings/wcm/templates/basic-af").deleteDir()
    assert new File("$confFolder/settings/wcm/templates/blank-af").deleteDir()
    assert new File("$uiContentPackage/src/main/content/jcr_root/content/dam/formsanddocuments-themes").deleteDir()
    assert new File("$uiContentPackage/src/main/content/jcr_root/content/dam/$appId/sample_logo.png").deleteDir()
    assert new File("$uiContentPackage/src/main/content/jcr_root/content/dam/$appId/sample_terms.png").deleteDir()
    assert new File("$uiTestPackage/test-module/specs/aem/forms.js").delete()
    assert new File("$uiTestPackage/test-module/lib/util/forms.selectors.js").delete()

} else {
    if (aemVersion == "cloud") {
        // if forms is included and aem version is set to cloud, set the forms sdk version
        if (sdkFormsVersion == "latest") {
            println "No Forms SDK version specified, trying to fetch latest"
            sdkFormsVersion = getLatestFormsSDK(request.getArchetypeVersion())
        }
        println "Using AEM Forms as a Cloud Service SDK version: " + sdkFormsVersion
        rootPom.text = rootPom.text.replaceAll('SDK_FORMS_VERSION', sdkFormsVersion.toString())
    }
}

// if config.publish folder ends up empty, remove it, otherwise the filevault-package-maven-plugin will throw
// an violation with severity=ERROR
if(new File("$configFolder/config.publish").list().length == 0) {
    assert new File("$configFolder/config.publish").deleteDir()
}

/**
 * Creates content skeleton based upon singleCountry, language and country input from user
 */
def buildContentSkeleton(uiContentPackage, uiAppsPackage, singleCountry, appId, language, country) {
    println "Creating content skeleton..."

    if (singleCountry == "y") {
        def languageMastersDir = new File(uiContentPackage, "src/main/content/jcr_root/content/${appId}/language-masters")
        languageMastersDir.deleteDir()
        def languageMastersXFMDir = new File(uiContentPackage, "src/main/content/jcr_root/content/experience-fragments/${appId}/language-masters")
        languageMastersXFMDir.deleteDir()
        def msmAppsDir = new File(uiAppsPackage, "src/main/content/jcr_root/apps/msm")
        msmAppsDir.deleteDir()
    } else {
        def languageDir = new File(uiContentPackage, "src/main/content/jcr_root/content/${appId}/language-masters/en")
        languageDir.renameTo(new File(uiContentPackage, "src/main/content/jcr_root/content/${appId}/language-masters/${language}"))
        def languageXFMDir = new File(uiContentPackage, "src/main/content/jcr_root/content/experience-fragments/${appId}/language" + "-masters/en")
        languageXFMDir.renameTo(new File(uiContentPackage, "src/main/content/jcr_root/content/experience-fragments/${appId}/language-masters/${language}"))
    }

    def countryDir = new File(uiContentPackage, "src/main/content/jcr_root/content/${appId}/us")
    def countryXFMDir = new File(uiContentPackage, "src/main/content/jcr_root/content/experience-fragments/${appId}/us")
    countryDir.renameTo(new File(uiContentPackage, "src/main/content/jcr_root/content/${appId}/${country}"))
    countryXFMDir.renameTo(new File(uiContentPackage, "src/main/content/jcr_root/content/experience-fragments/${appId}/${country}"))
    def languageInCountryDir = new File(uiContentPackage, "src/main/content/jcr_root/content/${appId}/${country}/en")
    def languageInCountryXFMDir = new File(uiContentPackage, "src/main/content/jcr_root/content/experience-fragments/${appId}/${country}/en")
    languageInCountryDir.renameTo(new File(uiContentPackage, "src/main/content/jcr_root/content/${appId}/${country}/${language}"))
    languageInCountryXFMDir.renameTo(new File(uiContentPackage, "src/main/content/jcr_root/content/experience-fragments/${appId}/${country}/${language}"))
}

/**
 * Renames and deletes frontend related files as necessary
 */
def cleanUpFrontendModule(frontendModules, optionFrontendModule, rootPom, rootDir, appsFolder, confFolder, configFolder, contentFolder, enableSSR) {
    // Delete unwanted frontend modules
    frontendModules.each { def frontendModule ->
        // Clean up POM file
        removeModule(rootPom, 'ui.frontend.' + frontendModule)

        // Delete corresponding "ui.frontend.*" directory
        if (optionFrontendModule != frontendModule) {
            assert new File(rootDir, "ui.frontend.$frontendModule").deleteDir()
        }
    }

    // Rename selected frontend module (e.g. "ui.frontend.angular" -> "ui.frontend")
    if (optionFrontendModule != "none") {
        assert new File(rootDir, "ui.frontend.$optionFrontendModule").renameTo(new File(rootDir, "ui.frontend"))
    }

    // Not generating SPA: Delete SPA-specific files
    if (optionFrontendModule != "angular" && optionFrontendModule != "react") {
        // Delete app component
        assert new File("$appsFolder/components/structure/spa").deleteDir()
        assert new File("$appsFolder/components/xfpage/body.html").delete()

        // Delete EditConfigs
        assert new File("$appsFolder/components/text/_cq_editConfig.xml").delete()

        // Delete SPA templates
        assert new File("$confFolder/settings/wcm/templates/spa-app-template").deleteDir()
        assert new File("$confFolder/settings/wcm/templates/spa-page-template").deleteDir()

        // Delete SPA template types
        assert new File("$confFolder/settings/wcm/template-types/spa-app").deleteDir()
        assert new File("$confFolder/settings/wcm/template-types/spa-page").deleteDir()

        // Delete SPA content
        assert new File("$contentFolder/us/en/home").deleteDir()

    }else{
        assert new File("$appsFolder/components/xfpage/content.html").delete()
    }

    //cleanup SSR related files / folders when not choosing react (only react is supported for now) or not choosing SSR
    if(enableSSR == "n" )
    {
        assert new File("$appsFolder/components/page/body.html").delete();
        assert new File("$configFolder/config/com.adobe.cq.remote.content.renderer.impl.factory.ConfigurationFactoryImpl~${appId}.cfg.json").delete()
    }

    // Generating SPA: Delete non-SPA specific files
    if (optionFrontendModule == "angular" || optionFrontendModule == "react") {
        assert new File("$confFolder/settings/wcm/templates/page-content").deleteDir()
        assert new File("$confFolder/settings/wcm/template-types/page").deleteDir()

        if(enableSSR == "n"){

            if(optionFrontendModule == "react"){
                //cleanup IO runtime related files from react module
                assert new File(rootDir, "ui.frontend/webpack.config.express.js").delete();
                assert new File(rootDir, "ui.frontend/webpack.config.adobeio.js").delete();
                assert new File(rootDir, "ui.frontend/manifest.yml").delete();
                assert new File(rootDir, "ui.frontend/src/server").deleteDir();
                assert new File(rootDir, "ui.frontend/actions").deleteDir();
                assert new File(rootDir, "ui.frontend/scripts").deleteDir();
            }else if(optionFrontendModule == "angular"){
                assert new File(rootDir, "ui.frontend/server.ts").delete();
                assert new File(rootDir, "ui.frontend/tsconfig.server.json").delete();
                assert new File(rootDir, "ui.frontend/src/main.server.ts").delete();
                assert new File(rootDir, "ui.frontend/src/app/app.server.module.ts").delete();
            }

        }
    }

}

/**
 * Removes a module section from the given pom file
 * @param pomFile pom file where the module section should be removed from
 * @param module name of the module which should be removed
 */
def removeModule(pomFile, module) {
    def pattern = Pattern.compile("\\s*<module>" + Pattern.quote(module) + "</module>", Pattern.MULTILINE)
    def pomContent = pomFile.getText("UTF-8")
    pomContent = pomContent.replaceAll(pattern, "")
    pomFile.newWriter("UTF-8").withWriter { w ->
        w << pomContent
    }
}

def getLatestSDK(archetypeVersion) {
    def metadata = new XmlSlurper().parse("https://repo1.maven.org/maven2/com/adobe/aem/aem-sdk-api/maven-metadata.xml")
    def sdkVersion = metadata.versioning.latest
    if (sdkVersion == null || sdkVersion == "") {
        sdkVersion = System.console().readLine("Cannot get latest SDK version, please provide it manually: ")
    }
    return sdkVersion
}

def getLatestFormsSDK(archetypeVersion) {
    def metadata = new XmlSlurper().parse("https://repo1.maven.org/maven2/com/adobe/aem/aem-forms-sdk-api/maven-metadata.xml")
    def sdkVersion = metadata.versioning.latest
    if (sdkVersion == null || sdkVersion == "") {
        sdkVersion = System.console().readLine("Cannot get latest SDK version, please provide it manually: ")
    }
    return sdkVersion
}
