/*
Copyright 2020 Adobe
All Rights Reserved.

NOTICE: Adobe permits you to use, modify, and distribute this file in
accordance with the terms of the Adobe license agreement accompanying
it.
*/

'use strict';

const path                    = require('path');
const webpack                 = require('webpack');
const MiniCssExtractPlugin    = require("mini-css-extract-plugin");
const TSConfigPathsPlugin     = require('tsconfig-paths-webpack-plugin');
const TSLintPlugin            = require('tslint-webpack-plugin');
const CopyWebpackPlugin       = require('copy-webpack-plugin');
const { CleanWebpackPlugin }  = require('clean-webpack-plugin');

const SOURCE_ROOT = __dirname + '/src';

module.exports = {
        resolve: {
            extensions: ['.js', '.ts'],
            plugins: [new TSConfigPathsPlugin({
                configFile: "./tsconfig.json"
            })]
        },
        entry: {
            site: SOURCE_ROOT + '/main.ts'
        },
        output: {
            filename: 'js/theme.js',
            path: path.resolve(__dirname, 'dist')
        },
        optimization: {
            splitChunks: {
                   chunks: 'all'
                 }
        },
        module: {
            rules: [
                {
                    test: /\.tsx?$/,
                    exclude: [
                        /(node_modules)/
                    ],
                    use: [
                        {
                            loader: 'ts-loader'
                        },
                        {
                            loader: 'webpack-import-glob-loader',
                            options: {
                                url: false
                            }
                        }
                    ]
                },
                {
                    test: /\.scss$/,
                    use: [
                        MiniCssExtractPlugin.loader,
                        {
                            loader: "css-loader",
                            options: {
                                url: false
                            }
                        },
                        {
                            loader: 'postcss-loader',
                            options: {
                                plugins() {
                                    return [
                                        require('autoprefixer')
                                    ];
                                }
                            }
                        },
                        {
                            loader: "sass-loader",
                            options: {
                                url: false
                            }
                        },
                        {
                            loader: "webpack-import-glob-loader",
                            options: {
                                url: false
                            }
                        }
                    ]
                },
                {
                    test: /\.(ico|jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2)(\?.*)?$/,
                    use: {
                      loader: 'file-loader',
                      options: {
                        name: '[path][name].[ext]'
                      }
                    }
                },
            ]
        },
        plugins: [
            new CleanWebpackPlugin(),
            new webpack.NoEmitOnErrorsPlugin(),
            new MiniCssExtractPlugin({
                filename: 'css/theme.css',
            }),
            new CopyWebpackPlugin([
                { from: path.resolve(__dirname, SOURCE_ROOT + '/resources'), to: './resources' }
            ]),
            new webpack.optimize.LimitChunkCountPlugin({
                maxChunks: 1
            })
        ],
        stats: {
            assetsSort: "chunks",
            builtAt: true,
            children: false,
            chunkGroups: true,
            chunkOrigins: true,
            colors: false,
            errors: true,
            errorDetails: true,
            env: true,
            modules: false,
            performance: true,
            providedExports: false,
            source: false,
            warnings: true
        }
};
