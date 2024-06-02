// TODO: Remove unused rules
module.exports = grammar({
  name: 'fearless',
  inline: $ => [$.packagePathDot],

  rules: {
    source_file: $ => seq($.package, repeat($.alias), repeat($.topDec)),

    package: $ => seq('package ', field('name', $.packagePath), '\n'),

    // FIXME: Completely fails with one.two.Tree, maybe make (pkgName.)+ the default, then add the last one in $.package?
    packagePath: $ => prec.left(seq(optional(repeat(seq($._pkgName, '.'))), $._pkgName)),
    packagePathDot: $ => alias(seq($.packagePath, '.'), $.packagePath),

    _pkgName: $ => seq($._idLow, repeat($._idChar)),

    alias: $ => seq('alias ', field('from', $.type), ' as ', field('to', $.typeName), ','),
    type: $ => seq(
      optional(field('package', $.packagePathDot)),
      field('name', $.typeName),
      optional(field('generic', $.genericList))),
    typeName: $ => seq($._idUp, repeat($._idChar), repeat('\'')),

    // TODO: Generics, reuse mGen
    genericList: $ => 'todo!',

    fullCN: $ => choice(
      // TODO: Have some sort of packagePath for the bit in []: [some.cool].package
      seq(optional(field('package', $.packagePathDot)), field('type', alias($.typeName, $.type))),
      // TODO: The rest of FullCN like FStringMulti, etc
    ),

    // mGen   : | OS (genDecl (Comma genDecl)*)? CS;
    mGen: $ => seq('[', optional(seq($._genDecl, repeat(seq(',', $._genDecl)))) ,']'),

    // genDecl : t Colon (mdf (Comma mdf)*) | t;
    // TODO: Is it safe to use optional instead of the  t <stuff> | t
    // TODO: Make mdf a child of fullcn or smth like that.
    _genDecl: $ => prec.left(seq(
      $._t,
      optional(
        seq(
          ':',
          optional($.mdf),
          repeat(
            seq(',', optional($.mdf))))))),
    // TODO: Ask what these shortnames mean and see if i can give them more descriptive names
    //       It'd make writing queries and such easier.
    _t: $ => seq(optional($.mdf), $.fullCN, optional($.mGen)),
    mdf: $ => choice('mut' , 'readH' , 'mutH' , 'read/imm' , 'read' , 'iso' , 'recMdf' , 'imm'),

    // TODO: topDec
    topDec: $ => 'topDec',

    _idLow: $ => choice(/_*[a-z]/, /_+[0-9]/),
    _idUp: $ => /_*[A-Z]/,
    _idChar: $ => /[a-zA-Z_0-9]/,
    _fIdLow: $ => seq($._idLow, repeat($._idChar), repeat('\'')),
  }
});
