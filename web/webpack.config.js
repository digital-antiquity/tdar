const path = require('path');

// const ExtractTextPlugin = require("extract-text-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const CopyWebpackPlugin = require('copy-webpack-plugin')

// Assets is a list of files that were previously Bower-managed dependencies.
// They need to be copied into the webapp/components directory so they can be served in the WAR file.
const Assets = require('./assets');
const staticAssets = require('./src/main/webapp/js/webpack/static-assets.js');

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
     
     staticAssets: staticAssets
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
              test: /jquery-file-upload/,
              loader: 'imports?jQuery=jquery,$=jquery,this=>window'
          },
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
        new CopyWebpackPlugin(
                cssFiles 
          ),
          new CopyWebpackPlugin(
                 nodeModules 
            ),
            new MiniCssExtractPlugin()
    ]
};