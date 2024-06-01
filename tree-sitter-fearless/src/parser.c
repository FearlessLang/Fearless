#include "tree_sitter/parser.h"

#if defined(__GNUC__) || defined(__clang__)
#pragma GCC diagnostic ignored "-Wmissing-field-initializers"
#endif

#define LANGUAGE_VERSION 14
#define STATE_COUNT 35
#define LARGE_STATE_COUNT 2
#define SYMBOL_COUNT 24
#define ALIAS_COUNT 0
#define TOKEN_COUNT 11
#define EXTERNAL_TOKEN_COUNT 0
#define FIELD_COUNT 3
#define MAX_ALIAS_SEQUENCE_LENGTH 3
#define PRODUCTION_ID_COUNT 4

enum ts_symbol_identifiers {
  anon_sym_alias = 1,
  anon_sym_DOT = 2,
  sym_topDec = 3,
  anon_sym_package = 4,
  anon_sym_LF = 5,
  aux_sym__idLow_token1 = 6,
  aux_sym__idLow_token2 = 7,
  sym__idUp = 8,
  sym__idChar = 9,
  anon_sym_SQUOTE = 10,
  sym_source_file = 11,
  sym_alias = 12,
  sym_fullCN = 13,
  sym_package = 14,
  sym_packagePath = 15,
  sym__px = 16,
  sym__idLow = 17,
  sym__fIdUp = 18,
  aux_sym_source_file_repeat1 = 19,
  aux_sym_source_file_repeat2 = 20,
  aux_sym_fullCN_repeat1 = 21,
  aux_sym__px_repeat1 = 22,
  aux_sym__fIdUp_repeat1 = 23,
};

static const char * const ts_symbol_names[] = {
  [ts_builtin_sym_end] = "end",
  [anon_sym_alias] = "alias ",
  [anon_sym_DOT] = ".",
  [sym_topDec] = "topDec",
  [anon_sym_package] = "package ",
  [anon_sym_LF] = "\n",
  [aux_sym__idLow_token1] = "_idLow_token1",
  [aux_sym__idLow_token2] = "_idLow_token2",
  [sym__idUp] = "_idUp",
  [sym__idChar] = "_idChar",
  [anon_sym_SQUOTE] = "'",
  [sym_source_file] = "source_file",
  [sym_alias] = "alias",
  [sym_fullCN] = "fullCN",
  [sym_package] = "package",
  [sym_packagePath] = "packagePath",
  [sym__px] = "_px",
  [sym__idLow] = "_idLow",
  [sym__fIdUp] = "type",
  [aux_sym_source_file_repeat1] = "source_file_repeat1",
  [aux_sym_source_file_repeat2] = "source_file_repeat2",
  [aux_sym_fullCN_repeat1] = "fullCN_repeat1",
  [aux_sym__px_repeat1] = "_px_repeat1",
  [aux_sym__fIdUp_repeat1] = "_fIdUp_repeat1",
};

static const TSSymbol ts_symbol_map[] = {
  [ts_builtin_sym_end] = ts_builtin_sym_end,
  [anon_sym_alias] = anon_sym_alias,
  [anon_sym_DOT] = anon_sym_DOT,
  [sym_topDec] = sym_topDec,
  [anon_sym_package] = anon_sym_package,
  [anon_sym_LF] = anon_sym_LF,
  [aux_sym__idLow_token1] = aux_sym__idLow_token1,
  [aux_sym__idLow_token2] = aux_sym__idLow_token2,
  [sym__idUp] = sym__idUp,
  [sym__idChar] = sym__idChar,
  [anon_sym_SQUOTE] = anon_sym_SQUOTE,
  [sym_source_file] = sym_source_file,
  [sym_alias] = sym_alias,
  [sym_fullCN] = sym_fullCN,
  [sym_package] = sym_package,
  [sym_packagePath] = sym_packagePath,
  [sym__px] = sym__px,
  [sym__idLow] = sym__idLow,
  [sym__fIdUp] = sym__fIdUp,
  [aux_sym_source_file_repeat1] = aux_sym_source_file_repeat1,
  [aux_sym_source_file_repeat2] = aux_sym_source_file_repeat2,
  [aux_sym_fullCN_repeat1] = aux_sym_fullCN_repeat1,
  [aux_sym__px_repeat1] = aux_sym__px_repeat1,
  [aux_sym__fIdUp_repeat1] = aux_sym__fIdUp_repeat1,
};

static const TSSymbolMetadata ts_symbol_metadata[] = {
  [ts_builtin_sym_end] = {
    .visible = false,
    .named = true,
  },
  [anon_sym_alias] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_DOT] = {
    .visible = true,
    .named = false,
  },
  [sym_topDec] = {
    .visible = true,
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
  [anon_sym_SQUOTE] = {
    .visible = true,
    .named = false,
  },
  [sym_source_file] = {
    .visible = true,
    .named = true,
  },
  [sym_alias] = {
    .visible = true,
    .named = true,
  },
  [sym_fullCN] = {
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
  [sym__px] = {
    .visible = false,
    .named = true,
  },
  [sym__idLow] = {
    .visible = false,
    .named = true,
  },
  [sym__fIdUp] = {
    .visible = true,
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
  [aux_sym_fullCN_repeat1] = {
    .visible = false,
    .named = false,
  },
  [aux_sym__px_repeat1] = {
    .visible = false,
    .named = false,
  },
  [aux_sym__fIdUp_repeat1] = {
    .visible = false,
    .named = false,
  },
};

enum ts_field_identifiers {
  field_name = 1,
  field_package = 2,
  field_type = 3,
};

static const char * const ts_field_names[] = {
  [0] = NULL,
  [field_name] = "name",
  [field_package] = "package",
  [field_type] = "type",
};

static const TSFieldMapSlice ts_field_map_slices[PRODUCTION_ID_COUNT] = {
  [1] = {.index = 0, .length = 1},
  [2] = {.index = 1, .length = 1},
  [3] = {.index = 2, .length = 2},
};

static const TSFieldMapEntry ts_field_map_entries[] = {
  [0] =
    {field_name, 1},
  [1] =
    {field_type, 0},
  [2] =
    {field_package, 0},
    {field_type, 1},
};

static const TSSymbol ts_alias_sequences[PRODUCTION_ID_COUNT][MAX_ALIAS_SEQUENCE_LENGTH] = {
  [0] = {0},
  [3] = {
    [0] = sym_packagePath,
  },
};

static const uint16_t ts_non_terminal_alias_map[] = {
  aux_sym_fullCN_repeat1, 2,
    aux_sym_fullCN_repeat1,
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
  [8] = 8,
  [9] = 9,
  [10] = 10,
  [11] = 11,
  [12] = 12,
  [13] = 13,
  [14] = 14,
  [15] = 15,
  [16] = 16,
  [17] = 7,
  [18] = 18,
  [19] = 19,
  [20] = 20,
  [21] = 21,
  [22] = 22,
  [23] = 23,
  [24] = 24,
  [25] = 25,
  [26] = 26,
  [27] = 18,
  [28] = 16,
  [29] = 7,
  [30] = 30,
  [31] = 31,
  [32] = 32,
  [33] = 33,
  [34] = 34,
};

static bool ts_lex(TSLexer *lexer, TSStateId state) {
  START_LEXER();
  eof = lexer->eof(lexer);
  switch (state) {
    case 0:
      if (eof) ADVANCE(21);
      if (lookahead == '\'') ADVANCE(33);
      if (lookahead == '.') ADVANCE(23);
      if (lookahead == 'a') ADVANCE(31);
      if (lookahead == 't') ADVANCE(32);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(0);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('b' <= lookahead && lookahead <= 'z')) ADVANCE(30);
      END_STATE();
    case 1:
      if (lookahead == '\n') ADVANCE(26);
      if (lookahead == '.') ADVANCE(23);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(1);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(30);
      END_STATE();
    case 2:
      if (lookahead == ' ') ADVANCE(22);
      END_STATE();
    case 3:
      if (lookahead == ' ') ADVANCE(25);
      END_STATE();
    case 4:
      if (lookahead == '.') ADVANCE(23);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(4);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(30);
      END_STATE();
    case 5:
      if (lookahead == 'D') ADVANCE(13);
      END_STATE();
    case 6:
      if (lookahead == '_') ADVANCE(7);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(6);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(29);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(27);
      END_STATE();
    case 7:
      if (lookahead == '_') ADVANCE(7);
      if (('0' <= lookahead && lookahead <= '9')) ADVANCE(28);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(29);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(27);
      END_STATE();
    case 8:
      if (lookahead == 'a') ADVANCE(20);
      END_STATE();
    case 9:
      if (lookahead == 'a') ADVANCE(15);
      END_STATE();
    case 10:
      if (lookahead == 'a') ADVANCE(12);
      END_STATE();
    case 11:
      if (lookahead == 'c') ADVANCE(24);
      END_STATE();
    case 12:
      if (lookahead == 'c') ADVANCE(17);
      END_STATE();
    case 13:
      if (lookahead == 'e') ADVANCE(11);
      END_STATE();
    case 14:
      if (lookahead == 'e') ADVANCE(3);
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
      if (lookahead == 'p') ADVANCE(5);
      END_STATE();
    case 19:
      if (lookahead == 'p') ADVANCE(10);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(19);
      END_STATE();
    case 20:
      if (lookahead == 's') ADVANCE(2);
      END_STATE();
    case 21:
      ACCEPT_TOKEN(ts_builtin_sym_end);
      END_STATE();
    case 22:
      ACCEPT_TOKEN(anon_sym_alias);
      END_STATE();
    case 23:
      ACCEPT_TOKEN(anon_sym_DOT);
      END_STATE();
    case 24:
      ACCEPT_TOKEN(sym_topDec);
      END_STATE();
    case 25:
      ACCEPT_TOKEN(anon_sym_package);
      END_STATE();
    case 26:
      ACCEPT_TOKEN(anon_sym_LF);
      if (lookahead == '\n') ADVANCE(26);
      END_STATE();
    case 27:
      ACCEPT_TOKEN(aux_sym__idLow_token1);
      END_STATE();
    case 28:
      ACCEPT_TOKEN(aux_sym__idLow_token2);
      END_STATE();
    case 29:
      ACCEPT_TOKEN(sym__idUp);
      END_STATE();
    case 30:
      ACCEPT_TOKEN(sym__idChar);
      END_STATE();
    case 31:
      ACCEPT_TOKEN(sym__idChar);
      if (lookahead == 'l') ADVANCE(16);
      END_STATE();
    case 32:
      ACCEPT_TOKEN(sym__idChar);
      if (lookahead == 'o') ADVANCE(18);
      END_STATE();
    case 33:
      ACCEPT_TOKEN(anon_sym_SQUOTE);
      END_STATE();
    default:
      return false;
  }
}

static const TSLexMode ts_lex_modes[STATE_COUNT] = {
  [0] = {.lex_state = 0},
  [1] = {.lex_state = 19},
  [2] = {.lex_state = 6},
  [3] = {.lex_state = 0},
  [4] = {.lex_state = 0},
  [5] = {.lex_state = 6},
  [6] = {.lex_state = 6},
  [7] = {.lex_state = 0},
  [8] = {.lex_state = 0},
  [9] = {.lex_state = 6},
  [10] = {.lex_state = 0},
  [11] = {.lex_state = 6},
  [12] = {.lex_state = 0},
  [13] = {.lex_state = 0},
  [14] = {.lex_state = 0},
  [15] = {.lex_state = 0},
  [16] = {.lex_state = 1},
  [17] = {.lex_state = 1},
  [18] = {.lex_state = 1},
  [19] = {.lex_state = 0},
  [20] = {.lex_state = 0},
  [21] = {.lex_state = 6},
  [22] = {.lex_state = 0},
  [23] = {.lex_state = 0},
  [24] = {.lex_state = 0},
  [25] = {.lex_state = 0},
  [26] = {.lex_state = 0},
  [27] = {.lex_state = 4},
  [28] = {.lex_state = 4},
  [29] = {.lex_state = 4},
  [30] = {.lex_state = 1},
  [31] = {.lex_state = 1},
  [32] = {.lex_state = 0},
  [33] = {.lex_state = 0},
  [34] = {.lex_state = 1},
};

static const uint16_t ts_parse_table[LARGE_STATE_COUNT][SYMBOL_COUNT] = {
  [0] = {
    [ts_builtin_sym_end] = ACTIONS(1),
    [anon_sym_alias] = ACTIONS(1),
    [anon_sym_DOT] = ACTIONS(1),
    [sym_topDec] = ACTIONS(1),
    [sym__idChar] = ACTIONS(1),
    [anon_sym_SQUOTE] = ACTIONS(1),
  },
  [1] = {
    [sym_source_file] = STATE(33),
    [sym_package] = STATE(8),
    [anon_sym_package] = ACTIONS(3),
  },
};

static const uint16_t ts_small_parse_table[] = {
  [0] = 7,
    ACTIONS(7), 1,
      sym__idUp,
    STATE(5), 1,
      aux_sym_fullCN_repeat1,
    STATE(19), 1,
      sym_fullCN,
    STATE(20), 1,
      sym__fIdUp,
    STATE(27), 1,
      sym__idLow,
    STATE(32), 1,
      sym__px,
    ACTIONS(5), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [23] = 5,
    ACTIONS(11), 1,
      sym__idChar,
    ACTIONS(13), 1,
      anon_sym_SQUOTE,
    STATE(4), 1,
      aux_sym__px_repeat1,
    STATE(12), 1,
      aux_sym__fIdUp_repeat1,
    ACTIONS(9), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [41] = 5,
    ACTIONS(17), 1,
      sym__idChar,
    ACTIONS(19), 1,
      anon_sym_SQUOTE,
    STATE(7), 1,
      aux_sym__px_repeat1,
    STATE(14), 1,
      aux_sym__fIdUp_repeat1,
    ACTIONS(15), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [59] = 6,
    ACTIONS(7), 1,
      sym__idUp,
    STATE(9), 1,
      aux_sym_fullCN_repeat1,
    STATE(24), 1,
      sym__fIdUp,
    STATE(27), 1,
      sym__idLow,
    STATE(32), 1,
      sym__px,
    ACTIONS(5), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [79] = 5,
    STATE(11), 1,
      aux_sym_fullCN_repeat1,
    STATE(18), 1,
      sym__idLow,
    STATE(31), 1,
      sym__px,
    STATE(34), 1,
      sym_packagePath,
    ACTIONS(21), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [96] = 3,
    ACTIONS(25), 1,
      sym__idChar,
    STATE(7), 1,
      aux_sym__px_repeat1,
    ACTIONS(23), 4,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
      anon_sym_SQUOTE,
  [109] = 5,
    ACTIONS(28), 1,
      ts_builtin_sym_end,
    ACTIONS(30), 1,
      anon_sym_alias,
    ACTIONS(32), 1,
      sym_topDec,
    STATE(26), 1,
      aux_sym_source_file_repeat2,
    STATE(10), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [126] = 5,
    ACTIONS(37), 1,
      sym__idUp,
    STATE(9), 1,
      aux_sym_fullCN_repeat1,
    STATE(27), 1,
      sym__idLow,
    STATE(32), 1,
      sym__px,
    ACTIONS(34), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [143] = 5,
    ACTIONS(30), 1,
      anon_sym_alias,
    ACTIONS(39), 1,
      ts_builtin_sym_end,
    ACTIONS(41), 1,
      sym_topDec,
    STATE(22), 1,
      aux_sym_source_file_repeat2,
    STATE(15), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [160] = 4,
    STATE(9), 1,
      aux_sym_fullCN_repeat1,
    STATE(18), 1,
      sym__idLow,
    STATE(30), 1,
      sym__px,
    ACTIONS(21), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [174] = 3,
    ACTIONS(43), 1,
      anon_sym_SQUOTE,
    STATE(13), 1,
      aux_sym__fIdUp_repeat1,
    ACTIONS(15), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [186] = 3,
    ACTIONS(47), 1,
      anon_sym_SQUOTE,
    STATE(13), 1,
      aux_sym__fIdUp_repeat1,
    ACTIONS(45), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [198] = 3,
    ACTIONS(43), 1,
      anon_sym_SQUOTE,
    STATE(13), 1,
      aux_sym__fIdUp_repeat1,
    ACTIONS(50), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [210] = 3,
    ACTIONS(54), 1,
      anon_sym_alias,
    ACTIONS(52), 2,
      ts_builtin_sym_end,
      sym_topDec,
    STATE(15), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [222] = 4,
    ACTIONS(57), 1,
      anon_sym_DOT,
    ACTIONS(59), 1,
      anon_sym_LF,
    ACTIONS(61), 1,
      sym__idChar,
    STATE(17), 1,
      aux_sym__px_repeat1,
  [235] = 4,
    ACTIONS(23), 1,
      anon_sym_LF,
    ACTIONS(63), 1,
      anon_sym_DOT,
    ACTIONS(65), 1,
      sym__idChar,
    STATE(17), 1,
      aux_sym__px_repeat1,
  [248] = 4,
    ACTIONS(68), 1,
      anon_sym_DOT,
    ACTIONS(70), 1,
      anon_sym_LF,
    ACTIONS(72), 1,
      sym__idChar,
    STATE(16), 1,
      aux_sym__px_repeat1,
  [261] = 1,
    ACTIONS(74), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [267] = 1,
    ACTIONS(76), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [273] = 1,
    ACTIONS(37), 3,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
      sym__idUp,
  [279] = 3,
    ACTIONS(78), 1,
      ts_builtin_sym_end,
    ACTIONS(80), 1,
      sym_topDec,
    STATE(23), 1,
      aux_sym_source_file_repeat2,
  [289] = 3,
    ACTIONS(82), 1,
      ts_builtin_sym_end,
    ACTIONS(84), 1,
      sym_topDec,
    STATE(23), 1,
      aux_sym_source_file_repeat2,
  [299] = 1,
    ACTIONS(87), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [305] = 1,
    ACTIONS(89), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [311] = 3,
    ACTIONS(39), 1,
      ts_builtin_sym_end,
    ACTIONS(80), 1,
      sym_topDec,
    STATE(23), 1,
      aux_sym_source_file_repeat2,
  [321] = 3,
    ACTIONS(70), 1,
      anon_sym_DOT,
    ACTIONS(91), 1,
      sym__idChar,
    STATE(28), 1,
      aux_sym__px_repeat1,
  [331] = 3,
    ACTIONS(59), 1,
      anon_sym_DOT,
    ACTIONS(93), 1,
      sym__idChar,
    STATE(29), 1,
      aux_sym__px_repeat1,
  [341] = 3,
    ACTIONS(23), 1,
      anon_sym_DOT,
    ACTIONS(95), 1,
      sym__idChar,
    STATE(29), 1,
      aux_sym__px_repeat1,
  [351] = 2,
    ACTIONS(98), 1,
      anon_sym_DOT,
    ACTIONS(100), 1,
      anon_sym_LF,
  [358] = 2,
    ACTIONS(98), 1,
      anon_sym_DOT,
    ACTIONS(102), 1,
      anon_sym_LF,
  [365] = 1,
    ACTIONS(104), 1,
      anon_sym_DOT,
  [369] = 1,
    ACTIONS(106), 1,
      ts_builtin_sym_end,
  [373] = 1,
    ACTIONS(108), 1,
      anon_sym_LF,
};

static const uint32_t ts_small_parse_table_map[] = {
  [SMALL_STATE(2)] = 0,
  [SMALL_STATE(3)] = 23,
  [SMALL_STATE(4)] = 41,
  [SMALL_STATE(5)] = 59,
  [SMALL_STATE(6)] = 79,
  [SMALL_STATE(7)] = 96,
  [SMALL_STATE(8)] = 109,
  [SMALL_STATE(9)] = 126,
  [SMALL_STATE(10)] = 143,
  [SMALL_STATE(11)] = 160,
  [SMALL_STATE(12)] = 174,
  [SMALL_STATE(13)] = 186,
  [SMALL_STATE(14)] = 198,
  [SMALL_STATE(15)] = 210,
  [SMALL_STATE(16)] = 222,
  [SMALL_STATE(17)] = 235,
  [SMALL_STATE(18)] = 248,
  [SMALL_STATE(19)] = 261,
  [SMALL_STATE(20)] = 267,
  [SMALL_STATE(21)] = 273,
  [SMALL_STATE(22)] = 279,
  [SMALL_STATE(23)] = 289,
  [SMALL_STATE(24)] = 299,
  [SMALL_STATE(25)] = 305,
  [SMALL_STATE(26)] = 311,
  [SMALL_STATE(27)] = 321,
  [SMALL_STATE(28)] = 331,
  [SMALL_STATE(29)] = 341,
  [SMALL_STATE(30)] = 351,
  [SMALL_STATE(31)] = 358,
  [SMALL_STATE(32)] = 365,
  [SMALL_STATE(33)] = 369,
  [SMALL_STATE(34)] = 373,
};

static const TSParseActionEntry ts_parse_actions[] = {
  [0] = {.entry = {.count = 0, .reusable = false}},
  [1] = {.entry = {.count = 1, .reusable = false}}, RECOVER(),
  [3] = {.entry = {.count = 1, .reusable = true}}, SHIFT(6),
  [5] = {.entry = {.count = 1, .reusable = true}}, SHIFT(27),
  [7] = {.entry = {.count = 1, .reusable = true}}, SHIFT(3),
  [9] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fIdUp, 1, 0, 0),
  [11] = {.entry = {.count = 1, .reusable = false}}, SHIFT(4),
  [13] = {.entry = {.count = 1, .reusable = true}}, SHIFT(12),
  [15] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fIdUp, 2, 0, 0),
  [17] = {.entry = {.count = 1, .reusable = false}}, SHIFT(7),
  [19] = {.entry = {.count = 1, .reusable = true}}, SHIFT(14),
  [21] = {.entry = {.count = 1, .reusable = true}}, SHIFT(18),
  [23] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__px_repeat1, 2, 0, 0),
  [25] = {.entry = {.count = 2, .reusable = false}}, REDUCE(aux_sym__px_repeat1, 2, 0, 0), SHIFT_REPEAT(7),
  [28] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 1, 0, 0),
  [30] = {.entry = {.count = 1, .reusable = true}}, SHIFT(2),
  [32] = {.entry = {.count = 1, .reusable = true}}, SHIFT(26),
  [34] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_fullCN_repeat1, 2, 0, 0), SHIFT_REPEAT(27),
  [37] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_fullCN_repeat1, 2, 0, 0),
  [39] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 2, 0, 0),
  [41] = {.entry = {.count = 1, .reusable = true}}, SHIFT(22),
  [43] = {.entry = {.count = 1, .reusable = true}}, SHIFT(13),
  [45] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__fIdUp_repeat1, 2, 0, 0),
  [47] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__fIdUp_repeat1, 2, 0, 0), SHIFT_REPEAT(13),
  [50] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fIdUp, 3, 0, 0),
  [52] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0),
  [54] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0), SHIFT_REPEAT(2),
  [57] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym__px, 2, 0, 0),
  [59] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__px, 2, 0, 0),
  [61] = {.entry = {.count = 1, .reusable = false}}, SHIFT(17),
  [63] = {.entry = {.count = 1, .reusable = false}}, REDUCE(aux_sym__px_repeat1, 2, 0, 0),
  [65] = {.entry = {.count = 2, .reusable = false}}, REDUCE(aux_sym__px_repeat1, 2, 0, 0), SHIFT_REPEAT(17),
  [68] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym__px, 1, 0, 0),
  [70] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__px, 1, 0, 0),
  [72] = {.entry = {.count = 1, .reusable = false}}, SHIFT(16),
  [74] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_alias, 2, 0, 0),
  [76] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_fullCN, 1, 0, 2),
  [78] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 3, 0, 0),
  [80] = {.entry = {.count = 1, .reusable = true}}, SHIFT(23),
  [82] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0),
  [84] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0), SHIFT_REPEAT(23),
  [87] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_fullCN, 2, 0, 3),
  [89] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_package, 3, 0, 1),
  [91] = {.entry = {.count = 1, .reusable = true}}, SHIFT(28),
  [93] = {.entry = {.count = 1, .reusable = true}}, SHIFT(29),
  [95] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__px_repeat1, 2, 0, 0), SHIFT_REPEAT(29),
  [98] = {.entry = {.count = 1, .reusable = false}}, SHIFT(21),
  [100] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_packagePath, 2, 0, 0),
  [102] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_packagePath, 1, 0, 0),
  [104] = {.entry = {.count = 1, .reusable = true}}, SHIFT(21),
  [106] = {.entry = {.count = 1, .reusable = true}},  ACCEPT_INPUT(),
  [108] = {.entry = {.count = 1, .reusable = true}}, SHIFT(25),
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
