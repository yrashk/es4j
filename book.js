var execSync = require('child_process').execSync;

var version = execSync("git describe --abbrev=0 | sed s/v//").toString().replace(/\n$/, "");

module.exports = {
    // Documentation for GitBook is stored under "docs"
    root: './docs',
    title: 'Eventsourcing for Java Documentation',
    plugins: [ "versions" ],
    pluginsConfig: {
        versions: {
            type: "branches"
        }
    },
    variables: {
        version: version
    },
    gitbook: '3.0.0-pre.5'
};
