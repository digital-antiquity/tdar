// webpack v4
var webpack = require("webpack");
const path = require('path');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
var DuplicatePackageCheckerPlugin = require("duplicate-package-checker-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

const merge = require('webpack-merge');

const common = require('./webpack.base.config.js');

module.exports = merge(common, {
    mode: 'development',
    devtool: 'eval-source-map',
    module: {rules:[
    ]},
    plugins: [
    //    new BundleAnalyzerPlugin()
    ]
});