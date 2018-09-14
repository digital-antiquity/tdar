// webpack v4
var webpack = require("webpack");
const path = require('path');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const devMode = process.env.NODE_ENV !== 'production'


// webpack.autoProvidejQuery();
module.exports = {
    mode: 'development',
    context: __dirname,
    //devtool: 'eval-source-map',
    entry: { 
     // jquery : ['jquery'],
      main: './src/main/webapp/js/webpack/tdar-entry-point.js' 
    },
    output: {
      path: path.resolve(__dirname, "./src/main/webapp/dist/"),
      filename: '[name].bundle.js',
      chunkFilename: "[id].bundle.js"
    },
  
    module: {
        rules: [
            {
                test: /\.(sa|sc|c)ss$/,
                use: [
                  devMode ? 'style-loader' : MiniCssExtractPlugin.loader,
                  'css-loader',
                  'sass-loader',
                ],
              },

              {
                test: /\.(png|jpg|gif|svg|eot|ttf|woff|woff2)$/,
                loader: 'url-loader',
                options: {
                  limit: 10
                }
              }
        ]
    }
  ,
  
  plugins: [
      /**Don't need to use the analyzer for tests**/
     //new BundleAnalyzerPlugin(),
     new webpack.ProvidePlugin({
         $: "jquery",
         $j : "jquery",
         jQuery: "jquery",
         axios:"Axios",
         jquery: "jquery",
         c3: "c3",
         d3: "d3",
         
         TDAR : 'JS/tdar.master',
         Vue : 'vue',
         LatLon: path.resolve(__dirname,'src/main/webapp/js/latLongUtil-1.0'),
         L : 'leaflet'

     })
  ],
 
  // this seemed to make a difference: 
  // https://github.com/webpack/webpack.js.org/issues/63
  resolve: {
         alias: {
            'leaflet':'Components/leaflet',
            "jquery-ui" : path.join(__dirname, "src/main/webapp/includes/jquery-ui-1.11.4.custom/jquery-ui.js"),
            modules : path.join(__dirname, "node_modules"),
            JS : path.resolve(__dirname, 'src/main/webapp/js/'),
            Components : path.resolve(__dirname, 'src/main/webapp/js_includes/components/'),
            Includes : path.resolve(__dirname, 'src/main/webapp/js_includes/includes/')
         }
  }
};
