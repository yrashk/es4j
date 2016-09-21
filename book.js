var fs = require('fs');

var version = fs.readFileSync('gradle.properties').toString().split("\n").filter(function (s) {
    return s.startsWith("lastRelease");
})[0].split("=")[1];

var isSnapshot = version.endsWith("-SNAPSHOT");

module.exports = {
    // Documentation for GitBook is stored under "docs"
    root: './docs',
    title: 'Eventsourcing for Java Documentation',
    plugins: ["versions","es4j-doc","include","local-plantuml"],
    pluginsConfig: {
        versions: {
            type: "branches",
            gitbookConfigURL: "https://raw.githubusercontent.com/eventsourcing/es4j/master/book.js",
            options: [
                {
                    value: "https://es4j.eventsourcing.com/docs/master",
                    text: "master",
                    selected: true
                },
                {
                    value: "https://es4j.eventsourcing.com/docs/0.4.2",
                    text: "Version 0.4.2"
                },
                {
                    value: "https://es4j.eventsourcing.com/docs/0.4.1",
                    text: "Version 0.4.1"
                },
                {
                    value: "https://es4j.eventsourcing.com/docs/0.4.0",
                    text: "Version 0.4.0"
                }
            ]

        }
    },
    variables: {
        isSnapshot: isSnapshot,
        version: version
    },
    gitbook: '3.x.x'
};
