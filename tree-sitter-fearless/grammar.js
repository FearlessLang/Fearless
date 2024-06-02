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
    _pkgName: $ => /(_*[a-z]|_+[0-9])[a-zA-Z_0-9]*/,

    alias: $ => seq('alias', ' ', field('from', alias($._noMdfConcreteType, $.concreteType)), ' ', 'as', ' ', field('to', $.typeName), ','),

    // mdf is invalid in some places, like the LHS of alias.
    _noMdfConcreteType: $ => seq(
      optional(field('package', $.packagePath)),
      field('name', $.typeName),
      optional(field('generic', $.concreteTypes))
    ),

    concreteType: $ => seq(optional(field("mdf", $.mdf)), $._noMdfConcreteType),

    typeName: $ => /_*[A-Z][a-zA-Z_0-9]*'*/,
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
    // WARN: read/imm is only allowed in specific conditions, but I think it's probably fine for us to ignore that
    mdf: $ => choice('mut' , 'readH' , 'mutH' , 'read/imm' , 'read' , 'iso' , 'recMdf' , 'imm'),

    // TODO: topDec
    topDec: $ => 'topDec',
  }
});
