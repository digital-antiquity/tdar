// webpack v4
var webpack = require("webpack");
const path = require('path');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

// webpack.autoProvidejQuery();
module.exports = {
        mode: 'development',
    context: __dirname,
    //devtool: 'eval-source-map',
  entry: { 
     // jquery : ['jquery'],
      main: './src/main/webapp/dist/main.bundle.js' 
  },
  output: {
      path: path.resolve(__dirname, "./src/main/webapp/dist/"),
      filename: '[name].bundle.js',
      chunkFilename: "[id].bundle.js"
  },
  
  // externals: ['axios'], this is a way to use an extenral version of jquery, whatever is referecned here should probably be a 'dev' dependency too
  module: {
    rules: [
        /*{
            test: require.resolve("jquery-validation"),
            use: "imports-loader?$=jquery,this=>window,define=>false"
        }*/
    ]
  }
  ,
  /**
  optimization: {
    runtimeChunk: false,

    splitChunks: {
        chunks: a
      cacheGroups: {
        //default: false,
        commons: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendor_app',
          chunks: 'all',
          minChunks: 2
        }
      }
    }
  },**/
  
  plugins: [
     new BundleAnalyzerPlugin(),
     new webpack.ProvidePlugin({
         $: "jquery",
         $j : "jquery",
         jQuery: "jquery",
         axios:"Axios",
        // jquery: "jquery",
         c3: "c3",
         d3: "d3",
         // TDAR : path.resolve(__dirname,'src/main/webapp/js/tdar.core'),
         // axios:"axios",
         Vue : 'vue',
         // Vue : 'Vue'
         LatLon: path.resolve(__dirname,'src/main/webapp/js/latLongUtil-1.0')
     })
  ],
 
  // this seemed to make a difference: 
  // https://github.com/webpack/webpack.js.org/issues/63
  resolve: {
        // alias: {
        //     jquery: "jquery/dist/jquery"
        // }
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