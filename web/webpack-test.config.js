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
     })
  ],
 
  // this seemed to make a difference: 
  // https://github.com/webpack/webpack.js.org/issues/63
  resolve: {
         alias: {
             JS : path.resolve(__dirname,'src/main/webapp/js/')
         }
  }
};

//
//
// const path = require('path');
//
// // const ExtractTextPlugin = require("extract-text-webpack-plugin");
// const MiniCssExtractPlugin = require("mini-css-extract-plugin");
// const CopyWebpackPlugin = require('copy-webpack-plugin');
// const webpack = require("webpack");
//
// // Assets is a list of files that were previously Bower-managed dependencies.
// // They need to be copied into the webapp/components directory so they can be served in the WAR file.
// const Assets = require('./assets');
//
// // This will parse out
// const cssFiles = Assets[0].map(asset => {
//     return {
//         from: path.resolve(__dirname, `./${asset}`),
//         to: path.resolve(__dirname, './src/main/webpack/assets/')
//       };
// });
//
// const nodeModules = Assets[1].map(asset => {
//     return {
//         from: path.resolve(__dirname, `./${asset.path}`),
//         to: path.resolve(__dirname, `./src/main/webapp/webpack/${asset.name}`),
//         toType: 'dir'
//       };
// });
//
// module.exports = {
//   entry: {
//       // This is the main bundle entry point. All of the tdar components are listed in here and will be bundled as one file.
//       bundle: './src/main/webapp/js/webpack/index',
//
//       // All of the angular integration files will be bundled into a separate file.
//       angular : './src/main/webapp/js/webpack/angular-assets',
//
//      staticAssets: './static-assets.js'
//   },
//
//   // One one output entry can be specified. The [name] will be replaced with what the name of the entry point was.
//   output: {
//     filename: '[name].js',
//     path: path.resolve(__dirname, './src/main/webapp/webpack/')
//   },
//
//   // This is supposed to be for the CSS files that get bundled to be loaded into one file.
//   optimization: {
//       splitChunks: {
//         cacheGroups: {
//             // default: false,
//             // commons: {
//             //   test: /[\\/]node_modules[\\/]/,
//             //   name: 'vendor_app',
//             //   chunks: 'all',
//             //   minChunks: 2
//             // },
//           styles: {
//             name: 'styles',
//             test: /\.css$/,
//             chunks: 'all',
//             enforce: true
//           },
//         }
//       }
//   },
//
//   module: {
//       rules: [
//
//           {
//               test: /\.css$/,
//               use: [
//                 MiniCssExtractPlugin.loader,
//                 "css-loader"
//               ]
//           },
//           {
//                test: /\.(png|svg|jpg|gif)$/,
//                use: [
//                  'file-loader'
//                ]
//          },
//          {
//                test: require.resolve('jquery-validation'),
//                loader: 'imports-loader',
//                query: 'jQuery=jquery,$=jquery',
//          }
//         ]
//     },
//
//     // This plugin forces the files defined in the Assets.js file to be copied to their destination.
//     plugins: [
//         new webpack.ProvidePlugin({
//             $: "jquery",
//             jQuery: "jquery",
//             'window.$': 'jquery',
//             'window.jQuery': 'jquery',
//             d3:"d3",
//             c3:"c3"
//         }),
//         new CopyWebpackPlugin(
//                 cssFiles
//           ),
//         new CopyWebpackPlugin(
//              nodeModules
//         ),
//         new MiniCssExtractPlugin()
//     ]
// };