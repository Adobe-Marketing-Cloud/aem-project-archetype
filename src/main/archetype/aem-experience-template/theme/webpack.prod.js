/*
Copyright 2020 Adobe
All Rights Reserved.

NOTICE: Adobe permits you to use, modify, and distribute this file in
accordance with the terms of the Adobe license agreement accompanying
it.
*/

const merge                   = require('webpack-merge');
const OptimizeCSSAssetsPlugin = require("optimize-css-assets-webpack-plugin");
const TerserPlugin            = require('terser-webpack-plugin');
const common                  = require('./webpack.common.js');

module.exports = merge(common, {
   mode: 'production',
   optimization: {
      minimizer: [
          new TerserPlugin(),
          new OptimizeCSSAssetsPlugin({
              cssProcessorPluginOptions: {
                  cssProcessor: require('cssnano'),
                  preset: ['default', {
                      calc: true,
                      convertValues: true,
                      discardComments: {
                          removeAll: true
                      },
                      discardDuplicates: true,
                      discardEmpty: true,
                      mergeRules: true,
                      normalizeCharset: true,
                      reduceInitial: true, // This is since IE11 does not support the value Initial
                      svgo: true
                  }],
              },
              canPrint: false
          })
      ]
   },
   devtool: 'none',
   performance: {hints: false}
});
