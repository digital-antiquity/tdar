const path = require('path');

// const ExtractTextPlugin = require("extract-text-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const CopyWebpackPlugin = require('copy-webpack-plugin');
const webpack = require("webpack");

// Assets is a list of files that were previously Bower-managed dependencies.
// They need to be copied into the webapp/components directory so they can be served in the WAR file.
const Assets = require('./assets');

// This will parse out
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
  entry: {
      // This is the main bundle entry point. All of the tdar components are listed in here and will be bundled as one file.
      bundle: './src/main/webapp/js/webpack/index',
      
      // All of the angular integration files will be bundled into a separate file.
      angular : './src/main/webapp/js/webpack/angular-assets',
     
     staticAssets: './static-assets.js'
  },
  
  // One one output entry can be specified. The [name] will be replaced with what the name of the entry point was.
  output: {
    filename: '[name].js',
    path: path.resolve(__dirname, './src/main/webapp/components')
  },
  
  // This is supposed to be for the CSS files that get bundled to be loaded into one file.
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
          },
          {
               test: /\.(png|svg|jpg|gif)$/,
               use: [
                 'file-loader'
               ]
         }
        ]
    },
    
    // This plugin forces the files defined in the Assets.js file to be copied to their destination.
    plugins: [
        new webpack.ProvidePlugin({
            $: "jquery",
            jQuery: "jquery"
        }),
        new CopyWebpackPlugin(
                cssFiles 
          ),
        new CopyWebpackPlugin(
             nodeModules 
        ),
        new MiniCssExtractPlugin()
    ]
};