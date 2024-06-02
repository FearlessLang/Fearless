#include "tree_sitter/parser.h"

#if defined(__GNUC__) || defined(__clang__)
#pragma GCC diagnostic ignored "-Wmissing-field-initializers"
#endif

#define LANGUAGE_VERSION 14
#define STATE_COUNT 71
#define LARGE_STATE_COUNT 2
#define SYMBOL_COUNT 32
#define ALIAS_COUNT 0
#define TOKEN_COUNT 15
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
  anon_sym_LBRACK = 8,
  anon_sym_RBRACK = 9,
  sym_topDec = 10,
  aux_sym__idLow_token1 = 11,
  aux_sym__idLow_token2 = 12,
  sym__idUp = 13,
  sym__idChar = 14,
  sym_source_file = 15,
  sym_package = 16,
  sym__fullPkgName = 17,
  sym_packagePath = 18,
  sym__pkgName = 19,
  sym_alias = 20,
  sym_concreteType = 21,
  sym_typeName = 22,
  sym_concreteTypes = 23,
  sym__idLow = 24,
  aux_sym_source_file_repeat1 = 25,
  aux_sym_source_file_repeat2 = 26,
  aux_sym__fullPkgName_repeat1 = 27,
  aux_sym_packagePath_repeat1 = 28,
  aux_sym__pkgName_repeat1 = 29,
  aux_sym_typeName_repeat1 = 30,
  aux_sym_concreteTypes_repeat1 = 31,
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
  [anon_sym_LBRACK] = "[",
  [anon_sym_RBRACK] = "]",
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
  [sym_concreteType] = "concreteType",
  [sym_typeName] = "typeName",
  [sym_concreteTypes] = "concreteTypes",
  [sym__idLow] = "_idLow",
  [aux_sym_source_file_repeat1] = "source_file_repeat1",
  [aux_sym_source_file_repeat2] = "source_file_repeat2",
  [aux_sym__fullPkgName_repeat1] = "_fullPkgName_repeat1",
  [aux_sym_packagePath_repeat1] = "packagePath_repeat1",
  [aux_sym__pkgName_repeat1] = "_pkgName_repeat1",
  [aux_sym_typeName_repeat1] = "typeName_repeat1",
  [aux_sym_concreteTypes_repeat1] = "concreteTypes_repeat1",
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
  [anon_sym_LBRACK] = anon_sym_LBRACK,
  [anon_sym_RBRACK] = anon_sym_RBRACK,
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
  [sym_concreteType] = sym_concreteType,
  [sym_typeName] = sym_typeName,
  [sym_concreteTypes] = sym_concreteTypes,
  [sym__idLow] = sym__idLow,
  [aux_sym_source_file_repeat1] = aux_sym_source_file_repeat1,
  [aux_sym_source_file_repeat2] = aux_sym_source_file_repeat2,
  [aux_sym__fullPkgName_repeat1] = aux_sym__fullPkgName_repeat1,
  [aux_sym_packagePath_repeat1] = aux_sym_packagePath_repeat1,
  [aux_sym__pkgName_repeat1] = aux_sym__pkgName_repeat1,
  [aux_sym_typeName_repeat1] = aux_sym_typeName_repeat1,
  [aux_sym_concreteTypes_repeat1] = aux_sym_concreteTypes_repeat1,
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
  [anon_sym_LBRACK] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_RBRACK] = {
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
  [sym_concreteType] = {
    .visible = true,
    .named = true,
  },
  [sym_typeName] = {
    .visible = true,
    .named = true,
  },
  [sym_concreteTypes] = {
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
  [aux_sym_concreteTypes_repeat1] = {
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
  [3] = 2,
  [4] = 4,
  [5] = 5,
  [6] = 6,
  [7] = 7,
  [8] = 8,
  [9] = 9,
  [10] = 10,
  [11] = 11,
  [12] = 12,
  [13] = 8,
  [14] = 14,
  [15] = 15,
  [16] = 7,
  [17] = 6,
  [18] = 18,
  [19] = 19,
  [20] = 20,
  [21] = 21,
  [22] = 22,
  [23] = 23,
  [24] = 6,
  [25] = 25,
  [26] = 26,
  [27] = 22,
  [28] = 28,
  [29] = 29,
  [30] = 30,
  [31] = 23,
  [32] = 32,
  [33] = 21,
  [34] = 34,
  [35] = 35,
  [36] = 36,
  [37] = 37,
  [38] = 38,
  [39] = 32,
  [40] = 29,
  [41] = 37,
  [42] = 42,
  [43] = 43,
  [44] = 44,
  [45] = 36,
  [46] = 46,
  [47] = 14,
  [48] = 9,
  [49] = 49,
  [50] = 50,
  [51] = 51,
  [52] = 52,
  [53] = 53,
  [54] = 49,
  [55] = 55,
  [56] = 56,
  [57] = 57,
  [58] = 53,
  [59] = 59,
  [60] = 60,
  [61] = 61,
  [62] = 62,
  [63] = 50,
  [64] = 57,
  [65] = 65,
  [66] = 52,
  [67] = 67,
  [68] = 68,
  [69] = 55,
  [70] = 70,
};

static bool ts_lex(TSLexer *lexer, TSStateId state) {
  START_LEXER();
  eof = lexer->eof(lexer);
  switch (state) {
    case 0:
      if (eof) ADVANCE(21);
      if (lookahead == '\'') ADVANCE(28);
      if (lookahead == ',') ADVANCE(27);
      if (lookahead == '.') ADVANCE(24);
      if (lookahead == '[') ADVANCE(29);
      if (lookahead == ']') ADVANCE(30);
      if (lookahead == 'a') ADVANCE(36);
      if (lookahead == 't') ADVANCE(37);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(0);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('b' <= lookahead && lookahead <= 'z')) ADVANCE(35);
      END_STATE();
    case 1:
      if (lookahead == ' ') ADVANCE(23);
      if (lookahead == '\'') ADVANCE(28);
      if (lookahead == '[') ADVANCE(29);
      if (('\t' <= lookahead && lookahead <= '\r')) SKIP(1);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(35);
      END_STATE();
    case 2:
      if (lookahead == '\'') ADVANCE(28);
      if (lookahead == ',') ADVANCE(27);
      if (lookahead == '.') ADVANCE(24);
      if (lookahead == '[') ADVANCE(29);
      if (lookahead == ']') ADVANCE(30);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(2);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(35);
      END_STATE();
    case 3:
      if (lookahead == 'D') ADVANCE(13);
      END_STATE();
    case 4:
      if (lookahead == ']') ADVANCE(30);
      if (lookahead == '_') ADVANCE(7);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(4);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(34);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(32);
      END_STATE();
    case 5:
      if (lookahead == '_') ADVANCE(6);
      if (lookahead == 'a') ADVANCE(20);
      if (lookahead == 'p') ADVANCE(10);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(5);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(34);
      END_STATE();
    case 6:
      if (lookahead == '_') ADVANCE(6);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(34);
      END_STATE();
    case 7:
      if (lookahead == '_') ADVANCE(7);
      if (('0' <= lookahead && lookahead <= '9')) ADVANCE(33);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(34);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(32);
      END_STATE();
    case 8:
      if (lookahead == 'a') ADVANCE(19);
      END_STATE();
    case 9:
      if (lookahead == 'a') ADVANCE(15);
      END_STATE();
    case 10:
      if (lookahead == 'a') ADVANCE(12);
      END_STATE();
    case 11:
      if (lookahead == 'c') ADVANCE(31);
      END_STATE();
    case 12:
      if (lookahead == 'c') ADVANCE(17);
      END_STATE();
    case 13:
      if (lookahead == 'e') ADVANCE(11);
      END_STATE();
    case 14:
      if (lookahead == 'e') ADVANCE(22);
      END_STATE();
    case 15:
      if (lookahead == 'g') ADVANCE(14);
      END_STATE();
    case 16:
      if (lookahead == 'i') ADVANCE(8);
      END_STATE();
    case 17:
      if (lookahead == 'k') ADVANCE(9);
      END_STATE();
    case 18:
      if (lookahead == 'p') ADVANCE(3);
      END_STATE();
    case 19:
      if (lookahead == 's') ADVANCE(25);
      END_STATE();
    case 20:
      if (lookahead == 's') ADVANCE(26);
      END_STATE();
    case 21:
      ACCEPT_TOKEN(ts_builtin_sym_end);
      END_STATE();
    case 22:
      ACCEPT_TOKEN(anon_sym_package);
      END_STATE();
    case 23:
      ACCEPT_TOKEN(anon_sym_SPACE);
      if (lookahead == ' ') ADVANCE(23);
      END_STATE();
    case 24:
      ACCEPT_TOKEN(anon_sym_DOT);
      END_STATE();
    case 25:
      ACCEPT_TOKEN(anon_sym_alias);
      END_STATE();
    case 26:
      ACCEPT_TOKEN(anon_sym_as);
      END_STATE();
    case 27:
      ACCEPT_TOKEN(anon_sym_COMMA);
      END_STATE();
    case 28:
      ACCEPT_TOKEN(anon_sym_SQUOTE);
      END_STATE();
    case 29:
      ACCEPT_TOKEN(anon_sym_LBRACK);
      END_STATE();
    case 30:
      ACCEPT_TOKEN(anon_sym_RBRACK);
      END_STATE();
    case 31:
      ACCEPT_TOKEN(sym_topDec);
      END_STATE();
    case 32:
      ACCEPT_TOKEN(aux_sym__idLow_token1);
      END_STATE();
    case 33:
      ACCEPT_TOKEN(aux_sym__idLow_token2);
      END_STATE();
    case 34:
      ACCEPT_TOKEN(sym__idUp);
      END_STATE();
    case 35:
      ACCEPT_TOKEN(sym__idChar);
      END_STATE();
    case 36:
      ACCEPT_TOKEN(sym__idChar);
      if (lookahead == 'l') ADVANCE(16);
      END_STATE();
    case 37:
      ACCEPT_TOKEN(sym__idChar);
      if (lookahead == 'o') ADVANCE(18);
      END_STATE();
    default:
      return false;
  }
}

static const TSLexMode ts_lex_modes[STATE_COUNT] = {
  [0] = {.lex_state = 0},
  [1] = {.lex_state = 5},
  [2] = {.lex_state = 4},
  [3] = {.lex_state = 4},
  [4] = {.lex_state = 4},
  [5] = {.lex_state = 4},
  [6] = {.lex_state = 2},
  [7] = {.lex_state = 2},
  [8] = {.lex_state = 2},
  [9] = {.lex_state = 0},
  [10] = {.lex_state = 0},
  [11] = {.lex_state = 0},
  [12] = {.lex_state = 4},
  [13] = {.lex_state = 1},
  [14] = {.lex_state = 0},
  [15] = {.lex_state = 4},
  [16] = {.lex_state = 1},
  [17] = {.lex_state = 0},
  [18] = {.lex_state = 0},
  [19] = {.lex_state = 0},
  [20] = {.lex_state = 4},
  [21] = {.lex_state = 0},
  [22] = {.lex_state = 0},
  [23] = {.lex_state = 0},
  [24] = {.lex_state = 1},
  [25] = {.lex_state = 0},
  [26] = {.lex_state = 0},
  [27] = {.lex_state = 1},
  [28] = {.lex_state = 4},
  [29] = {.lex_state = 0},
  [30] = {.lex_state = 0},
  [31] = {.lex_state = 1},
  [32] = {.lex_state = 0},
  [33] = {.lex_state = 1},
  [34] = {.lex_state = 4},
  [35] = {.lex_state = 0},
  [36] = {.lex_state = 0},
  [37] = {.lex_state = 0},
  [38] = {.lex_state = 0},
  [39] = {.lex_state = 1},
  [40] = {.lex_state = 1},
  [41] = {.lex_state = 0},
  [42] = {.lex_state = 0},
  [43] = {.lex_state = 0},
  [44] = {.lex_state = 0},
  [45] = {.lex_state = 0},
  [46] = {.lex_state = 0},
  [47] = {.lex_state = 2},
  [48] = {.lex_state = 2},
  [49] = {.lex_state = 5},
  [50] = {.lex_state = 0},
  [51] = {.lex_state = 0},
  [52] = {.lex_state = 0},
  [53] = {.lex_state = 0},
  [54] = {.lex_state = 5},
  [55] = {.lex_state = 0},
  [56] = {.lex_state = 5},
  [57] = {.lex_state = 0},
  [58] = {.lex_state = 1},
  [59] = {.lex_state = 1},
  [60] = {.lex_state = 1},
  [61] = {.lex_state = 0},
  [62] = {.lex_state = 5},
  [63] = {.lex_state = 1},
  [64] = {.lex_state = 1},
  [65] = {.lex_state = 0},
  [66] = {.lex_state = 1},
  [67] = {.lex_state = 1},
  [68] = {.lex_state = 0},
  [69] = {.lex_state = 1},
  [70] = {.lex_state = 1},
};

static const uint16_t ts_parse_table[LARGE_STATE_COUNT][SYMBOL_COUNT] = {
  [0] = {
    [ts_builtin_sym_end] = ACTIONS(1),
    [anon_sym_DOT] = ACTIONS(1),
    [anon_sym_alias] = ACTIONS(1),
    [anon_sym_COMMA] = ACTIONS(1),
    [anon_sym_SQUOTE] = ACTIONS(1),
    [anon_sym_LBRACK] = ACTIONS(1),
    [anon_sym_RBRACK] = ACTIONS(1),
    [sym_topDec] = ACTIONS(1),
    [sym__idChar] = ACTIONS(1),
  },
  [1] = {
    [sym_source_file] = STATE(68),
    [sym_package] = STATE(10),
    [anon_sym_package] = ACTIONS(3),
  },
};

static const uint16_t ts_small_parse_table[] = {
  [0] = 9,
    ACTIONS(5), 1,
      anon_sym_RBRACK,
    ACTIONS(9), 1,
      sym__idUp,
    STATE(15), 1,
      aux_sym_packagePath_repeat1,
    STATE(32), 1,
      sym_typeName,
    STATE(37), 1,
      sym_concreteType,
    STATE(47), 1,
      sym__idLow,
    STATE(54), 1,
      sym_packagePath,
    STATE(61), 1,
      sym__pkgName,
    ACTIONS(7), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [29] = 9,
    ACTIONS(9), 1,
      sym__idUp,
    ACTIONS(11), 1,
      anon_sym_RBRACK,
    STATE(15), 1,
      aux_sym_packagePath_repeat1,
    STATE(32), 1,
      sym_typeName,
    STATE(41), 1,
      sym_concreteType,
    STATE(47), 1,
      sym__idLow,
    STATE(54), 1,
      sym_packagePath,
    STATE(61), 1,
      sym__pkgName,
    ACTIONS(7), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [58] = 8,
    ACTIONS(9), 1,
      sym__idUp,
    STATE(15), 1,
      aux_sym_packagePath_repeat1,
    STATE(32), 1,
      sym_typeName,
    STATE(47), 1,
      sym__idLow,
    STATE(51), 1,
      sym_concreteType,
    STATE(54), 1,
      sym_packagePath,
    STATE(61), 1,
      sym__pkgName,
    ACTIONS(7), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [84] = 8,
    ACTIONS(13), 1,
      sym__idUp,
    STATE(15), 1,
      aux_sym_packagePath_repeat1,
    STATE(39), 1,
      sym_typeName,
    STATE(47), 1,
      sym__idLow,
    STATE(49), 1,
      sym_packagePath,
    STATE(59), 1,
      sym_concreteType,
    STATE(61), 1,
      sym__pkgName,
    ACTIONS(7), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [110] = 3,
    ACTIONS(17), 1,
      sym__idChar,
    STATE(6), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(15), 5,
      anon_sym_DOT,
      anon_sym_COMMA,
      anon_sym_SQUOTE,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
  [124] = 5,
    ACTIONS(22), 1,
      anon_sym_SQUOTE,
    ACTIONS(24), 1,
      sym__idChar,
    STATE(6), 1,
      aux_sym__pkgName_repeat1,
    STATE(22), 1,
      aux_sym_typeName_repeat1,
    ACTIONS(20), 3,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
  [142] = 5,
    ACTIONS(28), 1,
      anon_sym_SQUOTE,
    ACTIONS(30), 1,
      sym__idChar,
    STATE(7), 1,
      aux_sym__pkgName_repeat1,
    STATE(23), 1,
      aux_sym_typeName_repeat1,
    ACTIONS(26), 3,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
  [160] = 3,
    ACTIONS(34), 1,
      sym__idChar,
    STATE(17), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(32), 4,
      ts_builtin_sym_end,
      anon_sym_DOT,
      anon_sym_alias,
      sym_topDec,
  [173] = 5,
    ACTIONS(36), 1,
      ts_builtin_sym_end,
    ACTIONS(38), 1,
      anon_sym_alias,
    ACTIONS(40), 1,
      sym_topDec,
    STATE(44), 1,
      aux_sym_source_file_repeat2,
    STATE(11), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [190] = 5,
    ACTIONS(38), 1,
      anon_sym_alias,
    ACTIONS(42), 1,
      ts_builtin_sym_end,
    ACTIONS(44), 1,
      sym_topDec,
    STATE(42), 1,
      aux_sym_source_file_repeat2,
    STATE(19), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [207] = 5,
    ACTIONS(49), 1,
      sym__idUp,
    STATE(12), 1,
      aux_sym_packagePath_repeat1,
    STATE(47), 1,
      sym__idLow,
    STATE(61), 1,
      sym__pkgName,
    ACTIONS(46), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [224] = 6,
    ACTIONS(26), 1,
      anon_sym_SPACE,
    ACTIONS(51), 1,
      anon_sym_SQUOTE,
    ACTIONS(53), 1,
      anon_sym_LBRACK,
    ACTIONS(55), 1,
      sym__idChar,
    STATE(16), 1,
      aux_sym__pkgName_repeat1,
    STATE(31), 1,
      aux_sym_typeName_repeat1,
  [243] = 3,
    ACTIONS(59), 1,
      sym__idChar,
    STATE(9), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(57), 4,
      ts_builtin_sym_end,
      anon_sym_DOT,
      anon_sym_alias,
      sym_topDec,
  [256] = 5,
    ACTIONS(61), 1,
      sym__idUp,
    STATE(12), 1,
      aux_sym_packagePath_repeat1,
    STATE(47), 1,
      sym__idLow,
    STATE(61), 1,
      sym__pkgName,
    ACTIONS(7), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [273] = 6,
    ACTIONS(20), 1,
      anon_sym_SPACE,
    ACTIONS(63), 1,
      anon_sym_SQUOTE,
    ACTIONS(65), 1,
      anon_sym_LBRACK,
    ACTIONS(67), 1,
      sym__idChar,
    STATE(24), 1,
      aux_sym__pkgName_repeat1,
    STATE(27), 1,
      aux_sym_typeName_repeat1,
  [292] = 3,
    ACTIONS(69), 1,
      sym__idChar,
    STATE(17), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(15), 4,
      ts_builtin_sym_end,
      anon_sym_DOT,
      anon_sym_alias,
      sym_topDec,
  [305] = 3,
    ACTIONS(74), 1,
      anon_sym_DOT,
    STATE(25), 1,
      aux_sym__fullPkgName_repeat1,
    ACTIONS(72), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [317] = 3,
    ACTIONS(78), 1,
      anon_sym_alias,
    ACTIONS(76), 2,
      ts_builtin_sym_end,
      sym_topDec,
    STATE(19), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [329] = 4,
    STATE(14), 1,
      sym__idLow,
    STATE(26), 1,
      sym__pkgName,
    STATE(43), 1,
      sym__fullPkgName,
    ACTIONS(81), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [343] = 3,
    ACTIONS(85), 1,
      anon_sym_SQUOTE,
    STATE(21), 1,
      aux_sym_typeName_repeat1,
    ACTIONS(83), 3,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
  [355] = 3,
    ACTIONS(90), 1,
      anon_sym_SQUOTE,
    STATE(21), 1,
      aux_sym_typeName_repeat1,
    ACTIONS(88), 3,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
  [367] = 3,
    ACTIONS(90), 1,
      anon_sym_SQUOTE,
    STATE(21), 1,
      aux_sym_typeName_repeat1,
    ACTIONS(20), 3,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
  [379] = 4,
    ACTIONS(15), 1,
      anon_sym_SPACE,
    ACTIONS(94), 1,
      sym__idChar,
    STATE(24), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(92), 2,
      anon_sym_SQUOTE,
      anon_sym_LBRACK,
  [393] = 3,
    ACTIONS(99), 1,
      anon_sym_DOT,
    STATE(25), 1,
      aux_sym__fullPkgName_repeat1,
    ACTIONS(97), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [405] = 3,
    ACTIONS(74), 1,
      anon_sym_DOT,
    STATE(18), 1,
      aux_sym__fullPkgName_repeat1,
    ACTIONS(102), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [417] = 4,
    ACTIONS(88), 1,
      anon_sym_SPACE,
    ACTIONS(104), 1,
      anon_sym_SQUOTE,
    ACTIONS(106), 1,
      anon_sym_LBRACK,
    STATE(33), 1,
      aux_sym_typeName_repeat1,
  [430] = 3,
    STATE(14), 1,
      sym__idLow,
    STATE(30), 1,
      sym__pkgName,
    ACTIONS(81), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [441] = 3,
    ACTIONS(110), 1,
      anon_sym_LBRACK,
    STATE(55), 1,
      sym_concreteTypes,
    ACTIONS(108), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [452] = 1,
    ACTIONS(97), 4,
      ts_builtin_sym_end,
      anon_sym_DOT,
      anon_sym_alias,
      sym_topDec,
  [459] = 4,
    ACTIONS(20), 1,
      anon_sym_SPACE,
    ACTIONS(65), 1,
      anon_sym_LBRACK,
    ACTIONS(104), 1,
      anon_sym_SQUOTE,
    STATE(33), 1,
      aux_sym_typeName_repeat1,
  [472] = 3,
    ACTIONS(110), 1,
      anon_sym_LBRACK,
    STATE(57), 1,
      sym_concreteTypes,
    ACTIONS(112), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [483] = 4,
    ACTIONS(83), 1,
      anon_sym_SPACE,
    ACTIONS(114), 1,
      anon_sym_SQUOTE,
    ACTIONS(117), 1,
      anon_sym_LBRACK,
    STATE(33), 1,
      aux_sym_typeName_repeat1,
  [496] = 1,
    ACTIONS(49), 3,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
      sym__idUp,
  [502] = 3,
    ACTIONS(119), 1,
      anon_sym_COMMA,
    ACTIONS(122), 1,
      anon_sym_RBRACK,
    STATE(35), 1,
      aux_sym_concreteTypes_repeat1,
  [512] = 3,
    ACTIONS(124), 1,
      anon_sym_COMMA,
    ACTIONS(126), 1,
      anon_sym_RBRACK,
    STATE(35), 1,
      aux_sym_concreteTypes_repeat1,
  [522] = 3,
    ACTIONS(124), 1,
      anon_sym_COMMA,
    ACTIONS(128), 1,
      anon_sym_RBRACK,
    STATE(36), 1,
      aux_sym_concreteTypes_repeat1,
  [532] = 3,
    ACTIONS(130), 1,
      ts_builtin_sym_end,
    ACTIONS(132), 1,
      sym_topDec,
    STATE(38), 1,
      aux_sym_source_file_repeat2,
  [542] = 3,
    ACTIONS(112), 1,
      anon_sym_SPACE,
    ACTIONS(135), 1,
      anon_sym_LBRACK,
    STATE(64), 1,
      sym_concreteTypes,
  [552] = 3,
    ACTIONS(108), 1,
      anon_sym_SPACE,
    ACTIONS(135), 1,
      anon_sym_LBRACK,
    STATE(69), 1,
      sym_concreteTypes,
  [562] = 3,
    ACTIONS(124), 1,
      anon_sym_COMMA,
    ACTIONS(137), 1,
      anon_sym_RBRACK,
    STATE(45), 1,
      aux_sym_concreteTypes_repeat1,
  [572] = 3,
    ACTIONS(139), 1,
      ts_builtin_sym_end,
    ACTIONS(141), 1,
      sym_topDec,
    STATE(38), 1,
      aux_sym_source_file_repeat2,
  [582] = 1,
    ACTIONS(143), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [588] = 3,
    ACTIONS(42), 1,
      ts_builtin_sym_end,
    ACTIONS(141), 1,
      sym_topDec,
    STATE(38), 1,
      aux_sym_source_file_repeat2,
  [598] = 3,
    ACTIONS(124), 1,
      anon_sym_COMMA,
    ACTIONS(145), 1,
      anon_sym_RBRACK,
    STATE(35), 1,
      aux_sym_concreteTypes_repeat1,
  [608] = 1,
    ACTIONS(147), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [614] = 3,
    ACTIONS(57), 1,
      anon_sym_DOT,
    ACTIONS(149), 1,
      sym__idChar,
    STATE(48), 1,
      aux_sym__pkgName_repeat1,
  [624] = 3,
    ACTIONS(24), 1,
      sym__idChar,
    ACTIONS(32), 1,
      anon_sym_DOT,
    STATE(6), 1,
      aux_sym__pkgName_repeat1,
  [634] = 2,
    ACTIONS(13), 1,
      sym__idUp,
    STATE(40), 1,
      sym_typeName,
  [641] = 1,
    ACTIONS(151), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [646] = 1,
    ACTIONS(122), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [651] = 1,
    ACTIONS(153), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [656] = 1,
    ACTIONS(155), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [661] = 2,
    ACTIONS(9), 1,
      sym__idUp,
    STATE(29), 1,
      sym_typeName,
  [668] = 1,
    ACTIONS(157), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [673] = 2,
    ACTIONS(9), 1,
      sym__idUp,
    STATE(65), 1,
      sym_typeName,
  [680] = 1,
    ACTIONS(159), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [685] = 1,
    ACTIONS(155), 1,
      anon_sym_SPACE,
  [689] = 1,
    ACTIONS(161), 1,
      anon_sym_SPACE,
  [693] = 1,
    ACTIONS(163), 1,
      anon_sym_SPACE,
  [697] = 1,
    ACTIONS(165), 1,
      anon_sym_DOT,
  [701] = 1,
    ACTIONS(167), 1,
      anon_sym_as,
  [705] = 1,
    ACTIONS(151), 1,
      anon_sym_SPACE,
  [709] = 1,
    ACTIONS(159), 1,
      anon_sym_SPACE,
  [713] = 1,
    ACTIONS(169), 1,
      anon_sym_COMMA,
  [717] = 1,
    ACTIONS(153), 1,
      anon_sym_SPACE,
  [721] = 1,
    ACTIONS(171), 1,
      anon_sym_SPACE,
  [725] = 1,
    ACTIONS(173), 1,
      ts_builtin_sym_end,
  [729] = 1,
    ACTIONS(157), 1,
      anon_sym_SPACE,
  [733] = 1,
    ACTIONS(175), 1,
      anon_sym_SPACE,
};

static const uint32_t ts_small_parse_table_map[] = {
  [SMALL_STATE(2)] = 0,
  [SMALL_STATE(3)] = 29,
  [SMALL_STATE(4)] = 58,
  [SMALL_STATE(5)] = 84,
  [SMALL_STATE(6)] = 110,
  [SMALL_STATE(7)] = 124,
  [SMALL_STATE(8)] = 142,
  [SMALL_STATE(9)] = 160,
  [SMALL_STATE(10)] = 173,
  [SMALL_STATE(11)] = 190,
  [SMALL_STATE(12)] = 207,
  [SMALL_STATE(13)] = 224,
  [SMALL_STATE(14)] = 243,
  [SMALL_STATE(15)] = 256,
  [SMALL_STATE(16)] = 273,
  [SMALL_STATE(17)] = 292,
  [SMALL_STATE(18)] = 305,
  [SMALL_STATE(19)] = 317,
  [SMALL_STATE(20)] = 329,
  [SMALL_STATE(21)] = 343,
  [SMALL_STATE(22)] = 355,
  [SMALL_STATE(23)] = 367,
  [SMALL_STATE(24)] = 379,
  [SMALL_STATE(25)] = 393,
  [SMALL_STATE(26)] = 405,
  [SMALL_STATE(27)] = 417,
  [SMALL_STATE(28)] = 430,
  [SMALL_STATE(29)] = 441,
  [SMALL_STATE(30)] = 452,
  [SMALL_STATE(31)] = 459,
  [SMALL_STATE(32)] = 472,
  [SMALL_STATE(33)] = 483,
  [SMALL_STATE(34)] = 496,
  [SMALL_STATE(35)] = 502,
  [SMALL_STATE(36)] = 512,
  [SMALL_STATE(37)] = 522,
  [SMALL_STATE(38)] = 532,
  [SMALL_STATE(39)] = 542,
  [SMALL_STATE(40)] = 552,
  [SMALL_STATE(41)] = 562,
  [SMALL_STATE(42)] = 572,
  [SMALL_STATE(43)] = 582,
  [SMALL_STATE(44)] = 588,
  [SMALL_STATE(45)] = 598,
  [SMALL_STATE(46)] = 608,
  [SMALL_STATE(47)] = 614,
  [SMALL_STATE(48)] = 624,
  [SMALL_STATE(49)] = 634,
  [SMALL_STATE(50)] = 641,
  [SMALL_STATE(51)] = 646,
  [SMALL_STATE(52)] = 651,
  [SMALL_STATE(53)] = 656,
  [SMALL_STATE(54)] = 661,
  [SMALL_STATE(55)] = 668,
  [SMALL_STATE(56)] = 673,
  [SMALL_STATE(57)] = 680,
  [SMALL_STATE(58)] = 685,
  [SMALL_STATE(59)] = 689,
  [SMALL_STATE(60)] = 693,
  [SMALL_STATE(61)] = 697,
  [SMALL_STATE(62)] = 701,
  [SMALL_STATE(63)] = 705,
  [SMALL_STATE(64)] = 709,
  [SMALL_STATE(65)] = 713,
  [SMALL_STATE(66)] = 717,
  [SMALL_STATE(67)] = 721,
  [SMALL_STATE(68)] = 725,
  [SMALL_STATE(69)] = 729,
  [SMALL_STATE(70)] = 733,
};

static const TSParseActionEntry ts_parse_actions[] = {
  [0] = {.entry = {.count = 0, .reusable = false}},
  [1] = {.entry = {.count = 1, .reusable = false}}, RECOVER(),
  [3] = {.entry = {.count = 1, .reusable = true}}, SHIFT(70),
  [5] = {.entry = {.count = 1, .reusable = true}}, SHIFT(53),
  [7] = {.entry = {.count = 1, .reusable = true}}, SHIFT(47),
  [9] = {.entry = {.count = 1, .reusable = true}}, SHIFT(8),
  [11] = {.entry = {.count = 1, .reusable = true}}, SHIFT(58),
  [13] = {.entry = {.count = 1, .reusable = true}}, SHIFT(13),
  [15] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0),
  [17] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(6),
  [20] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_typeName, 2, 0, 0),
  [22] = {.entry = {.count = 1, .reusable = true}}, SHIFT(22),
  [24] = {.entry = {.count = 1, .reusable = true}}, SHIFT(6),
  [26] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_typeName, 1, 0, 0),
  [28] = {.entry = {.count = 1, .reusable = true}}, SHIFT(23),
  [30] = {.entry = {.count = 1, .reusable = true}}, SHIFT(7),
  [32] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__pkgName, 2, 0, 0),
  [34] = {.entry = {.count = 1, .reusable = false}}, SHIFT(17),
  [36] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 1, 0, 0),
  [38] = {.entry = {.count = 1, .reusable = true}}, SHIFT(60),
  [40] = {.entry = {.count = 1, .reusable = true}}, SHIFT(44),
  [42] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 2, 0, 0),
  [44] = {.entry = {.count = 1, .reusable = true}}, SHIFT(42),
  [46] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_packagePath_repeat1, 2, 0, 0), SHIFT_REPEAT(47),
  [49] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_packagePath_repeat1, 2, 0, 0),
  [51] = {.entry = {.count = 1, .reusable = false}}, SHIFT(31),
  [53] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym_typeName, 1, 0, 0),
  [55] = {.entry = {.count = 1, .reusable = false}}, SHIFT(16),
  [57] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__pkgName, 1, 0, 0),
  [59] = {.entry = {.count = 1, .reusable = false}}, SHIFT(9),
  [61] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_packagePath, 1, 0, 0),
  [63] = {.entry = {.count = 1, .reusable = false}}, SHIFT(27),
  [65] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym_typeName, 2, 0, 0),
  [67] = {.entry = {.count = 1, .reusable = false}}, SHIFT(24),
  [69] = {.entry = {.count = 2, .reusable = false}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(17),
  [72] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fullPkgName, 2, 0, 0),
  [74] = {.entry = {.count = 1, .reusable = true}}, SHIFT(28),
  [76] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0),
  [78] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0), SHIFT_REPEAT(60),
  [81] = {.entry = {.count = 1, .reusable = true}}, SHIFT(14),
  [83] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_typeName_repeat1, 2, 0, 0),
  [85] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_typeName_repeat1, 2, 0, 0), SHIFT_REPEAT(21),
  [88] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_typeName, 3, 0, 0),
  [90] = {.entry = {.count = 1, .reusable = true}}, SHIFT(21),
  [92] = {.entry = {.count = 1, .reusable = false}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0),
  [94] = {.entry = {.count = 2, .reusable = false}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(24),
  [97] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__fullPkgName_repeat1, 2, 0, 0),
  [99] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__fullPkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(28),
  [102] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fullPkgName, 1, 0, 0),
  [104] = {.entry = {.count = 1, .reusable = false}}, SHIFT(33),
  [106] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym_typeName, 3, 0, 0),
  [108] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteType, 2, 0, 3),
  [110] = {.entry = {.count = 1, .reusable = true}}, SHIFT(2),
  [112] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteType, 1, 0, 2),
  [114] = {.entry = {.count = 2, .reusable = false}}, REDUCE(aux_sym_typeName_repeat1, 2, 0, 0), SHIFT_REPEAT(33),
  [117] = {.entry = {.count = 1, .reusable = false}}, REDUCE(aux_sym_typeName_repeat1, 2, 0, 0),
  [119] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_concreteTypes_repeat1, 2, 0, 0), SHIFT_REPEAT(4),
  [122] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_concreteTypes_repeat1, 2, 0, 0),
  [124] = {.entry = {.count = 1, .reusable = true}}, SHIFT(4),
  [126] = {.entry = {.count = 1, .reusable = true}}, SHIFT(52),
  [128] = {.entry = {.count = 1, .reusable = true}}, SHIFT(50),
  [130] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0),
  [132] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0), SHIFT_REPEAT(38),
  [135] = {.entry = {.count = 1, .reusable = false}}, SHIFT(3),
  [137] = {.entry = {.count = 1, .reusable = true}}, SHIFT(63),
  [139] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 3, 0, 0),
  [141] = {.entry = {.count = 1, .reusable = true}}, SHIFT(38),
  [143] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_package, 3, 0, 1),
  [145] = {.entry = {.count = 1, .reusable = true}}, SHIFT(66),
  [147] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_alias, 8, 0, 6),
  [149] = {.entry = {.count = 1, .reusable = true}}, SHIFT(48),
  [151] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteTypes, 3, 0, 0),
  [153] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteTypes, 4, 0, 0),
  [155] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteTypes, 2, 0, 0),
  [157] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteType, 3, 0, 5),
  [159] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteType, 2, 0, 4),
  [161] = {.entry = {.count = 1, .reusable = true}}, SHIFT(62),
  [163] = {.entry = {.count = 1, .reusable = true}}, SHIFT(5),
  [165] = {.entry = {.count = 1, .reusable = true}}, SHIFT(34),
  [167] = {.entry = {.count = 1, .reusable = true}}, SHIFT(67),
  [169] = {.entry = {.count = 1, .reusable = true}}, SHIFT(46),
  [171] = {.entry = {.count = 1, .reusable = true}}, SHIFT(56),
  [173] = {.entry = {.count = 1, .reusable = true}},  ACCEPT_INPUT(),
  [175] = {.entry = {.count = 1, .reusable = true}}, SHIFT(20),
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
