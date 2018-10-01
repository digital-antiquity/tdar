// webpack v4
var webpack = require("webpack");
const path = require('path');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
const devMode = process.env.NODE_ENV !== 'production'
var DuplicatePackageCheckerPlugin = require("duplicate-package-checker-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const MinifyPlugin = require('babel-minify-webpack-plugin');

const merge = require('webpack-merge');

const common = require('./webpack.base.config.js');

module.exports = merge(common, {
    mode: 'development',
    module: {rules:[
        // {
        //        test: /\.js$/,
        //        use: {
        //          loader: 'babel-loader',
        //          options: {
        //            presets: ['env']
        //          }
        //        }
        //      }

    ]},
    devtool: 'eval-source-map',
    plugins: [    /* new MinifyPlugin() */    ]
});