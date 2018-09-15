const requireAll = (requireContext) => { requireContext.keys().map(requireContext); };
 var r  = require.context('./src/test/frontend/spec/', false, /\.js$/);
 
 console.log(r.keys())
 
requireAll(r);
