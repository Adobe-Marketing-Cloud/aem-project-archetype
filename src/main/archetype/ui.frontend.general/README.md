${hash} Frontend Build

${hash}${hash} Features

* Full TypeScript, ES6 and ES5 support (with applicable Webpack wrappers).
* TypeScript and JavaScript linting (using a TSLint ruleset – driven by ESLint - rules can be adjusted to suit your team's needs).
* ES5 output, for legacy browser support.
* Globbing
    * No need to add imports anywhere.
    * All JS and CSS files can now be added to each component (best practice is under /clientlib/js or /clientlib/(s)css)
    * No .content.xml or js.txt/css.txt files needed as everything is run through Webpack
    * The globber pulls in all JS files under the /component/ folder. Webpack allows CSS/SCSS files to be chained in via JS files. They are pulled in through sites.js.
    * The only files consumed by AEM are the output files site.js and site.css, the resources folder in /clientlib-site as well as dependencies.js and dependencies.css in /clientlib-dependencies
* Chunks
    * Main (site js/css)
* Full Sass/Scss support (Sass is compiled to CSS via Webpack).
* Static webpack development server with built in proxy to a local instance of AEM

${hash}${hash} Installation

1. Install [NodeJS](https://nodejs.org/en/download/) (v10+), globally. This will also install `npm`.
2. Navigate to `ui.frontend` in your project and run `npm install`. (You must have run the archetype with `-DfrontendModule=general` to populate the ui.frontend folder)

${hash}${hash} Usage

The following npm scripts drive the frontend workflow:

* `npm run dev` - Full build of client libraries with JS optimization disabled (tree shaking, etc) and source maps enabled and CSS optimization disabled.
* `npm run prod` - Full build of client libraries build with JS optimization enabled (tree shaking, etc), source maps disabled and CSS optimization enabled.
* `npm run start` - Starts a static webpack development server for local development with minimal dependencies on AEM.
* `npm run scrape` - Downloads the specified pages' HTML and saves it as static files for use by the webpack development server. See `Scraper Configurtion` section

${hash}${hash}${hash} General

The ui.frontend module compiles the code under the `ui.frontend/src` folder and outputs the compiled CSS and JS, and any resources beneath a folder named `ui.frontend/dist`.

* **Site** - `site.js`, `site.css` and a `resources/` folder for layout dependent images and fonts are created in a `dist/clientlib-site` folder.
* **Dependencies** - `dependencies.js` and `dependencies.css` are created in a `dist/clientlib-dependencies` folder.

${hash}${hash}${hash} JavaScript

* **Optimization** - for production builds, all JS that is not being used or
called is removed.

${hash}${hash}${hash} CSS

* **Autoprefixing** - all CSS is run through a prefixer and any properties that require prefixing will automatically have those added in the CSS.
* **Optimization** - at post, all CSS is run through an optimizer (cssnano) which normalizes it according to the following default rules:
    * Reduces CSS calc expression wherever possible, ensuring both browser compatibility and compression.
    * Converts between equivalent length, time and angle values. Note that by default, length values are not converted.
    * Removes comments in and around rules, selectors & declarations.
    * Removes duplicated rules, at-rules and declarations. Note that this only works for exact duplicates.
    * Removes empty rules, media queries and rules with empty selectors, as they do not affect the output.
    * Merges adjacent rules by selectors and overlapping property/value pairs.
    * Ensures that only a single `@charset` is present in the CSS file and moves it to the top of the document.
    * Replaces the CSS initial keyword with the actual value, when the resulting output is smaller.
    * Compresses inline SVG definitions with SVGO.
* **Cleaning** - explicit clean task for wiping out the generated CSS, JS and Map files on demand.
* **Source Mapping** - development build only.

${hash}${hash}${hash}${hash} Notes

* Utilizes dev-only and prod-only webpack config files that share a common config file. This way the development and production settings can be tweaked independently.

${hash}${hash}${hash} Client Library Generation

The second part of the ui.frontend module build process leverages the [aem-clientlib-generator](https://www.npmjs.com/package/aem-clientlib-generator) plugin to move the compiled CSS, JS and any resources into the `ui.apps` module. The aem-clientlib-generator configuration is defined in `clientlib.config.js`. The following client libraries are generated:

* **clientlib-site** - `ui.apps/src/main/content/jcr_root/apps/<app>/clientlibs/clientlib-site`
* **clientlib-dependencies** - `ui.apps/src/main/content/jcr_root/apps/<app>/clientlibs/clientlib-dependencies`

${hash}${hash}${hash}  Page Inclusion

`clientlib-site` and `clientlib-dependencies` categories are included on pages via the Page Policy configuration as part of the default template. To view the policy, edit the **Content Page Template**  > **Page Information** > **Page Policy**.

The final inclusion of client libraries on the sites page is as follows:

```html

<HTML>
    <head>
        <link rel="stylesheet" href="clientlib-base.css" type="text/css">
        <script type="text/javascript" src="clientlib-dependencies.js"></script>
        <link rel="stylesheet" href="clientlib-dependencies.css" type="text/css">
        <link rel="stylesheet" href="clientlib-site.css" type="text/css">
    </head>
    <body>
        ....
        <script type="text/javascript" src="clientlib-site.js"></script>
        <script type="text/javascript" src="clientlib-base.js"></script>
    </body>
</HTML>
```

The above inclusion can of course be modified by updating the Page Policy and/or modifying the categories and embed properties of respective client libraries.

${hash}${hash}${hash} Static Webpack Development Server

Included in the ui.frontend module is a [webpack-dev-server](https://github.com/webpack/webpack-dev-server) that provides live reloading for rapid front-end development outside of AEM. The setup leverages the [html-webpack-plugin](https://github.com/jantimon/html-webpack-plugin) to automatically inject CSS and JS compiled from the ui.frontend module into a static HTML template.

${hash}${hash}${hash}${hash} Important files

* `ui.frontend/webpack.dev.js` - This contains the configuration for the webpack-dev-serve and points to the html template to use. It also contains a proxy configuration to an AEM instance running on `localhost:4502`.
* `ui.frontend/src/main/webpack/static/index.html` - This is the static HTML that the server will run against. This allows a developer to make CSS/JS changes and see them immediately reflected in the markup. It is assumed that the markup placed in this file accurately reflects generated markup by AEM components. Note* that markup in this file does **not** get automatically synced with AEM component markup. This file also contains references to client libraries stored in AEM, like Core Component CSS and Responsive Grid CSS. The webpack development server is set up to proxy these CSS/JS includes from a local running AEM instance based on the configuration found in `ui.frontend/webpack.dev.js`.

${hash}${hash}${hash}${hash} Using

1. From within the root of the project run the command `mvn -PautoInstallSinglePackage clean install` to install the entire project to an AEM instance running at `localhost:4502`
2. Navigate inside the `ui.frontend` folder.
3. Run the following command `npm run start` to start the webpack dev server. Once started it should open a browser (localhost:8080 or the next available port).
4. You can now modify CSS, JS, SCSS, and TS files and see the changes immediately reflected in the webpack dev server. 

${hash}${hash}${hash} Scraper configuration

The scraper downloads the specified AEM pages as static HTML files and saves to the `src/main/webpack/site/static/` directory. It's used by the webpack development server.
It can be run with the `npm run scrape` command.
It can be configured either by passing command line arguments or using a configuration file (by default `scraper.config.json`).
Note that not all options can be set by command line parameters. Configuration file must contain `index` property which define a page that will be downloaded and saved into `webpack/static/index.html`

${hash}${hash}${hash}${hash} Command line parameters
Example of specifying a custom instance and password:
```bash
npm run scrape -- --host http://localhost:1234 --pass mypassword42
```

All the options:

Option | Alias | Default value | Description
--- | --- | --- | ---
--help | -h | n/a | Shows this table
--dir | -d | n/a | Directory to save the files to
--page | -p | n/a | AEM page to download as index.html
--config | -c | ./scraper.config.json | Configuration file
--host | n/a | http://localhost:4502 | AEM instance url
--user | n/a | admin | AEM username
--pass | n/a | admin | AEM password

${hash}${hash}${hash}${hash} Configuration file

By default, the `scraper.config.json` file is used. Sample configuration:

```json
{
    "dir": "./src/main/webpack/static",
    "index": "content/${appId}/us/en.html",
    "pages": [
        "content/${appId}/us/en.html",
        "content/${appId}/us.html"
    ]
}
```

The same options can be set as via the CLI. Additional pages to download can also be specified via the `pages` array. The necessary directory structure will be created.