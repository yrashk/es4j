var fs = require('fs');

var version = fs.readFileSync('gradle.properties').toString().split("\n").filter(function (s) {
    return s.startsWith("lastRelease");
})[0].split("=")[1];

var isSnapshot = version.endsWith("-SNAPSHOT");

module.exports = {
    // Documentation for GitBook is stored under "docs"
    root: './docs',
    title: 'Eventsourcing for Java Documentation',
    plugins: ["versions","es4j-doc","include"],
    pluginsConfig: {
        versions: {
            type: "branches"
        }
    },
    variables: {
        isSnapshot: isSnapshot,
        version: version
    },
    gitbook: '3.x.x'
};
