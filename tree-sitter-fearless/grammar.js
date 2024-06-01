module.exports = grammar({
  name: 'fearless',

  rules: {
    source_file: $ => seq($.package, repeat($.alias), repeat($.topDec)),
    // TODO: Alias
    alias: $ => 'alias',
    // TODO: topDec
    topDec: $ => 'topDec',
    // XXX: Do we want something like tree-sitter-java, where each path of the
    //      package is its own scoped identifier?
    package: $ => seq('package ', $.packageName, '\n'),
    packageName: $ => seq(repeat(seq($._px, '.')), $._px),
    _px: $ => seq($._idLow, repeat($._idChar)),
    _idLow: $ => choice(/_*[a-z]/, /_+[0-9]/),
    _idUp: $ => /_*[A-Z]/,
    _idChar: $ => /[a-zA-Z_0-9]/,
  }
});
