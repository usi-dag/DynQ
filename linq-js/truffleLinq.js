
const API = process.env.DYNQ_LANGUAGE || "TruffleLINQ";

exports.API = () => Polyglot.eval(API, "API")
exports.isJavaScriptSpecific = () => API == 'TruffleLINQ_JS'
