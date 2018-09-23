// webpack v4
var webpack = require("webpack");
const path = require('path');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
const devMode = process.env.NODE_ENV !== 'production'
var DuplicatePackageCheckerPlugin = require("duplicate-package-checker-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

const merge = require('webpack-merge');

const common = require('./webpack.base.config.js');

module.exports = merge(common, {
    mode: 'production',
    module: {rules:[
                {
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
                }
    ]},
    plugins: [
    //    new BundleAnalyzerPlugin()
    ]
});