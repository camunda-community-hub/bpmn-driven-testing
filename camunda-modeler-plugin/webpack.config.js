const path = require("path");
const webpack = require("webpack");

module.exports = {
  mode: "development",
  entry: "./main/index.js",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "client.js"
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: "babel-loader",
          options: {
            presets: ["@babel/preset-react"]
          }
        }
      },
      {
        test: /\.css$/,
        use: ["style-loader", "css-loader" ]
      },
      {
        test: /\.(png|woff|woff2|eot|ttf|svg)$/,
        loader: "url-loader?limit=100000"
      }
    ]
  },
  devtool: "cheap-module-source-map",
  plugins: [
    new webpack.DefinePlugin({
        '__REACT_DEVTOOLS_GLOBAL_HOOK__': '({ isDisabled: true })'
    }),
  ]
};
