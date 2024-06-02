// TODO: Remove unused rules
module.exports = grammar({
  name: 'fearless',

  rules: {
    source_file: $ => seq($.package, repeat($.alias), repeat($.topDec)),

    // _fullPkgName need to be its own rule or we get a duplicate filed for some reason.
    package: $ => seq('package', ' ', field('name', alias($._fullPkgName, $.packagePath), '\n')),
    _fullPkgName: $ => seq($._pkgName, repeat(seq('.', $._pkgName))),

    // prec.left does NOT work.
    packagePath: $ => prec.right(repeat1(seq($._pkgName, '.'))),
    _pkgName: $ => seq($._idLow, repeat($._idChar)),

    alias: $ => seq('alias', ' ', field('from', $.concreteType), ' ', 'as', ' ', field('to', $.typeName), ','),

    concreteType: $ => seq(
      optional(field('package', $.packagePath)),
      field('name', $.typeName),
      optional(field('generic', $.concreteTypes))),

    typeName: $ => seq($._idUp, repeat($._idChar), repeat('\'')),
    concreteTypes: $ => seq('[', optional(seq($.concreteType, repeat(seq(',', $.concreteType)))), ']'),

    // mGen   : | OS (genDecl (Comma genDecl)*)? CS;
    // mGen: $ => seq('[', optional(seq($._genDecl, repeat(seq(',', $._genDecl)))) ,']'),

    // genDecl : t Colon (mdf (Comma mdf)*) | t;
    // TODO: Is it safe to use optional instead of the  t <stuff> | t
    // TODO: Make mdf a child of fullcn or smth like that.
    // _genDecl: $ => prec.left(seq(
      // $._t,
      // optional(
        // seq(
          // ':',
          // optional($.mdf),
          // repeat(
            // seq(',', optional($.mdf))))))),
    // TODO: Ask what these shortnames mean and see if i can give them more descriptive names
    //       It'd make writing queries and such easier.
    // _t: $ => seq(optional($.mdf), $.fullCN, optional($.mGen)),
    _mdfGeneric: $ => choice('mut' , 'readH' , 'mutH' , 'read/imm' , 'read' , 'iso' , 'recMdf' , 'imm'),
    _mdfConcrete: $ => choice('mut' , 'readH' , 'mutH', 'read' , 'iso' , 'recMdf' , 'imm'),

    // TODO: topDec
    topDec: $ => 'topDec',

    _idLow: $ => choice(/_*[a-z]/, /_+[0-9]/),
    _idUp: $ => /_*[A-Z]/,
    _idChar: $ => /[a-zA-Z_0-9]/,
    _fIdLow: $ => seq($._idLow, repeat($._idChar), repeat('\'')),
  }
});
