## Site Template

### Build

To generate the site template:

```
cd site
mvn clean install
```

This command creates an AEM content package (e.g. `${appId}.site-0.0.1-SNAPSHOT.zip`) below the `target` folder: it contains the templates, the policies and the client library for the site theme.
