const path = require('path');

//const ExtractTextPlugin = require("extract-text-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const CopyWebpackPlugin = require('copy-webpack-plugin')
const Assets = require('./assets');

const cssFiles = Assets[0].map(asset => {
    return {
        from: path.resolve(__dirname, `./${asset}`),
        to: path.resolve(__dirname, './src/main/webapp/components/')
      };
    });

const nodeModules = Assets[1].map(asset => {
    return {
        from: path.resolve(__dirname, `./${asset.path}`),
        to: path.resolve(__dirname, `./src/main/webapp/components/${asset.name}`),
        toType: 'dir'
      };
    });
    
module.exports = {
  entry: './src/main/webapp/js/index.js',
  output: {
    filename: 'bundle.js',
    path: path.resolve(__dirname, './src/main/webapp/components')
  },
  optimization: {
      splitChunks: {
        cacheGroups: {
          styles: {
            name: 'styles',
            test: /\.css$/,
            chunks: 'all',
            enforce: true
          }
        }
      }
    },
  module: {
      rules: [
          {
              test: /\.css$/,
              use: [
                MiniCssExtractPlugin.loader,
                "css-loader"
              ]
          }
        ]
    },
    plugins: [
        new CopyWebpackPlugin(
                cssFiles 
          ),
          new CopyWebpackPlugin(
                 nodeModules 
            )
    ]
};