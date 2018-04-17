const path = require('path');

module.exports = {
  entry: './src/main/webapp/js/index.js',
  output: {
    filename: 'bundle.js',
    path: path.resolve(__dirname, './src/main/webapp/components')
  }
};