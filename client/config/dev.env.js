'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  WS_URL: '"ws://localhost:8080/game"',
  HTTP_URL: '"http://localhost:8080"',
})
