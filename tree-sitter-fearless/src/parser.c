#include "tree_sitter/parser.h"

#if defined(__GNUC__) || defined(__clang__)
#pragma GCC diagnostic ignored "-Wmissing-field-initializers"
#endif

#define LANGUAGE_VERSION 14
#define STATE_COUNT 51
#define LARGE_STATE_COUNT 2
#define SYMBOL_COUNT 32
#define ALIAS_COUNT 0
#define TOKEN_COUNT 17
#define EXTERNAL_TOKEN_COUNT 0
#define FIELD_COUNT 5
#define MAX_ALIAS_SEQUENCE_LENGTH 8
#define PRODUCTION_ID_COUNT 7

enum ts_symbol_identifiers {
  anon_sym_package = 1,
  anon_sym_SPACE = 2,
  anon_sym_DOT = 3,
  anon_sym_alias = 4,
  anon_sym_as = 5,
  anon_sym_COMMA = 6,
  anon_sym_SQUOTE = 7,
  sym_genericList = 8,
  anon_sym_LBRACK = 9,
  anon_sym_RBRACK = 10,
  anon_sym_COLON = 11,
  sym_topDec = 12,
  aux_sym__idLow_token1 = 13,
  aux_sym__idLow_token2 = 14,
  sym__idUp = 15,
  sym__idChar = 16,
  sym_source_file = 17,
  sym_package = 18,
  sym__fullPkgName = 19,
  sym_packagePath = 20,
  sym__pkgName = 21,
  sym_alias = 22,
  sym_type = 23,
  sym_typeName = 24,
  sym__idLow = 25,
  aux_sym_source_file_repeat1 = 26,
  aux_sym_source_file_repeat2 = 27,
  aux_sym__fullPkgName_repeat1 = 28,
  aux_sym_packagePath_repeat1 = 29,
  aux_sym__pkgName_repeat1 = 30,
  aux_sym_typeName_repeat1 = 31,
};

static const char * const ts_symbol_names[] = {
  [ts_builtin_sym_end] = "end",
  [anon_sym_package] = "package",
  [anon_sym_SPACE] = " ",
  [anon_sym_DOT] = ".",
  [anon_sym_alias] = "alias",
  [anon_sym_as] = "as",
  [anon_sym_COMMA] = ",",
  [anon_sym_SQUOTE] = "'",
  [sym_genericList] = "genericList",
  [anon_sym_LBRACK] = "[",
  [anon_sym_RBRACK] = "]",
  [anon_sym_COLON] = ":",
  [sym_topDec] = "topDec",
  [aux_sym__idLow_token1] = "_idLow_token1",
  [aux_sym__idLow_token2] = "_idLow_token2",
  [sym__idUp] = "_idUp",
  [sym__idChar] = "_idChar",
  [sym_source_file] = "source_file",
  [sym_package] = "package",
  [sym__fullPkgName] = "packagePath",
  [sym_packagePath] = "packagePath",
  [sym__pkgName] = "_pkgName",
  [sym_alias] = "alias",
  [sym_type] = "type",
  [sym_typeName] = "typeName",
  [sym__idLow] = "_idLow",
  [aux_sym_source_file_repeat1] = "source_file_repeat1",
  [aux_sym_source_file_repeat2] = "source_file_repeat2",
  [aux_sym__fullPkgName_repeat1] = "_fullPkgName_repeat1",
  [aux_sym_packagePath_repeat1] = "packagePath_repeat1",
  [aux_sym__pkgName_repeat1] = "_pkgName_repeat1",
  [aux_sym_typeName_repeat1] = "typeName_repeat1",
};

static const TSSymbol ts_symbol_map[] = {
  [ts_builtin_sym_end] = ts_builtin_sym_end,
  [anon_sym_package] = anon_sym_package,
  [anon_sym_SPACE] = anon_sym_SPACE,
  [anon_sym_DOT] = anon_sym_DOT,
  [anon_sym_alias] = anon_sym_alias,
  [anon_sym_as] = anon_sym_as,
  [anon_sym_COMMA] = anon_sym_COMMA,
  [anon_sym_SQUOTE] = anon_sym_SQUOTE,
  [sym_genericList] = sym_genericList,
  [anon_sym_LBRACK] = anon_sym_LBRACK,
  [anon_sym_RBRACK] = anon_sym_RBRACK,
  [anon_sym_COLON] = anon_sym_COLON,
  [sym_topDec] = sym_topDec,
  [aux_sym__idLow_token1] = aux_sym__idLow_token1,
  [aux_sym__idLow_token2] = aux_sym__idLow_token2,
  [sym__idUp] = sym__idUp,
  [sym__idChar] = sym__idChar,
  [sym_source_file] = sym_source_file,
  [sym_package] = sym_package,
  [sym__fullPkgName] = sym_packagePath,
  [sym_packagePath] = sym_packagePath,
  [sym__pkgName] = sym__pkgName,
  [sym_alias] = sym_alias,
  [sym_type] = sym_type,
  [sym_typeName] = sym_typeName,
  [sym__idLow] = sym__idLow,
  [aux_sym_source_file_repeat1] = aux_sym_source_file_repeat1,
  [aux_sym_source_file_repeat2] = aux_sym_source_file_repeat2,
  [aux_sym__fullPkgName_repeat1] = aux_sym__fullPkgName_repeat1,
  [aux_sym_packagePath_repeat1] = aux_sym_packagePath_repeat1,
  [aux_sym__pkgName_repeat1] = aux_sym__pkgName_repeat1,
  [aux_sym_typeName_repeat1] = aux_sym_typeName_repeat1,
};

static const TSSymbolMetadata ts_symbol_metadata[] = {
  [ts_builtin_sym_end] = {
    .visible = false,
    .named = true,
  },
  [anon_sym_package] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_SPACE] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_DOT] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_alias] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_as] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_COMMA] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_SQUOTE] = {
    .visible = true,
    .named = false,
  },
  [sym_genericList] = {
    .visible = true,
    .named = true,
  },
  [anon_sym_LBRACK] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_RBRACK] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_COLON] = {
    .visible = true,
    .named = false,
  },
  [sym_topDec] = {
    .visible = true,
    .named = true,
  },
  [aux_sym__idLow_token1] = {
    .visible = false,
    .named = false,
  },
  [aux_sym__idLow_token2] = {
    .visible = false,
    .named = false,
  },
  [sym__idUp] = {
    .visible = false,
    .named = true,
  },
  [sym__idChar] = {
    .visible = false,
    .named = true,
  },
  [sym_source_file] = {
    .visible = true,
    .named = true,
  },
  [sym_package] = {
    .visible = true,
    .named = true,
  },
  [sym__fullPkgName] = {
    .visible = true,
    .named = true,
  },
  [sym_packagePath] = {
    .visible = true,
    .named = true,
  },
  [sym__pkgName] = {
    .visible = false,
    .named = true,
  },
  [sym_alias] = {
    .visible = true,
    .named = true,
  },
  [sym_type] = {
    .visible = true,
    .named = true,
  },
  [sym_typeName] = {
    .visible = true,
    .named = true,
  },
  [sym__idLow] = {
    .visible = false,
    .named = true,
  },
  [aux_sym_source_file_repeat1] = {
    .visible = false,
    .named = false,
  },
  [aux_sym_source_file_repeat2] = {
    .visible = false,
    .named = false,
  },
  [aux_sym__fullPkgName_repeat1] = {
    .visible = false,
    .named = false,
  },
  [aux_sym_packagePath_repeat1] = {
    .visible = false,
    .named = false,
  },
  [aux_sym__pkgName_repeat1] = {
    .visible = false,
    .named = false,
  },
  [aux_sym_typeName_repeat1] = {
    .visible = false,
    .named = false,
  },
};

enum ts_field_identifiers {
  field_from = 1,
  field_generic = 2,
  field_name = 3,
  field_package = 4,
  field_to = 5,
};

static const char * const ts_field_names[] = {
  [0] = NULL,
  [field_from] = "from",
  [field_generic] = "generic",
  [field_name] = "name",
  [field_package] = "package",
  [field_to] = "to",
};

static const TSFieldMapSlice ts_field_map_slices[PRODUCTION_ID_COUNT] = {
  [1] = {.index = 0, .length = 1},
  [2] = {.index = 1, .length = 1},
  [3] = {.index = 2, .length = 2},
  [4] = {.index = 4, .length = 2},
  [5] = {.index = 6, .length = 3},
  [6] = {.index = 9, .length = 2},
};

static const TSFieldMapEntry ts_field_map_entries[] = {
  [0] =
    {field_name, 2},
  [1] =
    {field_name, 0},
  [2] =
    {field_name, 1},
    {field_package, 0},
  [4] =
    {field_generic, 1},
    {field_name, 0},
  [6] =
    {field_generic, 2},
    {field_name, 1},
    {field_package, 0},
  [9] =
    {field_from, 2},
    {field_to, 6},
};

static const TSSymbol ts_alias_sequences[PRODUCTION_ID_COUNT][MAX_ALIAS_SEQUENCE_LENGTH] = {
  [0] = {0},
};

static const uint16_t ts_non_terminal_alias_map[] = {
  0,
};

static const TSStateId ts_primary_state_ids[STATE_COUNT] = {
  [0] = 0,
  [1] = 1,
  [2] = 2,
  [3] = 3,
  [4] = 4,
  [5] = 5,
  [6] = 6,
  [7] = 7,
  [8] = 8,
  [9] = 9,
  [10] = 10,
  [11] = 11,
  [12] = 12,
  [13] = 13,
  [14] = 8,
  [15] = 15,
  [16] = 16,
  [17] = 17,
  [18] = 4,
  [19] = 8,
  [20] = 10,
  [21] = 21,
  [22] = 22,
  [23] = 23,
  [24] = 24,
  [25] = 25,
  [26] = 11,
  [27] = 27,
  [28] = 22,
  [29] = 29,
  [30] = 23,
  [31] = 24,
  [32] = 32,
  [33] = 33,
  [34] = 34,
  [35] = 6,
  [36] = 36,
  [37] = 37,
  [38] = 38,
  [39] = 39,
  [40] = 40,
  [41] = 41,
  [42] = 42,
  [43] = 43,
  [44] = 44,
  [45] = 45,
  [46] = 46,
  [47] = 47,
  [48] = 48,
  [49] = 49,
  [50] = 50,
};

static bool ts_lex(TSLexer *lexer, TSStateId state) {
  START_LEXER();
  eof = lexer->eof(lexer);
  switch (state) {
    case 0:
      if (eof) ADVANCE(26);
      ADVANCE_MAP(
        '\'', 33,
        ',', 32,
        '.', 29,
        ':', 37,
        '[', 35,
        ']', 36,
        'a', 43,
        't', 44,
      );
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(0);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('b' <= lookahead && lookahead <= 'z')) ADVANCE(42);
      END_STATE();
    case 1:
      if (lookahead == ' ') ADVANCE(28);
      if (lookahead == '\'') ADVANCE(33);
      if (lookahead == 't') ADVANCE(45);
      if (('\t' <= lookahead && lookahead <= '\r')) SKIP(1);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(42);
      END_STATE();
    case 2:
      if (lookahead == '!') ADVANCE(34);
      END_STATE();
    case 3:
      if (lookahead == '\'') ADVANCE(33);
      if (lookahead == ',') ADVANCE(32);
      if (lookahead == '.') ADVANCE(29);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(3);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(42);
      END_STATE();
    case 4:
      if (lookahead == 'D') ADVANCE(16);
      END_STATE();
    case 5:
      if (lookahead == '_') ADVANCE(6);
      if (lookahead == 'a') ADVANCE(24);
      if (lookahead == 'p') ADVANCE(11);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(5);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(41);
      END_STATE();
    case 6:
      if (lookahead == '_') ADVANCE(6);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(41);
      END_STATE();
    case 7:
      if (lookahead == '_') ADVANCE(8);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(7);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(41);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(39);
      END_STATE();
    case 8:
      if (lookahead == '_') ADVANCE(8);
      if (('0' <= lookahead && lookahead <= '9')) ADVANCE(40);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(41);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(39);
      END_STATE();
    case 9:
      if (lookahead == 'a') ADVANCE(23);
      END_STATE();
    case 10:
      if (lookahead == 'a') ADVANCE(18);
      END_STATE();
    case 11:
      if (lookahead == 'a') ADVANCE(13);
      END_STATE();
    case 12:
      if (lookahead == 'c') ADVANCE(38);
      END_STATE();
    case 13:
      if (lookahead == 'c') ADVANCE(20);
      END_STATE();
    case 14:
      if (lookahead == 'd') ADVANCE(21);
      END_STATE();
    case 15:
      if (lookahead == 'd') ADVANCE(21);
      if (lookahead == 'p') ADVANCE(4);
      END_STATE();
    case 16:
      if (lookahead == 'e') ADVANCE(12);
      END_STATE();
    case 17:
      if (lookahead == 'e') ADVANCE(27);
      END_STATE();
    case 18:
      if (lookahead == 'g') ADVANCE(17);
      END_STATE();
    case 19:
      if (lookahead == 'i') ADVANCE(9);
      END_STATE();
    case 20:
      if (lookahead == 'k') ADVANCE(10);
      END_STATE();
    case 21:
      if (lookahead == 'o') ADVANCE(2);
      END_STATE();
    case 22:
      if (lookahead == 'p') ADVANCE(4);
      END_STATE();
    case 23:
      if (lookahead == 's') ADVANCE(30);
      END_STATE();
    case 24:
      if (lookahead == 's') ADVANCE(31);
      END_STATE();
    case 25:
      if (eof) ADVANCE(26);
      if (lookahead == '.') ADVANCE(29);
      if (lookahead == 'a') ADVANCE(43);
      if (lookahead == 't') ADVANCE(46);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(25);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('b' <= lookahead && lookahead <= 'z')) ADVANCE(42);
      END_STATE();
    case 26:
      ACCEPT_TOKEN(ts_builtin_sym_end);
      END_STATE();
    case 27:
      ACCEPT_TOKEN(anon_sym_package);
      END_STATE();
    case 28:
      ACCEPT_TOKEN(anon_sym_SPACE);
      if (lookahead == ' ') ADVANCE(28);
      END_STATE();
    case 29:
      ACCEPT_TOKEN(anon_sym_DOT);
      END_STATE();
    case 30:
      ACCEPT_TOKEN(anon_sym_alias);
      END_STATE();
    case 31:
      ACCEPT_TOKEN(anon_sym_as);
      END_STATE();
    case 32:
      ACCEPT_TOKEN(anon_sym_COMMA);
      END_STATE();
    case 33:
      ACCEPT_TOKEN(anon_sym_SQUOTE);
      END_STATE();
    case 34:
      ACCEPT_TOKEN(sym_genericList);
      END_STATE();
    case 35:
      ACCEPT_TOKEN(anon_sym_LBRACK);
      END_STATE();
    case 36:
      ACCEPT_TOKEN(anon_sym_RBRACK);
      END_STATE();
    case 37:
      ACCEPT_TOKEN(anon_sym_COLON);
      END_STATE();
    case 38:
      ACCEPT_TOKEN(sym_topDec);
      END_STATE();
    case 39:
      ACCEPT_TOKEN(aux_sym__idLow_token1);
      END_STATE();
    case 40:
      ACCEPT_TOKEN(aux_sym__idLow_token2);
      END_STATE();
    case 41:
      ACCEPT_TOKEN(sym__idUp);
      END_STATE();
    case 42:
      ACCEPT_TOKEN(sym__idChar);
      END_STATE();
    case 43:
      ACCEPT_TOKEN(sym__idChar);
      if (lookahead == 'l') ADVANCE(19);
      END_STATE();
    case 44:
      ACCEPT_TOKEN(sym__idChar);
      if (lookahead == 'o') ADVANCE(15);
      END_STATE();
    case 45:
      ACCEPT_TOKEN(sym__idChar);
      if (lookahead == 'o') ADVANCE(14);
      END_STATE();
    case 46:
      ACCEPT_TOKEN(sym__idChar);
      if (lookahead == 'o') ADVANCE(22);
      END_STATE();
    default:
      return false;
  }
}

static const TSLexMode ts_lex_modes[STATE_COUNT] = {
  [0] = {.lex_state = 0},
  [1] = {.lex_state = 5},
  [2] = {.lex_state = 7},
  [3] = {.lex_state = 7},
  [4] = {.lex_state = 1},
  [5] = {.lex_state = 0},
  [6] = {.lex_state = 25},
  [7] = {.lex_state = 0},
  [8] = {.lex_state = 25},
  [9] = {.lex_state = 7},
  [10] = {.lex_state = 1},
  [11] = {.lex_state = 25},
  [12] = {.lex_state = 7},
  [13] = {.lex_state = 0},
  [14] = {.lex_state = 3},
  [15] = {.lex_state = 0},
  [16] = {.lex_state = 0},
  [17] = {.lex_state = 0},
  [18] = {.lex_state = 3},
  [19] = {.lex_state = 1},
  [20] = {.lex_state = 3},
  [21] = {.lex_state = 0},
  [22] = {.lex_state = 1},
  [23] = {.lex_state = 1},
  [24] = {.lex_state = 1},
  [25] = {.lex_state = 7},
  [26] = {.lex_state = 3},
  [27] = {.lex_state = 0},
  [28] = {.lex_state = 0},
  [29] = {.lex_state = 7},
  [30] = {.lex_state = 0},
  [31] = {.lex_state = 0},
  [32] = {.lex_state = 0},
  [33] = {.lex_state = 0},
  [34] = {.lex_state = 0},
  [35] = {.lex_state = 3},
  [36] = {.lex_state = 0},
  [37] = {.lex_state = 1},
  [38] = {.lex_state = 1},
  [39] = {.lex_state = 5},
  [40] = {.lex_state = 5},
  [41] = {.lex_state = 5},
  [42] = {.lex_state = 0},
  [43] = {.lex_state = 1},
  [44] = {.lex_state = 1},
  [45] = {.lex_state = 1},
  [46] = {.lex_state = 1},
  [47] = {.lex_state = 0},
  [48] = {.lex_state = 1},
  [49] = {.lex_state = 1},
  [50] = {.lex_state = 0},
};

static const uint16_t ts_parse_table[LARGE_STATE_COUNT][SYMBOL_COUNT] = {
  [0] = {
    [ts_builtin_sym_end] = ACTIONS(1),
    [anon_sym_DOT] = ACTIONS(1),
    [anon_sym_alias] = ACTIONS(1),
    [anon_sym_COMMA] = ACTIONS(1),
    [anon_sym_SQUOTE] = ACTIONS(1),
    [sym_genericList] = ACTIONS(1),
    [anon_sym_LBRACK] = ACTIONS(1),
    [anon_sym_RBRACK] = ACTIONS(1),
    [anon_sym_COLON] = ACTIONS(1),
    [sym_topDec] = ACTIONS(1),
    [sym__idChar] = ACTIONS(1),
  },
  [1] = {
    [sym_source_file] = STATE(50),
    [sym_package] = STATE(5),
    [anon_sym_package] = ACTIONS(3),
  },
};

static const uint16_t ts_small_parse_table[] = {
  [0] = 8,
    ACTIONS(7), 1,
      sym__idUp,
    STATE(3), 1,
      aux_sym_packagePath_repeat1,
    STATE(26), 1,
      sym__idLow,
    STATE(37), 1,
      sym_typeName,
    STATE(39), 1,
      sym_packagePath,
    STATE(43), 1,
      sym_type,
    STATE(47), 1,
      sym__pkgName,
    ACTIONS(5), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [26] = 5,
    ACTIONS(9), 1,
      sym__idUp,
    STATE(9), 1,
      aux_sym_packagePath_repeat1,
    STATE(26), 1,
      sym__idLow,
    STATE(47), 1,
      sym__pkgName,
    ACTIONS(5), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [43] = 6,
    ACTIONS(11), 1,
      anon_sym_SPACE,
    ACTIONS(13), 1,
      anon_sym_SQUOTE,
    ACTIONS(15), 1,
      sym_genericList,
    ACTIONS(17), 1,
      sym__idChar,
    STATE(10), 1,
      aux_sym__pkgName_repeat1,
    STATE(24), 1,
      aux_sym_typeName_repeat1,
  [62] = 5,
    ACTIONS(19), 1,
      ts_builtin_sym_end,
    ACTIONS(21), 1,
      anon_sym_alias,
    ACTIONS(23), 1,
      sym_topDec,
    STATE(33), 1,
      aux_sym_source_file_repeat2,
    STATE(7), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [79] = 3,
    ACTIONS(27), 1,
      sym__idChar,
    STATE(8), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(25), 4,
      ts_builtin_sym_end,
      anon_sym_DOT,
      anon_sym_alias,
      sym_topDec,
  [92] = 5,
    ACTIONS(21), 1,
      anon_sym_alias,
    ACTIONS(29), 1,
      ts_builtin_sym_end,
    ACTIONS(31), 1,
      sym_topDec,
    STATE(32), 1,
      aux_sym_source_file_repeat2,
    STATE(13), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [109] = 3,
    ACTIONS(35), 1,
      sym__idChar,
    STATE(8), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(33), 4,
      ts_builtin_sym_end,
      anon_sym_DOT,
      anon_sym_alias,
      sym_topDec,
  [122] = 5,
    ACTIONS(41), 1,
      sym__idUp,
    STATE(9), 1,
      aux_sym_packagePath_repeat1,
    STATE(26), 1,
      sym__idLow,
    STATE(47), 1,
      sym__pkgName,
    ACTIONS(38), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [139] = 6,
    ACTIONS(43), 1,
      anon_sym_SPACE,
    ACTIONS(45), 1,
      anon_sym_SQUOTE,
    ACTIONS(47), 1,
      sym_genericList,
    ACTIONS(49), 1,
      sym__idChar,
    STATE(19), 1,
      aux_sym__pkgName_repeat1,
    STATE(23), 1,
      aux_sym_typeName_repeat1,
  [158] = 3,
    ACTIONS(53), 1,
      sym__idChar,
    STATE(6), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(51), 4,
      ts_builtin_sym_end,
      anon_sym_DOT,
      anon_sym_alias,
      sym_topDec,
  [171] = 4,
    STATE(11), 1,
      sym__idLow,
    STATE(15), 1,
      sym__pkgName,
    STATE(34), 1,
      sym__fullPkgName,
    ACTIONS(55), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [185] = 3,
    ACTIONS(59), 1,
      anon_sym_alias,
    ACTIONS(57), 2,
      ts_builtin_sym_end,
      sym_topDec,
    STATE(13), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [197] = 3,
    ACTIONS(62), 1,
      sym__idChar,
    STATE(14), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(33), 3,
      anon_sym_DOT,
      anon_sym_COMMA,
      anon_sym_SQUOTE,
  [209] = 3,
    ACTIONS(67), 1,
      anon_sym_DOT,
    STATE(17), 1,
      aux_sym__fullPkgName_repeat1,
    ACTIONS(65), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [221] = 3,
    ACTIONS(71), 1,
      anon_sym_DOT,
    STATE(16), 1,
      aux_sym__fullPkgName_repeat1,
    ACTIONS(69), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [233] = 3,
    ACTIONS(67), 1,
      anon_sym_DOT,
    STATE(16), 1,
      aux_sym__fullPkgName_repeat1,
    ACTIONS(74), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [245] = 5,
    ACTIONS(11), 1,
      anon_sym_COMMA,
    ACTIONS(76), 1,
      anon_sym_SQUOTE,
    ACTIONS(78), 1,
      sym__idChar,
    STATE(20), 1,
      aux_sym__pkgName_repeat1,
    STATE(31), 1,
      aux_sym_typeName_repeat1,
  [261] = 4,
    ACTIONS(33), 1,
      anon_sym_SPACE,
    ACTIONS(82), 1,
      sym__idChar,
    STATE(19), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(80), 2,
      anon_sym_SQUOTE,
      sym_genericList,
  [275] = 5,
    ACTIONS(43), 1,
      anon_sym_COMMA,
    ACTIONS(85), 1,
      anon_sym_SQUOTE,
    ACTIONS(87), 1,
      sym__idChar,
    STATE(14), 1,
      aux_sym__pkgName_repeat1,
    STATE(30), 1,
      aux_sym_typeName_repeat1,
  [291] = 1,
    ACTIONS(69), 4,
      ts_builtin_sym_end,
      anon_sym_DOT,
      anon_sym_alias,
      sym_topDec,
  [298] = 4,
    ACTIONS(89), 1,
      anon_sym_SPACE,
    ACTIONS(91), 1,
      anon_sym_SQUOTE,
    ACTIONS(94), 1,
      sym_genericList,
    STATE(22), 1,
      aux_sym_typeName_repeat1,
  [311] = 4,
    ACTIONS(96), 1,
      anon_sym_SPACE,
    ACTIONS(98), 1,
      anon_sym_SQUOTE,
    ACTIONS(100), 1,
      sym_genericList,
    STATE(22), 1,
      aux_sym_typeName_repeat1,
  [324] = 4,
    ACTIONS(43), 1,
      anon_sym_SPACE,
    ACTIONS(47), 1,
      sym_genericList,
    ACTIONS(98), 1,
      anon_sym_SQUOTE,
    STATE(22), 1,
      aux_sym_typeName_repeat1,
  [337] = 3,
    STATE(11), 1,
      sym__idLow,
    STATE(21), 1,
      sym__pkgName,
    ACTIONS(55), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [348] = 3,
    ACTIONS(51), 1,
      anon_sym_DOT,
    ACTIONS(102), 1,
      sym__idChar,
    STATE(35), 1,
      aux_sym__pkgName_repeat1,
  [358] = 3,
    ACTIONS(104), 1,
      ts_builtin_sym_end,
    ACTIONS(106), 1,
      sym_topDec,
    STATE(27), 1,
      aux_sym_source_file_repeat2,
  [368] = 3,
    ACTIONS(89), 1,
      anon_sym_COMMA,
    ACTIONS(109), 1,
      anon_sym_SQUOTE,
    STATE(28), 1,
      aux_sym_typeName_repeat1,
  [378] = 1,
    ACTIONS(41), 3,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
      sym__idUp,
  [384] = 3,
    ACTIONS(96), 1,
      anon_sym_COMMA,
    ACTIONS(112), 1,
      anon_sym_SQUOTE,
    STATE(28), 1,
      aux_sym_typeName_repeat1,
  [394] = 3,
    ACTIONS(43), 1,
      anon_sym_COMMA,
    ACTIONS(112), 1,
      anon_sym_SQUOTE,
    STATE(28), 1,
      aux_sym_typeName_repeat1,
  [404] = 3,
    ACTIONS(114), 1,
      ts_builtin_sym_end,
    ACTIONS(116), 1,
      sym_topDec,
    STATE(27), 1,
      aux_sym_source_file_repeat2,
  [414] = 3,
    ACTIONS(29), 1,
      ts_builtin_sym_end,
    ACTIONS(116), 1,
      sym_topDec,
    STATE(27), 1,
      aux_sym_source_file_repeat2,
  [424] = 1,
    ACTIONS(118), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [430] = 3,
    ACTIONS(25), 1,
      anon_sym_DOT,
    ACTIONS(87), 1,
      sym__idChar,
    STATE(14), 1,
      aux_sym__pkgName_repeat1,
  [440] = 1,
    ACTIONS(120), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [446] = 2,
    ACTIONS(122), 1,
      anon_sym_SPACE,
    ACTIONS(124), 1,
      sym_genericList,
  [453] = 2,
    ACTIONS(126), 1,
      anon_sym_SPACE,
    ACTIONS(128), 1,
      sym_genericList,
  [460] = 2,
    ACTIONS(7), 1,
      sym__idUp,
    STATE(38), 1,
      sym_typeName,
  [467] = 2,
    ACTIONS(130), 1,
      sym__idUp,
    STATE(42), 1,
      sym_typeName,
  [474] = 1,
    ACTIONS(132), 1,
      anon_sym_as,
  [478] = 1,
    ACTIONS(134), 1,
      anon_sym_COMMA,
  [482] = 1,
    ACTIONS(136), 1,
      anon_sym_SPACE,
  [486] = 1,
    ACTIONS(138), 1,
      anon_sym_SPACE,
  [490] = 1,
    ACTIONS(140), 1,
      anon_sym_SPACE,
  [494] = 1,
    ACTIONS(142), 1,
      anon_sym_SPACE,
  [498] = 1,
    ACTIONS(144), 1,
      anon_sym_DOT,
  [502] = 1,
    ACTIONS(146), 1,
      anon_sym_SPACE,
  [506] = 1,
    ACTIONS(148), 1,
      anon_sym_SPACE,
  [510] = 1,
    ACTIONS(150), 1,
      ts_builtin_sym_end,
};

static const uint32_t ts_small_parse_table_map[] = {
  [SMALL_STATE(2)] = 0,
  [SMALL_STATE(3)] = 26,
  [SMALL_STATE(4)] = 43,
  [SMALL_STATE(5)] = 62,
  [SMALL_STATE(6)] = 79,
  [SMALL_STATE(7)] = 92,
  [SMALL_STATE(8)] = 109,
  [SMALL_STATE(9)] = 122,
  [SMALL_STATE(10)] = 139,
  [SMALL_STATE(11)] = 158,
  [SMALL_STATE(12)] = 171,
  [SMALL_STATE(13)] = 185,
  [SMALL_STATE(14)] = 197,
  [SMALL_STATE(15)] = 209,
  [SMALL_STATE(16)] = 221,
  [SMALL_STATE(17)] = 233,
  [SMALL_STATE(18)] = 245,
  [SMALL_STATE(19)] = 261,
  [SMALL_STATE(20)] = 275,
  [SMALL_STATE(21)] = 291,
  [SMALL_STATE(22)] = 298,
  [SMALL_STATE(23)] = 311,
  [SMALL_STATE(24)] = 324,
  [SMALL_STATE(25)] = 337,
  [SMALL_STATE(26)] = 348,
  [SMALL_STATE(27)] = 358,
  [SMALL_STATE(28)] = 368,
  [SMALL_STATE(29)] = 378,
  [SMALL_STATE(30)] = 384,
  [SMALL_STATE(31)] = 394,
  [SMALL_STATE(32)] = 404,
  [SMALL_STATE(33)] = 414,
  [SMALL_STATE(34)] = 424,
  [SMALL_STATE(35)] = 430,
  [SMALL_STATE(36)] = 440,
  [SMALL_STATE(37)] = 446,
  [SMALL_STATE(38)] = 453,
  [SMALL_STATE(39)] = 460,
  [SMALL_STATE(40)] = 467,
  [SMALL_STATE(41)] = 474,
  [SMALL_STATE(42)] = 478,
  [SMALL_STATE(43)] = 482,
  [SMALL_STATE(44)] = 486,
  [SMALL_STATE(45)] = 490,
  [SMALL_STATE(46)] = 494,
  [SMALL_STATE(47)] = 498,
  [SMALL_STATE(48)] = 502,
  [SMALL_STATE(49)] = 506,
  [SMALL_STATE(50)] = 510,
};

static const TSParseActionEntry ts_parse_actions[] = {
  [0] = {.entry = {.count = 0, .reusable = false}},
  [1] = {.entry = {.count = 1, .reusable = false}}, RECOVER(),
  [3] = {.entry = {.count = 1, .reusable = true}}, SHIFT(49),
  [5] = {.entry = {.count = 1, .reusable = true}}, SHIFT(26),
  [7] = {.entry = {.count = 1, .reusable = true}}, SHIFT(4),
  [9] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_packagePath, 1, 0, 0),
  [11] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_typeName, 1, 0, 0),
  [13] = {.entry = {.count = 1, .reusable = false}}, SHIFT(24),
  [15] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym_typeName, 1, 0, 0),
  [17] = {.entry = {.count = 1, .reusable = false}}, SHIFT(10),
  [19] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 1, 0, 0),
  [21] = {.entry = {.count = 1, .reusable = true}}, SHIFT(46),
  [23] = {.entry = {.count = 1, .reusable = true}}, SHIFT(33),
  [25] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__pkgName, 2, 0, 0),
  [27] = {.entry = {.count = 1, .reusable = false}}, SHIFT(8),
  [29] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 2, 0, 0),
  [31] = {.entry = {.count = 1, .reusable = true}}, SHIFT(32),
  [33] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0),
  [35] = {.entry = {.count = 2, .reusable = false}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(8),
  [38] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_packagePath_repeat1, 2, 0, 0), SHIFT_REPEAT(26),
  [41] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_packagePath_repeat1, 2, 0, 0),
  [43] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_typeName, 2, 0, 0),
  [45] = {.entry = {.count = 1, .reusable = false}}, SHIFT(23),
  [47] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym_typeName, 2, 0, 0),
  [49] = {.entry = {.count = 1, .reusable = false}}, SHIFT(19),
  [51] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__pkgName, 1, 0, 0),
  [53] = {.entry = {.count = 1, .reusable = false}}, SHIFT(6),
  [55] = {.entry = {.count = 1, .reusable = true}}, SHIFT(11),
  [57] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0),
  [59] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0), SHIFT_REPEAT(46),
  [62] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(14),
  [65] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fullPkgName, 1, 0, 0),
  [67] = {.entry = {.count = 1, .reusable = true}}, SHIFT(25),
  [69] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__fullPkgName_repeat1, 2, 0, 0),
  [71] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__fullPkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(25),
  [74] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fullPkgName, 2, 0, 0),
  [76] = {.entry = {.count = 1, .reusable = true}}, SHIFT(31),
  [78] = {.entry = {.count = 1, .reusable = true}}, SHIFT(20),
  [80] = {.entry = {.count = 1, .reusable = false}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0),
  [82] = {.entry = {.count = 2, .reusable = false}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(19),
  [85] = {.entry = {.count = 1, .reusable = true}}, SHIFT(30),
  [87] = {.entry = {.count = 1, .reusable = true}}, SHIFT(14),
  [89] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_typeName_repeat1, 2, 0, 0),
  [91] = {.entry = {.count = 2, .reusable = false}}, REDUCE(aux_sym_typeName_repeat1, 2, 0, 0), SHIFT_REPEAT(22),
  [94] = {.entry = {.count = 1, .reusable = false}}, REDUCE(aux_sym_typeName_repeat1, 2, 0, 0),
  [96] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_typeName, 3, 0, 0),
  [98] = {.entry = {.count = 1, .reusable = false}}, SHIFT(22),
  [100] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym_typeName, 3, 0, 0),
  [102] = {.entry = {.count = 1, .reusable = true}}, SHIFT(35),
  [104] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0),
  [106] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0), SHIFT_REPEAT(27),
  [109] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_typeName_repeat1, 2, 0, 0), SHIFT_REPEAT(28),
  [112] = {.entry = {.count = 1, .reusable = true}}, SHIFT(28),
  [114] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 3, 0, 0),
  [116] = {.entry = {.count = 1, .reusable = true}}, SHIFT(27),
  [118] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_package, 3, 0, 1),
  [120] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_alias, 8, 0, 6),
  [122] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_type, 1, 0, 2),
  [124] = {.entry = {.count = 1, .reusable = false}}, SHIFT(48),
  [126] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_type, 2, 0, 3),
  [128] = {.entry = {.count = 1, .reusable = false}}, SHIFT(45),
  [130] = {.entry = {.count = 1, .reusable = true}}, SHIFT(18),
  [132] = {.entry = {.count = 1, .reusable = true}}, SHIFT(44),
  [134] = {.entry = {.count = 1, .reusable = true}}, SHIFT(36),
  [136] = {.entry = {.count = 1, .reusable = true}}, SHIFT(41),
  [138] = {.entry = {.count = 1, .reusable = true}}, SHIFT(40),
  [140] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_type, 3, 0, 5),
  [142] = {.entry = {.count = 1, .reusable = true}}, SHIFT(2),
  [144] = {.entry = {.count = 1, .reusable = true}}, SHIFT(29),
  [146] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_type, 2, 0, 4),
  [148] = {.entry = {.count = 1, .reusable = true}}, SHIFT(12),
  [150] = {.entry = {.count = 1, .reusable = true}},  ACCEPT_INPUT(),
};

#ifdef __cplusplus
extern "C" {
#endif
#ifdef TREE_SITTER_HIDE_SYMBOLS
#define TS_PUBLIC
#elif defined(_WIN32)
#define TS_PUBLIC __declspec(dllexport)
#else
#define TS_PUBLIC __attribute__((visibility("default")))
#endif

TS_PUBLIC const TSLanguage *tree_sitter_fearless(void) {
  static const TSLanguage language = {
    .version = LANGUAGE_VERSION,
    .symbol_count = SYMBOL_COUNT,
    .alias_count = ALIAS_COUNT,
    .token_count = TOKEN_COUNT,
    .external_token_count = EXTERNAL_TOKEN_COUNT,
    .state_count = STATE_COUNT,
    .large_state_count = LARGE_STATE_COUNT,
    .production_id_count = PRODUCTION_ID_COUNT,
    .field_count = FIELD_COUNT,
    .max_alias_sequence_length = MAX_ALIAS_SEQUENCE_LENGTH,
    .parse_table = &ts_parse_table[0][0],
    .small_parse_table = ts_small_parse_table,
    .small_parse_table_map = ts_small_parse_table_map,
    .parse_actions = ts_parse_actions,
    .symbol_names = ts_symbol_names,
    .field_names = ts_field_names,
    .field_map_slices = ts_field_map_slices,
    .field_map_entries = ts_field_map_entries,
    .symbol_metadata = ts_symbol_metadata,
    .public_symbol_map = ts_symbol_map,
    .alias_map = ts_non_terminal_alias_map,
    .alias_sequences = &ts_alias_sequences[0][0],
    .lex_modes = ts_lex_modes,
    .lex_fn = ts_lex,
    .primary_state_ids = ts_primary_state_ids,
  };
  return &language;
}
#ifdef __cplusplus
}
#endif
