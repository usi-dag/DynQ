
const readline = require('readline');
const fs = require('fs');

function makeInterface(path) {
    return readline.createInterface({
        input: fs.createReadStream(path),
        // output: process.stdout,
        console: false
    });
}


const tpch_schema = ['lineitem', 'customer', 'nation', 'orders', 'partsupp', 'part', 'region', 'supplier'];

const dateFormat = /^\d{4}-\d{2}-\d{2}$/;
function parse_values(key, value) {
    if (typeof value === "string" && dateFormat.test(value)) {
        return new Date(value);
    }
    return value;
}

function parse_table_json(path, table) {
    const full_path = path + '/' + table + '.json';
    const readInterface = makeInterface(full_path);

    result = [];
    function parseLineWithSchema(line) {
        result.push(JSON.parse(line, parse_values));
    }

    readInterface.on('line', parseLineWithSchema);

    return new Promise(resolve => {
        readInterface.on('close', () =>  { resolve(result); });
    });
}

const parserFunctions = {
    'region': function(fields) {
        return {
            'r_regionkey': parseInt(fields[0]),
            'r_name': fields[1],
            'r_comment': fields[2]
        }
    },
    'nation': function(fields) {
        return {
            'n_nationkey': parseInt(fields[0]),
            'n_name': fields[1],
            'n_regionkey': parseInt(fields[2]),
            'n_comment': fields[3]
        }
    },
    'part': function(fields) {
        return {
            'p_partkey': parseInt(fields[0]),
            "p_name": fields[1],
            "p_mfgr": fields[2],
            "p_brand": fields[3],
            "p_type": fields[4],
            "p_size": parseInt(fields[5]),
            "p_container": fields[6],
            "p_retailprice": parseFloat(fields[7]),
            "p_comment": fields[8],
        }
    },
    'supplier': function (fields) {
        return {
            's_suppkey': parseInt(fields[0]),
            "s_name": fields[1],
            "s_address": fields[2],
            "s_nationkey": parseInt(fields[3]),
            "s_phone": fields[4],
            "s_acctbal": parseFloat(fields[5]),
            "s_comment": fields[6],
        }
    },
    'partsupp': function (fields) {
        return {
            'ps_partkey': parseInt(fields[0]),
            "ps_suppkey": parseInt(fields[1]),
            "ps_availqty": parseInt(fields[2]),
            "ps_supplycost": parseFloat(fields[3]),
            "ps_comment": fields[4],
        }
    },
    'customer': function (fields) {
        return {
            'c_custkey': parseInt(fields[0]),
            "c_name": fields[1],
            "c_address": fields[2],
            "c_nationkey": parseInt(fields[3]),
            "c_phone": fields[4],
            "c_acctbal": parseFloat(fields[5]),
            "c_mktsegment": fields[6],
            "c_comment": fields[7],
        }
    },
    'orders': function (fields) {
        return {
            'o_orderkey': parseInt(fields[0]),
            "o_custkey": parseInt(fields[1]),
            "o_orderstatus": fields[2],
            "o_totalprice": parseFloat(fields[3]),
            "o_orderdate": new Date(fields[4]),
            "o_orderpriority": fields[5],
            "o_clerk": fields[6],
            "o_shippriority": parseInt(fields[7]),
            "o_comment": fields[8],
        }
    },
    'lineitem': function(fields) {
        return {
            'l_orderkey': parseInt(fields[0]),
            "l_partkey": parseInt(fields[1]),
            "l_suppkey": parseInt(fields[2]),
            "l_linenumber": parseInt(fields[3]),

            "l_quantity": parseFloat(fields[4]),
            "l_extendedprice": parseFloat(fields[5]),
            "l_discount": parseFloat(fields[6]),
            "l_tax": parseFloat(fields[7]),

            "l_returnflag": fields[8],
            "l_linestatus": fields[9],

            "l_shipdate": new Date(fields[10]),
            "l_commitdate": new Date(fields[11]),
            "l_receiptdate": new Date(fields[12]),

            "l_shipinstruct": fields[13],
            "l_shipmode": fields[14],
            "l_comment": fields[15],
        }
    },

}

function make_parse_table_csv(line_parser) {
    return function(path, table) {
        const full_path = path + '/' + table + '.tbl';
        const readInterface = makeInterface(full_path);

        const result = [];
        var i = 0;
        function parseLineWithSchema(line) {
            const fields = line.split("|")
            row = line_parser(fields);
            result[i++]=row;
        }

        readInterface.on('line', parseLineWithSchema);

        return new Promise(resolve => {
            readInterface.on('close', () =>  { resolve(result); });
        });
    }
}



async function load(path) {
    const tpch = {};
    for(var table of tpch_schema) {
        // const loader = (table == 'lineitem') ? parse_table_csv : parse_table_json;
        const line_parser = parserFunctions[table];
        const loader = make_parse_table_csv(line_parser);
        tpch[table] = await loader(path, table);
    }
    return tpch;
}

module.exports.load = load;


/*

TPC-H Tables

-- lineitem --

l_orderkey    BIGINT not null
l_partkey     BIGINT not null
l_suppkey     BIGINT not null
l_linenumber  BIGINT not null
l_quantity    DOUBLE PRECISION not null
l_extendedprice  DOUBLE PRECISION not null
l_discount    DOUBLE PRECISION not null
l_tax         DOUBLE PRECISION not null
l_returnflag  CHAR(1) not null
l_linestatus  CHAR(1) not null
l_shipdate    DATE not null
l_commitdate  DATE not null
l_receiptdate DATE not null
l_shipinstruct CHAR(25) not null
l_shipmode     CHAR(10) not null
l_comment      VARCHAR(44) not nul
*/
