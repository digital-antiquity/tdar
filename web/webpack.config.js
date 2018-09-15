// webpack v4
var webpack = require("webpack");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const devMode = false; //process.env.NODE_ENV !== 'production'
const CopyWebpackPlugin = require('copy-webpack-plugin')
var DuplicatePackageCheckerPlugin = require("duplicate-package-checker-webpack-plugin");

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
        new DuplicatePackageCheckerPlugin(),
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
            'leaflet':'leaflet',
            
            "jquery-ui" : path.join(__dirname, "src/main/webapp/includes/jquery-ui-1.11.4.custom/jquery-ui.js"),
            modules : path.join(__dirname, "node_modules"),
            JS : path.resolve(__dirname, 'src/main/webapp/js/'),
            Components : path.resolve(__dirname, 'src/main/webapp/components/'),
            Includes : path.resolve(__dirname, 'src/main/webapp/includes/')
        }

    // alias: {
    // jquery: "jquery/dist/jquery"
    // }
    }
};
