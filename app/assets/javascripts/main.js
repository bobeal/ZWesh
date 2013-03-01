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

    // load widgets
    var gridster = $(".gridster ul").gridster().data("gridster");
    var widgets = [];

    // load capdemat widget
    var capdematWidget = (function() {
      var widget = null;
      return {
        init: function() {
          $.ajax("/capdemat/dashboard").then(function(html) {
            widget = gridster.add_widget("<li></li>", 3, 4);
            widget.html(html);
          });
        },
        update: function() {
          $.ajax("/capdemat/dashboard").then(function(html) {
            widget.html(html);
          });
        }
      }
    })();

    // register widgets
    widgets.push(capdematWidget);

    function initWidgets() {
      widgets.forEach(function(widget) { widget.init(); });
    }

    function updateWidgets() {
      widgets.forEach(function(widget) { widget.update(); });
    }

    function widgetDaemon() {
      updateWidgets();
      setTimeout(widgetDaemon, 2000);
    }

    initWidgets();
    widgetDaemon();
})