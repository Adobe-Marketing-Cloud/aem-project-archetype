# AEM Experience Template 

1. Initialize a Git repository at the project root.

```
$ git init
```

2. Build and deploy the project to AEM.

```
$ mvn clean install -PautoInstallSinglePackage
```

3. Deploy the Experience Template to AEM.

```
$ cd aem-experience-template
$ npm run deploy
```

4. Log into AEM and create a site from the Experience Template: _Sites_ > _Create_ > _Site (Template)_.
