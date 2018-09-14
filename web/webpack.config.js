// webpack v4
var webpack = require("webpack");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const devMode = false; //process.env.NODE_ENV !== 'production'
const CopyWebpackPlugin = require('copy-webpack-plugin')

const path = require('path');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

// webpack.autoProvidejQuery();
module.exports = {
    //mode: 'production',
    mode : 'development',

    context : __dirname,
    //devtool: 'eval-source-map',
    entry : {
        // jquery : ['jquery'],
        main : './src/main/webapp/js/webpack/tdar-entry-point.js'
    },
    output : {
        path : path.resolve(__dirname, "./src/main/webapp/dist/"),
        filename : 'bundle.js',
        chunkFilename : "[id].bundle.js"
    },

    // externals: ['axios'], this is a way to use an extenral version of jquery, whatever is referecned here should probably be a 'dev' dependency too
    module : {
        rules : [ {
            test : require.resolve('jquery'),
            use : [ {
                loader : 'expose-loader',
                options : 'window.jQuery'
            }, 
            {
                loader : 'expose-loader',
                options : '$'
            }, 
            {
                loader : 'expose-loader',
                options : "global.jQuery",
            }, 
            {
                loader : 'expose-loader',
                options : "$j",
            }
            ]
        },
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
    },
    optimization : {
        // runtimeChunk: false,
        splitChunks : {
            cacheGroups : {
                commons : {
                    test : /[\\/]node_modules[\\/]/,
                    name : 'vendors',
                    chunks : 'all',
                    minChunks : 2
                }
            }
        }
    },

    plugins : [
    //new BundleAnalyzerPlugin(),
        new MiniCssExtractPlugin({
            // Options similar to the same options in webpackOptions.output
            // both options are optional
            filename: devMode ? '[name].css' : '[name].css',
            chunkFilename: devMode ? '[id].css' : '[id].[hash].css',
          }),
          new CopyWebpackPlugin([
            {
              from: 'node_modules/tdar-autocomplete/template/autocomplete.html',
              to: 'templates/',
              toType: 'dir'
            }
          ], {}),
        new webpack.ProvidePlugin({
        $ : "jquery",
        $j : "jquery",
        jQuery : "jquery",
        'window.jQuery' : 'jquery',
        'window.$': 'jquery',
        'global.jQuery' : 'jquery',
        //'jquery.ui.widget' : 'blueimp-file-upload/js/vendor/jquery.ui.widget',
        
        //axios : "Axios",
        c3 : "c3",
        d3 : "d3",
        TDAR : "JS/tdar.master",
        LatLon : 'JS/latLongUtil-1.0',
        L : 'leaflet'
    }) ],

    // this seemed to make a difference:
    // https://github.com/webpack/webpack.js.org/issues/63
    resolve : {
        alias : {
            'leaflet':'Components/leaflet',
            
            "jquery-ui" : path.join(__dirname, "src/main/webapp/includes/jquery-ui-1.11.4.custom/jquery-ui.js"),
            modules : path.join(__dirname, "node_modules"),
            JS : path.resolve(__dirname, 'src/main/webapp/js/'),
            Components : path.resolve(__dirname, 'src/main/webapp/js_includes/components/'),
            Includes : path.resolve(__dirname, 'src/main/webapp/js_includes/includes/')
        }

    // alias: {
    // jquery: "jquery/dist/jquery"
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
// return {
// from: path.resolve(__dirname, `./${asset}`),
// to: path.resolve(__dirname, './src/main/webpack/assets/')
// };
// });
//
// const nodeModules = Assets[1].map(asset => {
// return {
// from: path.resolve(__dirname, `./${asset.path}`),
// to: path.resolve(__dirname, `./src/main/webapp/webpack/${asset.name}`),
// toType: 'dir'
// };
// });
//
// module.exports = {
// entry: {
// // This is the main bundle entry point. All of the tdar components are listed in here and will be bundled as one file.
// bundle: './src/main/webapp/js/webpack/index',
//
// // All of the angular integration files will be bundled into a separate file.
// angular : './src/main/webapp/js/webpack/angular-assets',
//
// staticAssets: './static-assets.js'
// },
//
// // One one output entry can be specified. The [name] will be replaced with what the name of the entry point was.
// output: {
// filename: '[name].js',
// path: path.resolve(__dirname, './src/main/webapp/webpack/')
// },
//
// // This is supposed to be for the CSS files that get bundled to be loaded into one file.
// optimization: {
// splitChunks: {
// cacheGroups: {
// // default: false,
// // commons: {
// // test: /[\\/]node_modules[\\/]/,
// // name: 'vendor_app',
// // chunks: 'all',
// // minChunks: 2
// // },
// styles: {
// name: 'styles',
// test: /\.css$/,
// chunks: 'all',
// enforce: true
// },
// }
// }
// },
//
// module: {
// rules: [
//
// {
// test: /\.css$/,
// use: [
// MiniCssExtractPlugin.loader,
// "css-loader"
// ]
// },
// {
// test: /\.(png|svg|jpg|gif)$/,
// use: [
// 'file-loader'
// ]
// },
// {
// test: require.resolve('jquery-validation'),
// loader: 'imports-loader',
// query: 'jQuery=jquery,$=jquery',
// }
// ]
// },
//
// // This plugin forces the files defined in the Assets.js file to be copied to their destination.
// plugins: [
// new webpack.ProvidePlugin({
// $: "jquery",
// jQuery: "jquery",
// 'window.$': 'jquery',
// 'window.jQuery': 'jquery',
// d3:"d3",
// c3:"c3"
// }),
// new CopyWebpackPlugin(
// cssFiles
// ),
// new CopyWebpackPlugin(
// nodeModules
// ),
// new MiniCssExtractPlugin()
// ]
// };
