

suite = {
    # --------------------------------------------------------------------------------------------------------------
    #  METADATA
    # --------------------------------------------------------------------------------------------------------------
    "mxversion": "5.190.1",
    "name": "DynQ",
    "versionConflictResolution": "latest",

    "version": "0.0.1",
    "release": False,
    "groupId": "ch.usi.inf.dag",

    "developer": {
        "name": "Filippo Schiavio",
        "organization": "USI - Dynamic Analysis Group",
    },


    # --------------------------------------------------------------------------------------------------------------
    #  DEPENDENCIES
    # --------------------------------------------------------------------------------------------------------------
    "imports": {
    },

    # --------------------------------------------------------------------------------------------------------------
    #  REPOS
    # --------------------------------------------------------------------------------------------------------------
    "repositories": {
    },

    "licenses" : {
        "UPL" : {
            "name" : "Universal Permissive License, Version 1.0",
            "url" : "http://opensource.org/licenses/UPL",
        }
    },
    "defaultLicense" : "UPL",

    # --------------------------------------------------------------------------------------------------------------
    #  LIBRARIES
    # --------------------------------------------------------------------------------------------------------------
    "libraries": {
        "GRAAL_SDK": {
            'sha1': '1ac4538804c9e18787ce66cf504fbfa4e8af5353',
            'sourceSha1': 'd3f6c4486f3269da1457c36656dd70ade54cf255',
            'maven': {
                "groupId": "org.graalvm.sdk",
                "artifactId": "graal-sdk",
                "version": "21.3.0",
            }
        },
        "TRUFFLE_API": {
            'sha1': 'c2ca434cf80f81c57c79f3f9b9be7a7c3452f531',
            'sourceSha1': '4d9aa62e787c630eb72663865ec0cdee31de6c12',
            'maven': {
                "groupId": "org.graalvm.truffle",
                "artifactId": "truffle-api",
                "version": "21.3.0",
            }
        },
        "TRUFFLE_DSL_PROCESSOR": {
            'sha1': 'e3433aa27affb7d1e0305db919dc19789c433eaf',
            'sourceSha1': '206b5f563183ede6f0d9eb00f462596c1627d5e8',
            'maven': {
                "groupId": "org.graalvm.truffle",
                "artifactId": "truffle-dsl-processor",
                "version": "21.3.0",
            }
        },

        # Calcite 1.29
        'CALCITE': {
            "sha1": "edac5c027882bd88087c7ee3eca7598bed2c446e",
            'path': './lib/calcite/target/calcite.jar'
        },

        "SLF4J": {
            "sha1": "8dacf9514f0c707cbbcdd6fd699e8940d42fb54e",
            "maven": {
                "groupId": "org.slf4j",
                "artifactId": "slf4j-simple",
                "version": "1.7.25",
            },
        },

        # Language specific
        "graaljs": {
            "sha1": "7e49ddee95ac60c4965a50518f405920e4704824",
            'sourceSha1': 'eb04c87a276ac7aa37115b3c110477f2b18fb09f',
            "maven": {
                "groupId": "org.graalvm.js",
                "artifactId": "js",
                "version": "21.3.0",
            },
        },

        "org.immutables.value": {
            "sha1": "d99fa1e04af5a1fda42fa9412d68eb7fe17a1071",
            "maven": {
                "groupId": "org.immutables",
                "artifactId": "value",
                "version": "2.8.8",
            },
        },

        'fastr': {
            "sha1": "03fb2e027428f55341d440261515139be6401ff1",
            'path': '$GRAALVM_DYNQ/languages/R/fastr.jar'
        },
    },

    # --------------------------------------------------------------------------------------------------------------
    #  PROJECTS
    # --------------------------------------------------------------------------------------------------------------
    "externalProjects": {
    },


    "projects": {
        "ch.usi.inf.dag.dynq": {
            "subDir": "projects",
            "sourceDirs": ["src"],
            "javaCompliance": "1.8",
            "annotationProcessors": ["org.immutables.value","TRUFFLE_DSL_PROCESSOR"],
            "dependencies": [
                "TRUFFLE_API",
                "GRAAL_SDK",
                "CALCITE",
                "SLF4J",
            ],
        },
        "ch.usi.inf.dag.dynq_js": {
            "subDir": "projects",
            "sourceDirs": ["src"],
            "javaCompliance": "1.8",
            "annotationProcessors": ["TRUFFLE_DSL_PROCESSOR"],
            "dependencies": [
                'ch.usi.inf.dag.dynq',
                "graaljs",
            ]
        },
        "ch.usi.inf.dag.dynq_r": {
            "subDir": "projects",
            "sourceDirs": ["src"],
            "javaCompliance": "1.8",
            "annotationProcessors": ["TRUFFLE_DSL_PROCESSOR"],
            "dependencies": [
                'ch.usi.inf.dag.dynq',
                'fastr',
            ]
        },
    },


    # --------------------------------------------------------------------------------------------------------------
    #  DISTRIBUTIONS
    # --------------------------------------------------------------------------------------------------------------
    "distributions": {
        "DynQ": {
            "dependencies": [
                "ch.usi.inf.dag.dynq",
            ],
            "distDependencies": [
                "TRUFFLE_API",
                "CALCITE",
                "SLF4J"
            ],
            "sourcesPath": "dynq.src.zip",
            "description": "DynQ",
        },
        "DynQ_JS": {
            "dependencies": [
                "ch.usi.inf.dag.dynq_js",
            ],
            "distDependencies": [
                'DynQ',
                "graaljs",
            ],
            "sourcesPath": "dynq_js.src.zip",
            "description": "DynQ_JS",
        },
        "DynQ_R": {
            "dependencies": [
                "ch.usi.inf.dag.dynq_r",
            ],
            "distDependencies": [
                'DynQ',
                'fastr',
            ],
            "sourcesPath": "dynq_r.src.zip",
            "description": "DynQ_R",
        },
    },
}
