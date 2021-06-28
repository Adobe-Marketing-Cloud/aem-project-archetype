const fs = require("fs-extra");
const { name } = require("../package.json");

console.log("command line: rename <targetname>");
console.log("");
console.log("help        : rename template to <targetname>");


console.log(`current name: ${name}`);

const walk = require('ignore-walk')
const target = "mysite";

function renameFolder(path, source, target) {
  fs.moveSync(`${path}/${source}`, `${path}/${target}`);
}

function replaceInFile(path, source, target) {
  if(fs.statSync(path).isFile()) {
    const file = fs.readFileSync(path).toString();
    // console.log(file.replace(new RegExp(source, 'g'), target))
    fs.writeFileSync(path, file.replace(new RegExp(source, 'g'), target));
  }
}

renameFolder( './site/src/main/content/jcr_root/conf', name, target );
renameFolder( './site/src/main/content/jcr_root/content', name, target );
renameFolder( './site/src/main/content/jcr_root/content/dam', name, target );
renameFolder( './site/src/main/content/jcr_root/content/experience-fragments', name, target );

// All options are optional, defaults provided.
// to walk synchronously, do it this way:
const result = walk.sync(
  {
      path: '.', // root dir to start in. defaults to process.cwd()
      ignoreFiles: [ '.gitignore' ], // list of filenames. defaults to ['.ignore']
      includeEmpty: true, // true to include empty dirs, default false
      follow: true // true to follow symlink dirs, default false
    }
).forEach( path => { if(!path.startsWith(".")) { 
    console.log(path)
    replaceInFile( path, name, target );
}})

