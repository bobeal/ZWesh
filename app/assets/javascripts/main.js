require.config({
  baseUrl: 'assets/javascripts/lib'
, paths: { app: '../app'
         , jquery: 'jquery.min'
         , gridster: 'jquery.gridster.min'
         , d3: 'd3.v3.min'
         }
, shim: { 'gridster': ['jquery']
        , 'd3': { exports: 'd3' }
        , 'd3.layout.cloud': ['d3']
        }
})

require(['jquery', 'app/charts/redmine-cloud', 'gridster'], function($, redmine) {

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
            widget = gridster.add_widget("<li></li>", 3, 5);
            widget.html(html);
          });
        },
        update: function() {
          $.ajax("/capdemat/dashboard").then(function(html) {
            if (widget) {
              widget.html(html)
            }
          });
        }
      }
    })();

    var redmineWidget = (function() {
      var widget
      return {
        init: function() {
          redmine.done(function(div) {
            widget = gridster.add_widget('<li></li>', 3, 3)
            widget.html(div)
          })
        },
        update: function() {
          redmine.done(function(div) {
            widget.html(div)
          })
        }
      }
    })()

    // register widgets
    widgets.push(capdematWidget);
    widgets.push(redmineWidget)

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
