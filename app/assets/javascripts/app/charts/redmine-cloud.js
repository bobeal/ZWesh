define(['jquery', 'd3', 'd3.layout.cloud'], function($, d3) {
  var me = {}
    , draw
    , url = '/getDescWordNumber'
    , chart
    , fill
    , div

  fill = d3.scale.category20()

  draw = function draw(words) {
    d3.select(div).append('svg')
        .attr('width', 400)
        .attr('height', 400)
      .append('g')
        .attr('transform', 'translate(150,150)')
      .selectAll('text')
        .data(words)
      .enter().append('text')
        .style('font-size', function(d) { return d.size + 'px' })
        .style('font-family', 'Impact')
        .style('fill', function(d, i) { return fill(i) })
        .attr('text-anchor', 'middle')
        .attr('transform', function(d) {
          return 'translate(' + [d.x, d.y] + ')rotate(' + d.rotate + ')'
        })
        .text(function(d) { return d.text })
    // FIXME: super hackish
    $(div).css('overflow', 'hidden').find('svg').css('zoom', 1.5)
  }

  chart = function(words) {
    div = $('<div></div>').get(0)
    d3.layout.cloud().size([400, 400])
      .words(words)
      .rotate(function() { return ~~(Math.random() * 5) * 30 - 60 })
      .font('Impact')
      .fontSize(function(d) { return d.size })
      .on('end', draw)
      .start()
    return div
  }

  return $.get(url).then(chart)
})
