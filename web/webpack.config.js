// webpack v4
console.log("NOP!");
var webpack = require("webpack");
const path = require('path');
module.exports = {
    context: __dirname,
    
  entry: { 
      bundle: './src/main/webapp/js/webpack/index.js'
  },
  output: {
        path: path.resolve(__dirname, "./src/main/webapp/dist/"),
        filename: '[name].js',
        chunkFilename: "[id].bundle.js"
  },
  module: {
    rules: [
        {
            test: /jquery-validation/,
            loader: 'imports-loader?jQuery=jquery,$=jquery,this=>window'
        }
    ],
  },
  optimization: {
    // minimizer: [
    //   new UglifyJSPlugin({
    //     sourceMap: true,
    //     uglifyOptions: {
    //       compress: {
    //         inline: false
    //       }
    //     }
    //   })     
    // ],
    runtimeChunk: false,
    splitChunks: {
      cacheGroups: {
        default: false,
        commons: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendor_app',
          chunks: 'all',
          minChunks: 2
        }
      }
    }
  },  plugins: [
     new webpack.ProvidePlugin({
         $: "jquery",
         jQuery: "jquery",
         c3: "c3",
         d3: "d3"
     })
  ]
};
