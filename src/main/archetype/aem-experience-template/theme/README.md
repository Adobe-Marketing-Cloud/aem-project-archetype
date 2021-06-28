# Site Theme

This is the theme of the Minimal  site template for Adobe Experience Manager (AEM).

This theme can be modified to customize the visual appearance of sites created from the Minimal  site template.

## Structure

* `src/main.ts`: This is the main entry point of your JS & CSS theme.
* `src/site`: Files that are generic to the entire site.
* `src/components`: Files that are specific to components.
* `src/resources`: Associated files, like icons, logos, fonts.

## Build

1. Initialize the project with following command executed at the theme root:

```
npm install
```

2. Complete the `.env` file with credentials for the local proxy server to access the site created on Cloud Service.

3. Run the local proxy server while working to preview your changes with the content from the production environment.

```
npm run live
```

4. Once your work completed, check your changes into GitHub, and execute the deployment action on GitHub.
