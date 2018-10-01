// webpack v4
var webpack = require("webpack");
const path = require('path');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
var DuplicatePackageCheckerPlugin = require("duplicate-package-checker-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const OptimizeCSSAssetsPlugin = require("optimize-css-assets-webpack-plugin");
const MinifyPlugin = require('babel-minify-webpack-plugin');

const merge = require('webpack-merge');

const common = require('./webpack.base.config.js');

module.exports = merge(common, {
    mode: 'production',
    module: {rules:[
        {
               test: /\.js$/,
               use: {
                 loader: 'babel-loader',
                 options: {
                   presets: ['env']
                 }
               }
             },
                {
                  test: /\.(sa|sc|c)ss$/,
                  use: [
                    MiniCssExtractPlugin.loader,
                    'css-loader',
                    'sass-loader',
                  ],
                }
    ]},
    plugins: [
    //    new BundleAnalyzerPlugin()
    new MinifyPlugin()
    ],
    optimization: {
        minimizer: [new OptimizeCSSAssetsPlugin({})
        ]
    }
});

/*
            new UglifyJsPlugin({uglifyOptions: {
    warnings: false,
    parse: {},
    compress: {},
    mangle: true, // Note `mangle.properties` is `false` by default.
    output: null,
    toplevel: false,
    nameCache: null,
    ie8: false,
    keep_fnames: false,
  }
})*/
