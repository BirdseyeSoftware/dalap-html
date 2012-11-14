var config = exports;

config['Browser Tests Development'] = {
    environment: 'browser',
    sources: [],
    tests: ["resources/js/dalap_html_browser_test.js"//,
            //"resources/js/dalap_html_browser_test_optimized.js"
           ]
};

config['Server Tests'] = {
     environment: 'node',
     sources: [],
     tests: ["resources/js/dalap_html_node_test.js"]
 };
