var fs = require('fs');

var version = fs.readFileSync('gradle.properties').toString().split("\n").filter(function(s) { return s.startsWith("lastRelease"); })[0].split("=")[1];

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
