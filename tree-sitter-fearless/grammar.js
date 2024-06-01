module.exports = grammar({
  name: 'fearless',

  rules: {
    source_file: $ => seq($.package, repeat($.alias), repeat($.topDec)),

    // alias  : Alias fullCN mGen As fullCN mGen Comma;
    // TODO: Add like a from: and to: fields.
    // XXX: Should 'alias' be put in a separate rule like in the antlr grammar?
    alias: $ => seq('alias ', $.fullCN, optional($.mGen), 'as', $.fullCN, optional($.mGen), ','),

    fullCN: $ => choice(
      // TODO: Have some sort of packagePath for the bit in []: [some.cool].package
      seq(field('package', alias(repeat(seq($._px, '.')), $.packagePath)), field('type', alias($._fIdUp, $.type))),
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
    mdf: $ => choice("Mut" , "ReadH" , "MutH" , "ReadImm" , "Read" , "Iso" , "RecMdf" , "Imm"),

    // TODO: topDec
    topDec: $ => 'topDec',
    // XXX: Do we want something like tree-sitter-java, where each path of the
    //      package is its own scoped identifier?
    package: $ => seq('package ', field("name", $.packagePath), '\n'),
    packagePath: $ => seq(repeat(seq($._px, '.')), $._px),
    _px: $ => seq($._idLow, repeat($._idChar)),
    _idLow: $ => choice(/_*[a-z]/, /_+[0-9]/),
    _idUp: $ => /_*[A-Z]/,
    _idChar: $ => /[a-zA-Z_0-9]/,
    _fIdUp: $ => seq($._idUp, repeat($._idChar), repeat('\'')),
    _fIdLow: $ => seq($._idLow, repeat($._idChar), repeat('\'')),
  }
});
