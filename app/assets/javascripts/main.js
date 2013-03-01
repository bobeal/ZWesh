require.config({
  baseUrl: 'assets/javascripts/lib'
, paths: { app: '../app'
         , jquery: 'jquery.min'
         , gridster: 'jquery.gridster.min'
         }
, shim: { 'gridster': ['jquery'] }
})

require(['jquery', 'gridster'], function($) {

  $('.gridster ul').gridster({
    widget_margins: [10, 10]
  , widget_base_dimensions: [140, 140]
  })

})
