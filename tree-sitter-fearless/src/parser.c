#include "tree_sitter/parser.h"

#if defined(__GNUC__) || defined(__clang__)
#pragma GCC diagnostic ignored "-Wmissing-field-initializers"
#endif

#define LANGUAGE_VERSION 14
#define STATE_COUNT 46
#define LARGE_STATE_COUNT 2
#define SYMBOL_COUNT 30
#define ALIAS_COUNT 0
#define TOKEN_COUNT 17
#define EXTERNAL_TOKEN_COUNT 0
#define FIELD_COUNT 5
#define MAX_ALIAS_SEQUENCE_LENGTH 5
#define PRODUCTION_ID_COUNT 7

enum ts_symbol_identifiers {
  anon_sym_package = 1,
  anon_sym_LF = 2,
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
  sym_packagePath = 19,
  sym__pkgName = 20,
  sym_alias = 21,
  sym_type = 22,
  sym_typeName = 23,
  sym__idLow = 24,
  aux_sym_source_file_repeat1 = 25,
  aux_sym_source_file_repeat2 = 26,
  aux_sym_packagePath_repeat1 = 27,
  aux_sym__pkgName_repeat1 = 28,
  aux_sym_typeName_repeat1 = 29,
};

static const char * const ts_symbol_names[] = {
  [ts_builtin_sym_end] = "end",
  [anon_sym_package] = "package ",
  [anon_sym_LF] = "\n",
  [anon_sym_DOT] = ".",
  [anon_sym_alias] = "alias ",
  [anon_sym_as] = " as ",
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
  [sym_packagePath] = "packagePath",
  [sym__pkgName] = "_pkgName",
  [sym_alias] = "alias",
  [sym_type] = "type",
  [sym_typeName] = "typeName",
  [sym__idLow] = "_idLow",
  [aux_sym_source_file_repeat1] = "source_file_repeat1",
  [aux_sym_source_file_repeat2] = "source_file_repeat2",
  [aux_sym_packagePath_repeat1] = "packagePath_repeat1",
  [aux_sym__pkgName_repeat1] = "_pkgName_repeat1",
  [aux_sym_typeName_repeat1] = "typeName_repeat1",
};

static const TSSymbol ts_symbol_map[] = {
  [ts_builtin_sym_end] = ts_builtin_sym_end,
  [anon_sym_package] = anon_sym_package,
  [anon_sym_LF] = anon_sym_LF,
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
  [sym_packagePath] = sym_packagePath,
  [sym__pkgName] = sym__pkgName,
  [sym_alias] = sym_alias,
  [sym_type] = sym_type,
  [sym_typeName] = sym_typeName,
  [sym__idLow] = sym__idLow,
  [aux_sym_source_file_repeat1] = aux_sym_source_file_repeat1,
  [aux_sym_source_file_repeat2] = aux_sym_source_file_repeat2,
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
  [anon_sym_LF] = {
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
  [4] = {.index = 4, .length = 3},
  [5] = {.index = 7, .length = 4},
  [6] = {.index = 11, .length = 2},
};

static const TSFieldMapEntry ts_field_map_entries[] = {
  [0] =
    {field_name, 1},
  [1] =
    {field_name, 0},
  [2] =
    {field_generic, 1},
    {field_name, 0},
  [4] =
    {field_name, 2},
    {field_package, 0},
    {field_package, 1},
  [7] =
    {field_generic, 3},
    {field_name, 2},
    {field_package, 0},
    {field_package, 1},
  [11] =
    {field_from, 1},
    {field_to, 3},
};

static const TSSymbol ts_alias_sequences[PRODUCTION_ID_COUNT][MAX_ALIAS_SEQUENCE_LENGTH] = {
  [0] = {0},
  [4] = {
    [0] = sym_packagePath,
    [1] = sym_packagePath,
  },
  [5] = {
    [0] = sym_packagePath,
    [1] = sym_packagePath,
  },
};

static const uint16_t ts_non_terminal_alias_map[] = {
  sym_packagePath, 2,
    sym_packagePath,
    sym_packagePath,
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
  [8] = 5,
  [9] = 9,
  [10] = 7,
  [11] = 9,
  [12] = 12,
  [13] = 13,
  [14] = 14,
  [15] = 15,
  [16] = 16,
  [17] = 15,
  [18] = 18,
  [19] = 19,
  [20] = 20,
  [21] = 15,
  [22] = 22,
  [23] = 23,
  [24] = 24,
  [25] = 25,
  [26] = 20,
  [27] = 19,
  [28] = 28,
  [29] = 29,
  [30] = 30,
  [31] = 31,
  [32] = 32,
  [33] = 33,
  [34] = 34,
  [35] = 35,
  [36] = 36,
  [37] = 37,
  [38] = 38,
  [39] = 39,
  [40] = 40,
  [41] = 35,
  [42] = 42,
  [43] = 30,
  [44] = 44,
  [45] = 45,
};

static bool ts_lex(TSLexer *lexer, TSStateId state) {
  START_LEXER();
  eof = lexer->eof(lexer);
  switch (state) {
    case 0:
      if (eof) ADVANCE(30);
      ADVANCE_MAP(
        ' ', 2,
        '\'', 37,
        ',', 36,
        '.', 33,
        ':', 41,
        '[', 39,
        ']', 40,
        't', 47,
      );
      if (('\t' <= lookahead && lookahead <= '\r')) SKIP(0);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(46);
      END_STATE();
    case 1:
      if (lookahead == '\n') ADVANCE(32);
      if (lookahead == '.') ADVANCE(33);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(1);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(46);
      END_STATE();
    case 2:
      ADVANCE_MAP(
        ' ', 2,
        '\'', 37,
        ',', 36,
        '.', 33,
        ':', 41,
        '[', 39,
        ']', 40,
        'a', 48,
        't', 47,
      );
      if (('\t' <= lookahead && lookahead <= '\r')) SKIP(3);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('b' <= lookahead && lookahead <= 'z')) ADVANCE(46);
      END_STATE();
    case 3:
      ADVANCE_MAP(
        ' ', 2,
        '\'', 37,
        ',', 36,
        '.', 33,
        ':', 41,
        '[', 39,
        ']', 40,
        't', 47,
      );
      if (('\t' <= lookahead && lookahead <= '\r')) SKIP(3);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(46);
      END_STATE();
    case 4:
      if (lookahead == ' ') ADVANCE(35);
      END_STATE();
    case 5:
      if (lookahead == ' ') ADVANCE(34);
      END_STATE();
    case 6:
      if (lookahead == ' ') ADVANCE(31);
      END_STATE();
    case 7:
      if (lookahead == '!') ADVANCE(38);
      END_STATE();
    case 8:
      if (lookahead == '\'') ADVANCE(37);
      if (lookahead == ',') ADVANCE(36);
      if (lookahead == '.') ADVANCE(33);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(8);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(46);
      END_STATE();
    case 9:
      if (lookahead == 'D') ADVANCE(19);
      END_STATE();
    case 10:
      if (lookahead == '_') ADVANCE(10);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(45);
      END_STATE();
    case 11:
      if (lookahead == '_') ADVANCE(12);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(11);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(45);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(43);
      END_STATE();
    case 12:
      if (lookahead == '_') ADVANCE(12);
      if (('0' <= lookahead && lookahead <= '9')) ADVANCE(44);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(45);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(43);
      END_STATE();
    case 13:
      if (lookahead == 'a') ADVANCE(16);
      END_STATE();
    case 14:
      if (lookahead == 'a') ADVANCE(28);
      END_STATE();
    case 15:
      if (lookahead == 'a') ADVANCE(21);
      END_STATE();
    case 16:
      if (lookahead == 'c') ADVANCE(23);
      END_STATE();
    case 17:
      if (lookahead == 'c') ADVANCE(42);
      END_STATE();
    case 18:
      if (lookahead == 'd') ADVANCE(25);
      END_STATE();
    case 19:
      if (lookahead == 'e') ADVANCE(17);
      END_STATE();
    case 20:
      if (lookahead == 'e') ADVANCE(6);
      END_STATE();
    case 21:
      if (lookahead == 'g') ADVANCE(20);
      END_STATE();
    case 22:
      if (lookahead == 'i') ADVANCE(14);
      END_STATE();
    case 23:
      if (lookahead == 'k') ADVANCE(15);
      END_STATE();
    case 24:
      if (lookahead == 'l') ADVANCE(22);
      END_STATE();
    case 25:
      if (lookahead == 'o') ADVANCE(7);
      END_STATE();
    case 26:
      if (lookahead == 'o') ADVANCE(27);
      END_STATE();
    case 27:
      if (lookahead == 'p') ADVANCE(9);
      END_STATE();
    case 28:
      if (lookahead == 's') ADVANCE(5);
      END_STATE();
    case 29:
      if (eof) ADVANCE(30);
      if (lookahead == '_') ADVANCE(10);
      if (lookahead == 'a') ADVANCE(24);
      if (lookahead == 'p') ADVANCE(13);
      if (lookahead == 't') ADVANCE(26);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(29);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(45);
      END_STATE();
    case 30:
      ACCEPT_TOKEN(ts_builtin_sym_end);
      END_STATE();
    case 31:
      ACCEPT_TOKEN(anon_sym_package);
      END_STATE();
    case 32:
      ACCEPT_TOKEN(anon_sym_LF);
      if (lookahead == '\n') ADVANCE(32);
      END_STATE();
    case 33:
      ACCEPT_TOKEN(anon_sym_DOT);
      END_STATE();
    case 34:
      ACCEPT_TOKEN(anon_sym_alias);
      END_STATE();
    case 35:
      ACCEPT_TOKEN(anon_sym_as);
      END_STATE();
    case 36:
      ACCEPT_TOKEN(anon_sym_COMMA);
      END_STATE();
    case 37:
      ACCEPT_TOKEN(anon_sym_SQUOTE);
      END_STATE();
    case 38:
      ACCEPT_TOKEN(sym_genericList);
      END_STATE();
    case 39:
      ACCEPT_TOKEN(anon_sym_LBRACK);
      END_STATE();
    case 40:
      ACCEPT_TOKEN(anon_sym_RBRACK);
      END_STATE();
    case 41:
      ACCEPT_TOKEN(anon_sym_COLON);
      END_STATE();
    case 42:
      ACCEPT_TOKEN(sym_topDec);
      END_STATE();
    case 43:
      ACCEPT_TOKEN(aux_sym__idLow_token1);
      END_STATE();
    case 44:
      ACCEPT_TOKEN(aux_sym__idLow_token2);
      END_STATE();
    case 45:
      ACCEPT_TOKEN(sym__idUp);
      END_STATE();
    case 46:
      ACCEPT_TOKEN(sym__idChar);
      END_STATE();
    case 47:
      ACCEPT_TOKEN(sym__idChar);
      if (lookahead == 'o') ADVANCE(18);
      END_STATE();
    case 48:
      ACCEPT_TOKEN(sym__idChar);
      if (lookahead == 's') ADVANCE(4);
      END_STATE();
    default:
      return false;
  }
}

static const TSLexMode ts_lex_modes[STATE_COUNT] = {
  [0] = {.lex_state = 0},
  [1] = {.lex_state = 29},
  [2] = {.lex_state = 11},
  [3] = {.lex_state = 29},
  [4] = {.lex_state = 11},
  [5] = {.lex_state = 0},
  [6] = {.lex_state = 29},
  [7] = {.lex_state = 0},
  [8] = {.lex_state = 8},
  [9] = {.lex_state = 11},
  [10] = {.lex_state = 8},
  [11] = {.lex_state = 11},
  [12] = {.lex_state = 0},
  [13] = {.lex_state = 0},
  [14] = {.lex_state = 11},
  [15] = {.lex_state = 8},
  [16] = {.lex_state = 0},
  [17] = {.lex_state = 0},
  [18] = {.lex_state = 29},
  [19] = {.lex_state = 1},
  [20] = {.lex_state = 1},
  [21] = {.lex_state = 1},
  [22] = {.lex_state = 29},
  [23] = {.lex_state = 29},
  [24] = {.lex_state = 29},
  [25] = {.lex_state = 29},
  [26] = {.lex_state = 8},
  [27] = {.lex_state = 8},
  [28] = {.lex_state = 29},
  [29] = {.lex_state = 29},
  [30] = {.lex_state = 1},
  [31] = {.lex_state = 0},
  [32] = {.lex_state = 29},
  [33] = {.lex_state = 11},
  [34] = {.lex_state = 0},
  [35] = {.lex_state = 1},
  [36] = {.lex_state = 0},
  [37] = {.lex_state = 0},
  [38] = {.lex_state = 0},
  [39] = {.lex_state = 0},
  [40] = {.lex_state = 0},
  [41] = {.lex_state = 0},
  [42] = {.lex_state = 0},
  [43] = {.lex_state = 0},
  [44] = {.lex_state = 0},
  [45] = {.lex_state = 1},
};

static const uint16_t ts_parse_table[LARGE_STATE_COUNT][SYMBOL_COUNT] = {
  [0] = {
    [ts_builtin_sym_end] = ACTIONS(1),
    [anon_sym_DOT] = ACTIONS(1),
    [anon_sym_as] = ACTIONS(1),
    [anon_sym_COMMA] = ACTIONS(1),
    [anon_sym_SQUOTE] = ACTIONS(1),
    [sym_genericList] = ACTIONS(1),
    [anon_sym_LBRACK] = ACTIONS(1),
    [anon_sym_RBRACK] = ACTIONS(1),
    [anon_sym_COLON] = ACTIONS(1),
    [sym__idChar] = ACTIONS(1),
  },
  [1] = {
    [sym_source_file] = STATE(40),
    [sym_package] = STATE(6),
    [anon_sym_package] = ACTIONS(3),
  },
};

static const uint16_t ts_small_parse_table[] = {
  [0] = 8,
    ACTIONS(7), 1,
      sym__idUp,
    STATE(11), 1,
      aux_sym_packagePath_repeat1,
    STATE(27), 1,
      sym__idLow,
    STATE(34), 1,
      sym_typeName,
    STATE(37), 1,
      sym_packagePath,
    STATE(41), 1,
      sym__pkgName,
    STATE(42), 1,
      sym_type,
    ACTIONS(5), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [26] = 5,
    ACTIONS(9), 1,
      ts_builtin_sym_end,
    ACTIONS(11), 1,
      anon_sym_alias,
    ACTIONS(13), 1,
      sym_topDec,
    STATE(22), 1,
      aux_sym_source_file_repeat2,
    STATE(18), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [43] = 5,
    STATE(9), 1,
      aux_sym_packagePath_repeat1,
    STATE(19), 1,
      sym__idLow,
    STATE(35), 1,
      sym__pkgName,
    STATE(45), 1,
      sym_packagePath,
    ACTIONS(15), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [60] = 5,
    ACTIONS(19), 1,
      anon_sym_SQUOTE,
    ACTIONS(21), 1,
      sym__idChar,
    STATE(13), 1,
      aux_sym_typeName_repeat1,
    STATE(17), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(17), 2,
      anon_sym_as,
      sym_genericList,
  [77] = 5,
    ACTIONS(11), 1,
      anon_sym_alias,
    ACTIONS(23), 1,
      ts_builtin_sym_end,
    ACTIONS(25), 1,
      sym_topDec,
    STATE(24), 1,
      aux_sym_source_file_repeat2,
    STATE(3), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [94] = 5,
    ACTIONS(29), 1,
      anon_sym_SQUOTE,
    ACTIONS(31), 1,
      sym__idChar,
    STATE(5), 1,
      aux_sym__pkgName_repeat1,
    STATE(16), 1,
      aux_sym_typeName_repeat1,
    ACTIONS(27), 2,
      anon_sym_as,
      sym_genericList,
  [111] = 5,
    ACTIONS(17), 1,
      anon_sym_COMMA,
    ACTIONS(19), 1,
      anon_sym_SQUOTE,
    ACTIONS(33), 1,
      sym__idChar,
    STATE(13), 1,
      aux_sym_typeName_repeat1,
    STATE(15), 1,
      aux_sym__pkgName_repeat1,
  [127] = 4,
    STATE(14), 1,
      aux_sym_packagePath_repeat1,
    STATE(19), 1,
      sym__idLow,
    STATE(30), 1,
      sym__pkgName,
    ACTIONS(15), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [141] = 5,
    ACTIONS(27), 1,
      anon_sym_COMMA,
    ACTIONS(29), 1,
      anon_sym_SQUOTE,
    ACTIONS(35), 1,
      sym__idChar,
    STATE(8), 1,
      aux_sym__pkgName_repeat1,
    STATE(16), 1,
      aux_sym_typeName_repeat1,
  [157] = 4,
    STATE(14), 1,
      aux_sym_packagePath_repeat1,
    STATE(27), 1,
      sym__idLow,
    STATE(43), 1,
      sym__pkgName,
    ACTIONS(5), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [171] = 3,
    ACTIONS(39), 1,
      anon_sym_SQUOTE,
    STATE(12), 1,
      aux_sym_typeName_repeat1,
    ACTIONS(37), 3,
      anon_sym_as,
      anon_sym_COMMA,
      sym_genericList,
  [183] = 3,
    ACTIONS(44), 1,
      anon_sym_SQUOTE,
    STATE(12), 1,
      aux_sym_typeName_repeat1,
    ACTIONS(42), 3,
      anon_sym_as,
      anon_sym_COMMA,
      sym_genericList,
  [195] = 4,
    STATE(14), 1,
      aux_sym_packagePath_repeat1,
    STATE(27), 1,
      sym__idLow,
    STATE(44), 1,
      sym__pkgName,
    ACTIONS(46), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [209] = 3,
    ACTIONS(51), 1,
      sym__idChar,
    STATE(15), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(49), 3,
      anon_sym_DOT,
      anon_sym_COMMA,
      anon_sym_SQUOTE,
  [221] = 3,
    ACTIONS(44), 1,
      anon_sym_SQUOTE,
    STATE(12), 1,
      aux_sym_typeName_repeat1,
    ACTIONS(17), 3,
      anon_sym_as,
      anon_sym_COMMA,
      sym_genericList,
  [233] = 3,
    ACTIONS(54), 1,
      sym__idChar,
    STATE(17), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(49), 3,
      anon_sym_as,
      anon_sym_SQUOTE,
      sym_genericList,
  [245] = 3,
    ACTIONS(59), 1,
      anon_sym_alias,
    ACTIONS(57), 2,
      ts_builtin_sym_end,
      sym_topDec,
    STATE(18), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [257] = 4,
    ACTIONS(62), 1,
      anon_sym_LF,
    ACTIONS(64), 1,
      anon_sym_DOT,
    ACTIONS(66), 1,
      sym__idChar,
    STATE(20), 1,
      aux_sym__pkgName_repeat1,
  [270] = 4,
    ACTIONS(68), 1,
      anon_sym_LF,
    ACTIONS(70), 1,
      anon_sym_DOT,
    ACTIONS(72), 1,
      sym__idChar,
    STATE(21), 1,
      aux_sym__pkgName_repeat1,
  [283] = 4,
    ACTIONS(49), 1,
      anon_sym_LF,
    ACTIONS(74), 1,
      anon_sym_DOT,
    ACTIONS(76), 1,
      sym__idChar,
    STATE(21), 1,
      aux_sym__pkgName_repeat1,
  [296] = 3,
    ACTIONS(79), 1,
      ts_builtin_sym_end,
    ACTIONS(81), 1,
      sym_topDec,
    STATE(28), 1,
      aux_sym_source_file_repeat2,
  [306] = 1,
    ACTIONS(83), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [312] = 3,
    ACTIONS(9), 1,
      ts_builtin_sym_end,
    ACTIONS(81), 1,
      sym_topDec,
    STATE(28), 1,
      aux_sym_source_file_repeat2,
  [322] = 1,
    ACTIONS(85), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [328] = 3,
    ACTIONS(33), 1,
      sym__idChar,
    ACTIONS(68), 1,
      anon_sym_DOT,
    STATE(15), 1,
      aux_sym__pkgName_repeat1,
  [338] = 3,
    ACTIONS(62), 1,
      anon_sym_DOT,
    ACTIONS(87), 1,
      sym__idChar,
    STATE(26), 1,
      aux_sym__pkgName_repeat1,
  [348] = 3,
    ACTIONS(89), 1,
      ts_builtin_sym_end,
    ACTIONS(91), 1,
      sym_topDec,
    STATE(28), 1,
      aux_sym_source_file_repeat2,
  [358] = 2,
    ACTIONS(7), 1,
      sym__idUp,
    STATE(31), 1,
      sym_typeName,
  [365] = 2,
    ACTIONS(94), 1,
      anon_sym_LF,
    ACTIONS(96), 1,
      anon_sym_DOT,
  [372] = 2,
    ACTIONS(98), 1,
      anon_sym_as,
    ACTIONS(100), 1,
      sym_genericList,
  [379] = 2,
    ACTIONS(102), 1,
      sym__idUp,
    STATE(38), 1,
      sym_typeName,
  [386] = 1,
    ACTIONS(104), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [391] = 2,
    ACTIONS(106), 1,
      anon_sym_as,
    ACTIONS(108), 1,
      sym_genericList,
  [398] = 2,
    ACTIONS(96), 1,
      anon_sym_DOT,
    ACTIONS(110), 1,
      anon_sym_LF,
  [405] = 1,
    ACTIONS(112), 1,
      anon_sym_as,
  [409] = 1,
    ACTIONS(114), 1,
      anon_sym_DOT,
  [413] = 1,
    ACTIONS(116), 1,
      anon_sym_COMMA,
  [417] = 1,
    ACTIONS(118), 1,
      anon_sym_as,
  [421] = 1,
    ACTIONS(120), 1,
      ts_builtin_sym_end,
  [425] = 1,
    ACTIONS(110), 1,
      anon_sym_DOT,
  [429] = 1,
    ACTIONS(122), 1,
      anon_sym_as,
  [433] = 1,
    ACTIONS(94), 1,
      anon_sym_DOT,
  [437] = 1,
    ACTIONS(124), 1,
      anon_sym_DOT,
  [441] = 1,
    ACTIONS(126), 1,
      anon_sym_LF,
};

static const uint32_t ts_small_parse_table_map[] = {
  [SMALL_STATE(2)] = 0,
  [SMALL_STATE(3)] = 26,
  [SMALL_STATE(4)] = 43,
  [SMALL_STATE(5)] = 60,
  [SMALL_STATE(6)] = 77,
  [SMALL_STATE(7)] = 94,
  [SMALL_STATE(8)] = 111,
  [SMALL_STATE(9)] = 127,
  [SMALL_STATE(10)] = 141,
  [SMALL_STATE(11)] = 157,
  [SMALL_STATE(12)] = 171,
  [SMALL_STATE(13)] = 183,
  [SMALL_STATE(14)] = 195,
  [SMALL_STATE(15)] = 209,
  [SMALL_STATE(16)] = 221,
  [SMALL_STATE(17)] = 233,
  [SMALL_STATE(18)] = 245,
  [SMALL_STATE(19)] = 257,
  [SMALL_STATE(20)] = 270,
  [SMALL_STATE(21)] = 283,
  [SMALL_STATE(22)] = 296,
  [SMALL_STATE(23)] = 306,
  [SMALL_STATE(24)] = 312,
  [SMALL_STATE(25)] = 322,
  [SMALL_STATE(26)] = 328,
  [SMALL_STATE(27)] = 338,
  [SMALL_STATE(28)] = 348,
  [SMALL_STATE(29)] = 358,
  [SMALL_STATE(30)] = 365,
  [SMALL_STATE(31)] = 372,
  [SMALL_STATE(32)] = 379,
  [SMALL_STATE(33)] = 386,
  [SMALL_STATE(34)] = 391,
  [SMALL_STATE(35)] = 398,
  [SMALL_STATE(36)] = 405,
  [SMALL_STATE(37)] = 409,
  [SMALL_STATE(38)] = 413,
  [SMALL_STATE(39)] = 417,
  [SMALL_STATE(40)] = 421,
  [SMALL_STATE(41)] = 425,
  [SMALL_STATE(42)] = 429,
  [SMALL_STATE(43)] = 433,
  [SMALL_STATE(44)] = 437,
  [SMALL_STATE(45)] = 441,
};

static const TSParseActionEntry ts_parse_actions[] = {
  [0] = {.entry = {.count = 0, .reusable = false}},
  [1] = {.entry = {.count = 1, .reusable = false}}, RECOVER(),
  [3] = {.entry = {.count = 1, .reusable = true}}, SHIFT(4),
  [5] = {.entry = {.count = 1, .reusable = true}}, SHIFT(27),
  [7] = {.entry = {.count = 1, .reusable = true}}, SHIFT(7),
  [9] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 2, 0, 0),
  [11] = {.entry = {.count = 1, .reusable = true}}, SHIFT(2),
  [13] = {.entry = {.count = 1, .reusable = true}}, SHIFT(22),
  [15] = {.entry = {.count = 1, .reusable = true}}, SHIFT(19),
  [17] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_typeName, 2, 0, 0),
  [19] = {.entry = {.count = 1, .reusable = true}}, SHIFT(13),
  [21] = {.entry = {.count = 1, .reusable = false}}, SHIFT(17),
  [23] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 1, 0, 0),
  [25] = {.entry = {.count = 1, .reusable = true}}, SHIFT(24),
  [27] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_typeName, 1, 0, 0),
  [29] = {.entry = {.count = 1, .reusable = true}}, SHIFT(16),
  [31] = {.entry = {.count = 1, .reusable = false}}, SHIFT(5),
  [33] = {.entry = {.count = 1, .reusable = true}}, SHIFT(15),
  [35] = {.entry = {.count = 1, .reusable = true}}, SHIFT(8),
  [37] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_typeName_repeat1, 2, 0, 0),
  [39] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_typeName_repeat1, 2, 0, 0), SHIFT_REPEAT(12),
  [42] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_typeName, 3, 0, 0),
  [44] = {.entry = {.count = 1, .reusable = true}}, SHIFT(12),
  [46] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_packagePath_repeat1, 2, 0, 0), SHIFT_REPEAT(27),
  [49] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0),
  [51] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(15),
  [54] = {.entry = {.count = 2, .reusable = false}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(17),
  [57] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0),
  [59] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0), SHIFT_REPEAT(2),
  [62] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__pkgName, 1, 0, 0),
  [64] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym__pkgName, 1, 0, 0),
  [66] = {.entry = {.count = 1, .reusable = false}}, SHIFT(20),
  [68] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__pkgName, 2, 0, 0),
  [70] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym__pkgName, 2, 0, 0),
  [72] = {.entry = {.count = 1, .reusable = false}}, SHIFT(21),
  [74] = {.entry = {.count = 1, .reusable = false}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0),
  [76] = {.entry = {.count = 2, .reusable = false}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(21),
  [79] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 3, 0, 0),
  [81] = {.entry = {.count = 1, .reusable = true}}, SHIFT(28),
  [83] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_alias, 5, 0, 6),
  [85] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_package, 3, 0, 1),
  [87] = {.entry = {.count = 1, .reusable = true}}, SHIFT(26),
  [89] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0),
  [91] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0), SHIFT_REPEAT(28),
  [94] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_packagePath, 2, 0, 0),
  [96] = {.entry = {.count = 1, .reusable = false}}, SHIFT(33),
  [98] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_type, 3, 0, 4),
  [100] = {.entry = {.count = 1, .reusable = true}}, SHIFT(39),
  [102] = {.entry = {.count = 1, .reusable = true}}, SHIFT(10),
  [104] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_packagePath_repeat1, 2, 0, 0),
  [106] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_type, 1, 0, 2),
  [108] = {.entry = {.count = 1, .reusable = true}}, SHIFT(36),
  [110] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_packagePath, 1, 0, 0),
  [112] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_type, 2, 0, 3),
  [114] = {.entry = {.count = 1, .reusable = true}}, SHIFT(29),
  [116] = {.entry = {.count = 1, .reusable = true}}, SHIFT(23),
  [118] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_type, 4, 0, 5),
  [120] = {.entry = {.count = 1, .reusable = true}},  ACCEPT_INPUT(),
  [122] = {.entry = {.count = 1, .reusable = true}}, SHIFT(32),
  [124] = {.entry = {.count = 1, .reusable = true}}, SHIFT(33),
  [126] = {.entry = {.count = 1, .reusable = true}}, SHIFT(25),
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
