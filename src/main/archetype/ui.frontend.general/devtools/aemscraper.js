const fetch = require('node-fetch');
const fs = require('fs');
const path = require('path')
const { Command } = require('commander');

const program = new Command();

program
    .option('-d, --dir <path>', 'Directory to save the files to')
    .option('-p, --page <index>', 'AEM page to download as index.html')
    .option('-c, --config <config>', 'Config file', './scraper.config.json')
    .option('--host <host>', 'AEM instance url', 'http://localhost:4502')
    .option('--user <user>', 'AEM username', 'admin')
    .option('--pass <pass>', 'AEM password', 'admin');

program.parse(process.argv);
const options = program.opts();

const config = JSON.parse(fs.readFileSync(options.config, 'utf8'));

config.dir = options.dir || config.dir;
config.index = options.index || config.index;
config.host = options.host || config.host;
config.user = options.user || config.user;
config.pass = options.pass || config.pass;
config.pages = config.pages || [];

const headers = {
    'Authorization': 'Basic ' + Buffer.from(`${config.user}:${config.pass}`).toString('base64')
};

const indexData = { file: 'index.html', page: config.index };
const pagesData = config.pages.map(page => ({ file: page, page }));

new Array(indexData).concat(pagesData).forEach(data => {
    const url = `${config.host}/${data.page}`;
    const file = `${config.dir}/${data.file}`;

    fetch(url, { method: 'GET', headers })
        .then(res => res.text())
        .then(body => fs.promises.mkdir(path.dirname(file), { recursive:true })
            .then(() => fs.promises.writeFile(file, body, 'utf-8')))
        .then(() => console.log(`AEM Page saved to disk:\n- page: ${url}\n- file: ${file}`))
        .catch(error => console.error(error));
});
