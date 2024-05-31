#include "tree_sitter/parser.h"

#if defined(__GNUC__) || defined(__clang__)
#pragma GCC diagnostic ignored "-Wmissing-field-initializers"
#endif

#define LANGUAGE_VERSION 14
#define STATE_COUNT 24
#define LARGE_STATE_COUNT 2
#define SYMBOL_COUNT 18
#define ALIAS_COUNT 0
#define TOKEN_COUNT 9
#define EXTERNAL_TOKEN_COUNT 0
#define FIELD_COUNT 0
#define MAX_ALIAS_SEQUENCE_LENGTH 3
#define PRODUCTION_ID_COUNT 1

enum ts_symbol_identifiers {
  sym_alias = 1,
  sym_topDec = 2,
  anon_sym_package = 3,
  anon_sym_LF = 4,
  anon_sym_DOT = 5,
  aux_sym__idLow_token1 = 6,
  aux_sym__idLow_token2 = 7,
  sym__idChar = 8,
  sym_source_file = 9,
  sym_package = 10,
  sym_packageName = 11,
  sym__px = 12,
  sym__idLow = 13,
  aux_sym_source_file_repeat1 = 14,
  aux_sym_source_file_repeat2 = 15,
  aux_sym_packageName_repeat1 = 16,
  aux_sym__px_repeat1 = 17,
};

static const char * const ts_symbol_names[] = {
  [ts_builtin_sym_end] = "end",
  [sym_alias] = "alias",
  [sym_topDec] = "topDec",
  [anon_sym_package] = "package ",
  [anon_sym_LF] = "\n",
  [anon_sym_DOT] = ".",
  [aux_sym__idLow_token1] = "_idLow_token1",
  [aux_sym__idLow_token2] = "_idLow_token2",
  [sym__idChar] = "_idChar",
  [sym_source_file] = "source_file",
  [sym_package] = "package",
  [sym_packageName] = "packageName",
  [sym__px] = "_px",
  [sym__idLow] = "_idLow",
  [aux_sym_source_file_repeat1] = "source_file_repeat1",
  [aux_sym_source_file_repeat2] = "source_file_repeat2",
  [aux_sym_packageName_repeat1] = "packageName_repeat1",
  [aux_sym__px_repeat1] = "_px_repeat1",
};

static const TSSymbol ts_symbol_map[] = {
  [ts_builtin_sym_end] = ts_builtin_sym_end,
  [sym_alias] = sym_alias,
  [sym_topDec] = sym_topDec,
  [anon_sym_package] = anon_sym_package,
  [anon_sym_LF] = anon_sym_LF,
  [anon_sym_DOT] = anon_sym_DOT,
  [aux_sym__idLow_token1] = aux_sym__idLow_token1,
  [aux_sym__idLow_token2] = aux_sym__idLow_token2,
  [sym__idChar] = sym__idChar,
  [sym_source_file] = sym_source_file,
  [sym_package] = sym_package,
  [sym_packageName] = sym_packageName,
  [sym__px] = sym__px,
  [sym__idLow] = sym__idLow,
  [aux_sym_source_file_repeat1] = aux_sym_source_file_repeat1,
  [aux_sym_source_file_repeat2] = aux_sym_source_file_repeat2,
  [aux_sym_packageName_repeat1] = aux_sym_packageName_repeat1,
  [aux_sym__px_repeat1] = aux_sym__px_repeat1,
};

static const TSSymbolMetadata ts_symbol_metadata[] = {
  [ts_builtin_sym_end] = {
    .visible = false,
    .named = true,
  },
  [sym_alias] = {
    .visible = true,
    .named = true,
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
  [anon_sym_DOT] = {
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
  [sym_packageName] = {
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
  [aux_sym_source_file_repeat1] = {
    .visible = false,
    .named = false,
  },
  [aux_sym_source_file_repeat2] = {
    .visible = false,
    .named = false,
  },
  [aux_sym_packageName_repeat1] = {
    .visible = false,
    .named = false,
  },
  [aux_sym__px_repeat1] = {
    .visible = false,
    .named = false,
  },
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
  [11] = 7,
  [12] = 9,
  [13] = 13,
  [14] = 14,
  [15] = 8,
  [16] = 16,
  [17] = 17,
  [18] = 18,
  [19] = 19,
  [20] = 20,
  [21] = 21,
  [22] = 22,
  [23] = 23,
};

static bool ts_lex(TSLexer *lexer, TSStateId state) {
  START_LEXER();
  eof = lexer->eof(lexer);
  switch (state) {
    case 0:
      if (eof) ADVANCE(21);
      if (lookahead == '\n') ADVANCE(25);
      if (lookahead == '.') ADVANCE(26);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(0);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(29);
      END_STATE();
    case 1:
      if (lookahead == ' ') ADVANCE(24);
      END_STATE();
    case 2:
      if (lookahead == '.') ADVANCE(26);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(2);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(29);
      END_STATE();
    case 3:
      if (lookahead == 'D') ADVANCE(12);
      END_STATE();
    case 4:
      if (lookahead == '_') ADVANCE(5);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(4);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(27);
      END_STATE();
    case 5:
      if (lookahead == '_') ADVANCE(5);
      if (('0' <= lookahead && lookahead <= '9')) ADVANCE(28);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(27);
      END_STATE();
    case 6:
      if (lookahead == 'a') ADVANCE(9);
      END_STATE();
    case 7:
      if (lookahead == 'a') ADVANCE(19);
      END_STATE();
    case 8:
      if (lookahead == 'a') ADVANCE(13);
      END_STATE();
    case 9:
      if (lookahead == 'c') ADVANCE(15);
      END_STATE();
    case 10:
      if (lookahead == 'c') ADVANCE(23);
      END_STATE();
    case 11:
      if (lookahead == 'e') ADVANCE(1);
      END_STATE();
    case 12:
      if (lookahead == 'e') ADVANCE(10);
      END_STATE();
    case 13:
      if (lookahead == 'g') ADVANCE(11);
      END_STATE();
    case 14:
      if (lookahead == 'i') ADVANCE(7);
      END_STATE();
    case 15:
      if (lookahead == 'k') ADVANCE(8);
      END_STATE();
    case 16:
      if (lookahead == 'l') ADVANCE(14);
      END_STATE();
    case 17:
      if (lookahead == 'o') ADVANCE(18);
      END_STATE();
    case 18:
      if (lookahead == 'p') ADVANCE(3);
      END_STATE();
    case 19:
      if (lookahead == 's') ADVANCE(22);
      END_STATE();
    case 20:
      if (eof) ADVANCE(21);
      if (lookahead == '.') ADVANCE(26);
      if (lookahead == 'a') ADVANCE(16);
      if (lookahead == 'p') ADVANCE(6);
      if (lookahead == 't') ADVANCE(17);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(20);
      END_STATE();
    case 21:
      ACCEPT_TOKEN(ts_builtin_sym_end);
      END_STATE();
    case 22:
      ACCEPT_TOKEN(sym_alias);
      END_STATE();
    case 23:
      ACCEPT_TOKEN(sym_topDec);
      END_STATE();
    case 24:
      ACCEPT_TOKEN(anon_sym_package);
      END_STATE();
    case 25:
      ACCEPT_TOKEN(anon_sym_LF);
      if (lookahead == '\n') ADVANCE(25);
      END_STATE();
    case 26:
      ACCEPT_TOKEN(anon_sym_DOT);
      END_STATE();
    case 27:
      ACCEPT_TOKEN(aux_sym__idLow_token1);
      END_STATE();
    case 28:
      ACCEPT_TOKEN(aux_sym__idLow_token2);
      END_STATE();
    case 29:
      ACCEPT_TOKEN(sym__idChar);
      END_STATE();
    default:
      return false;
  }
}

static const TSLexMode ts_lex_modes[STATE_COUNT] = {
  [0] = {.lex_state = 0},
  [1] = {.lex_state = 20},
  [2] = {.lex_state = 4},
  [3] = {.lex_state = 20},
  [4] = {.lex_state = 4},
  [5] = {.lex_state = 20},
  [6] = {.lex_state = 4},
  [7] = {.lex_state = 0},
  [8] = {.lex_state = 0},
  [9] = {.lex_state = 0},
  [10] = {.lex_state = 20},
  [11] = {.lex_state = 2},
  [12] = {.lex_state = 2},
  [13] = {.lex_state = 20},
  [14] = {.lex_state = 20},
  [15] = {.lex_state = 2},
  [16] = {.lex_state = 20},
  [17] = {.lex_state = 20},
  [18] = {.lex_state = 0},
  [19] = {.lex_state = 0},
  [20] = {.lex_state = 4},
  [21] = {.lex_state = 20},
  [22] = {.lex_state = 0},
  [23] = {.lex_state = 0},
};

static const uint16_t ts_parse_table[LARGE_STATE_COUNT][SYMBOL_COUNT] = {
  [0] = {
    [ts_builtin_sym_end] = ACTIONS(1),
    [anon_sym_LF] = ACTIONS(1),
    [anon_sym_DOT] = ACTIONS(1),
    [sym__idChar] = ACTIONS(1),
  },
  [1] = {
    [sym_source_file] = STATE(22),
    [sym_package] = STATE(3),
    [anon_sym_package] = ACTIONS(3),
  },
};

static const uint16_t ts_small_parse_table[] = {
  [0] = 5,
    STATE(4), 1,
      aux_sym_packageName_repeat1,
    STATE(8), 1,
      sym__idLow,
    STATE(18), 1,
      sym__px,
    STATE(23), 1,
      sym_packageName,
    ACTIONS(5), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [17] = 5,
    ACTIONS(7), 1,
      ts_builtin_sym_end,
    ACTIONS(9), 1,
      sym_alias,
    ACTIONS(11), 1,
      sym_topDec,
    STATE(5), 1,
      aux_sym_source_file_repeat1,
    STATE(13), 1,
      aux_sym_source_file_repeat2,
  [33] = 4,
    STATE(6), 1,
      aux_sym_packageName_repeat1,
    STATE(8), 1,
      sym__idLow,
    STATE(19), 1,
      sym__px,
    ACTIONS(5), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [47] = 5,
    ACTIONS(13), 1,
      ts_builtin_sym_end,
    ACTIONS(15), 1,
      sym_alias,
    ACTIONS(17), 1,
      sym_topDec,
    STATE(10), 1,
      aux_sym_source_file_repeat1,
    STATE(16), 1,
      aux_sym_source_file_repeat2,
  [63] = 4,
    STATE(6), 1,
      aux_sym_packageName_repeat1,
    STATE(15), 1,
      sym__idLow,
    STATE(21), 1,
      sym__px,
    ACTIONS(19), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [77] = 4,
    ACTIONS(22), 1,
      anon_sym_LF,
    ACTIONS(24), 1,
      anon_sym_DOT,
    ACTIONS(26), 1,
      sym__idChar,
    STATE(7), 1,
      aux_sym__px_repeat1,
  [90] = 4,
    ACTIONS(29), 1,
      anon_sym_LF,
    ACTIONS(31), 1,
      anon_sym_DOT,
    ACTIONS(33), 1,
      sym__idChar,
    STATE(9), 1,
      aux_sym__px_repeat1,
  [103] = 4,
    ACTIONS(35), 1,
      anon_sym_LF,
    ACTIONS(37), 1,
      anon_sym_DOT,
    ACTIONS(39), 1,
      sym__idChar,
    STATE(7), 1,
      aux_sym__px_repeat1,
  [116] = 3,
    ACTIONS(43), 1,
      sym_alias,
    STATE(10), 1,
      aux_sym_source_file_repeat1,
    ACTIONS(41), 2,
      ts_builtin_sym_end,
      sym_topDec,
  [127] = 3,
    ACTIONS(22), 1,
      anon_sym_DOT,
    ACTIONS(46), 1,
      sym__idChar,
    STATE(11), 1,
      aux_sym__px_repeat1,
  [137] = 3,
    ACTIONS(35), 1,
      anon_sym_DOT,
    ACTIONS(49), 1,
      sym__idChar,
    STATE(11), 1,
      aux_sym__px_repeat1,
  [147] = 3,
    ACTIONS(13), 1,
      ts_builtin_sym_end,
    ACTIONS(51), 1,
      sym_topDec,
    STATE(17), 1,
      aux_sym_source_file_repeat2,
  [157] = 1,
    ACTIONS(53), 3,
      ts_builtin_sym_end,
      sym_alias,
      sym_topDec,
  [163] = 3,
    ACTIONS(29), 1,
      anon_sym_DOT,
    ACTIONS(55), 1,
      sym__idChar,
    STATE(12), 1,
      aux_sym__px_repeat1,
  [173] = 3,
    ACTIONS(51), 1,
      sym_topDec,
    ACTIONS(57), 1,
      ts_builtin_sym_end,
    STATE(17), 1,
      aux_sym_source_file_repeat2,
  [183] = 3,
    ACTIONS(59), 1,
      ts_builtin_sym_end,
    ACTIONS(61), 1,
      sym_topDec,
    STATE(17), 1,
      aux_sym_source_file_repeat2,
  [193] = 2,
    ACTIONS(64), 1,
      anon_sym_LF,
    ACTIONS(66), 1,
      anon_sym_DOT,
  [200] = 2,
    ACTIONS(66), 1,
      anon_sym_DOT,
    ACTIONS(68), 1,
      anon_sym_LF,
  [207] = 1,
    ACTIONS(70), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [212] = 1,
    ACTIONS(72), 1,
      anon_sym_DOT,
  [216] = 1,
    ACTIONS(74), 1,
      ts_builtin_sym_end,
  [220] = 1,
    ACTIONS(76), 1,
      anon_sym_LF,
};

static const uint32_t ts_small_parse_table_map[] = {
  [SMALL_STATE(2)] = 0,
  [SMALL_STATE(3)] = 17,
  [SMALL_STATE(4)] = 33,
  [SMALL_STATE(5)] = 47,
  [SMALL_STATE(6)] = 63,
  [SMALL_STATE(7)] = 77,
  [SMALL_STATE(8)] = 90,
  [SMALL_STATE(9)] = 103,
  [SMALL_STATE(10)] = 116,
  [SMALL_STATE(11)] = 127,
  [SMALL_STATE(12)] = 137,
  [SMALL_STATE(13)] = 147,
  [SMALL_STATE(14)] = 157,
  [SMALL_STATE(15)] = 163,
  [SMALL_STATE(16)] = 173,
  [SMALL_STATE(17)] = 183,
  [SMALL_STATE(18)] = 193,
  [SMALL_STATE(19)] = 200,
  [SMALL_STATE(20)] = 207,
  [SMALL_STATE(21)] = 212,
  [SMALL_STATE(22)] = 216,
  [SMALL_STATE(23)] = 220,
};

static const TSParseActionEntry ts_parse_actions[] = {
  [0] = {.entry = {.count = 0, .reusable = false}},
  [1] = {.entry = {.count = 1, .reusable = false}}, RECOVER(),
  [3] = {.entry = {.count = 1, .reusable = true}}, SHIFT(2),
  [5] = {.entry = {.count = 1, .reusable = true}}, SHIFT(8),
  [7] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 1, 0, 0),
  [9] = {.entry = {.count = 1, .reusable = true}}, SHIFT(5),
  [11] = {.entry = {.count = 1, .reusable = true}}, SHIFT(13),
  [13] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 2, 0, 0),
  [15] = {.entry = {.count = 1, .reusable = true}}, SHIFT(10),
  [17] = {.entry = {.count = 1, .reusable = true}}, SHIFT(16),
  [19] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_packageName_repeat1, 2, 0, 0), SHIFT_REPEAT(15),
  [22] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__px_repeat1, 2, 0, 0),
  [24] = {.entry = {.count = 1, .reusable = false}}, REDUCE(aux_sym__px_repeat1, 2, 0, 0),
  [26] = {.entry = {.count = 2, .reusable = false}}, REDUCE(aux_sym__px_repeat1, 2, 0, 0), SHIFT_REPEAT(7),
  [29] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__px, 1, 0, 0),
  [31] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym__px, 1, 0, 0),
  [33] = {.entry = {.count = 1, .reusable = false}}, SHIFT(9),
  [35] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__px, 2, 0, 0),
  [37] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym__px, 2, 0, 0),
  [39] = {.entry = {.count = 1, .reusable = false}}, SHIFT(7),
  [41] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0),
  [43] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0), SHIFT_REPEAT(10),
  [46] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__px_repeat1, 2, 0, 0), SHIFT_REPEAT(11),
  [49] = {.entry = {.count = 1, .reusable = true}}, SHIFT(11),
  [51] = {.entry = {.count = 1, .reusable = true}}, SHIFT(17),
  [53] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_package, 3, 0, 0),
  [55] = {.entry = {.count = 1, .reusable = true}}, SHIFT(12),
  [57] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 3, 0, 0),
  [59] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0),
  [61] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0), SHIFT_REPEAT(17),
  [64] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_packageName, 1, 0, 0),
  [66] = {.entry = {.count = 1, .reusable = false}}, SHIFT(20),
  [68] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_packageName, 2, 0, 0),
  [70] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_packageName_repeat1, 2, 0, 0),
  [72] = {.entry = {.count = 1, .reusable = true}}, SHIFT(20),
  [74] = {.entry = {.count = 1, .reusable = true}},  ACCEPT_INPUT(),
  [76] = {.entry = {.count = 1, .reusable = true}}, SHIFT(14),
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
