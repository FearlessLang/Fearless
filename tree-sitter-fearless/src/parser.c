#include "tree_sitter/parser.h"

#if defined(__GNUC__) || defined(__clang__)
#pragma GCC diagnostic ignored "-Wmissing-field-initializers"
#endif

#define LANGUAGE_VERSION 14
#define STATE_COUNT 54
#define LARGE_STATE_COUNT 2
#define SYMBOL_COUNT 32
#define ALIAS_COUNT 0
#define TOKEN_COUNT 20
#define EXTERNAL_TOKEN_COUNT 0
#define FIELD_COUNT 5
#define MAX_ALIAS_SEQUENCE_LENGTH 8
#define PRODUCTION_ID_COUNT 7

enum ts_symbol_identifiers {
  anon_sym_package = 1,
  anon_sym_SPACE = 2,
  anon_sym_DOT = 3,
  sym__pkgName = 4,
  anon_sym_alias = 5,
  anon_sym_as = 6,
  anon_sym_COMMA = 7,
  sym_typeName = 8,
  anon_sym_LBRACK = 9,
  anon_sym_RBRACK = 10,
  anon_sym_mut = 11,
  anon_sym_readH = 12,
  anon_sym_mutH = 13,
  anon_sym_read_SLASHimm = 14,
  anon_sym_read = 15,
  anon_sym_iso = 16,
  anon_sym_recMdf = 17,
  anon_sym_imm = 18,
  sym_topDec = 19,
  sym_source_file = 20,
  sym_package = 21,
  sym__fullPkgName = 22,
  sym_packagePath = 23,
  sym_alias = 24,
  sym_concreteType = 25,
  sym_concreteTypes = 26,
  aux_sym_source_file_repeat1 = 27,
  aux_sym_source_file_repeat2 = 28,
  aux_sym__fullPkgName_repeat1 = 29,
  aux_sym_packagePath_repeat1 = 30,
  aux_sym_concreteTypes_repeat1 = 31,
};

static const char * const ts_symbol_names[] = {
  [ts_builtin_sym_end] = "end",
  [anon_sym_package] = "package",
  [anon_sym_SPACE] = " ",
  [anon_sym_DOT] = ".",
  [sym__pkgName] = "_pkgName",
  [anon_sym_alias] = "alias",
  [anon_sym_as] = "as",
  [anon_sym_COMMA] = ",",
  [sym_typeName] = "typeName",
  [anon_sym_LBRACK] = "[",
  [anon_sym_RBRACK] = "]",
  [anon_sym_mut] = "mut",
  [anon_sym_readH] = "readH",
  [anon_sym_mutH] = "mutH",
  [anon_sym_read_SLASHimm] = "read/imm",
  [anon_sym_read] = "read",
  [anon_sym_iso] = "iso",
  [anon_sym_recMdf] = "recMdf",
  [anon_sym_imm] = "imm",
  [sym_topDec] = "topDec",
  [sym_source_file] = "source_file",
  [sym_package] = "package",
  [sym__fullPkgName] = "packagePath",
  [sym_packagePath] = "packagePath",
  [sym_alias] = "alias",
  [sym_concreteType] = "concreteType",
  [sym_concreteTypes] = "concreteTypes",
  [aux_sym_source_file_repeat1] = "source_file_repeat1",
  [aux_sym_source_file_repeat2] = "source_file_repeat2",
  [aux_sym__fullPkgName_repeat1] = "_fullPkgName_repeat1",
  [aux_sym_packagePath_repeat1] = "packagePath_repeat1",
  [aux_sym_concreteTypes_repeat1] = "concreteTypes_repeat1",
};

static const TSSymbol ts_symbol_map[] = {
  [ts_builtin_sym_end] = ts_builtin_sym_end,
  [anon_sym_package] = anon_sym_package,
  [anon_sym_SPACE] = anon_sym_SPACE,
  [anon_sym_DOT] = anon_sym_DOT,
  [sym__pkgName] = sym__pkgName,
  [anon_sym_alias] = anon_sym_alias,
  [anon_sym_as] = anon_sym_as,
  [anon_sym_COMMA] = anon_sym_COMMA,
  [sym_typeName] = sym_typeName,
  [anon_sym_LBRACK] = anon_sym_LBRACK,
  [anon_sym_RBRACK] = anon_sym_RBRACK,
  [anon_sym_mut] = anon_sym_mut,
  [anon_sym_readH] = anon_sym_readH,
  [anon_sym_mutH] = anon_sym_mutH,
  [anon_sym_read_SLASHimm] = anon_sym_read_SLASHimm,
  [anon_sym_read] = anon_sym_read,
  [anon_sym_iso] = anon_sym_iso,
  [anon_sym_recMdf] = anon_sym_recMdf,
  [anon_sym_imm] = anon_sym_imm,
  [sym_topDec] = sym_topDec,
  [sym_source_file] = sym_source_file,
  [sym_package] = sym_package,
  [sym__fullPkgName] = sym_packagePath,
  [sym_packagePath] = sym_packagePath,
  [sym_alias] = sym_alias,
  [sym_concreteType] = sym_concreteType,
  [sym_concreteTypes] = sym_concreteTypes,
  [aux_sym_source_file_repeat1] = aux_sym_source_file_repeat1,
  [aux_sym_source_file_repeat2] = aux_sym_source_file_repeat2,
  [aux_sym__fullPkgName_repeat1] = aux_sym__fullPkgName_repeat1,
  [aux_sym_packagePath_repeat1] = aux_sym_packagePath_repeat1,
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
  [sym__pkgName] = {
    .visible = false,
    .named = true,
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
  [sym_typeName] = {
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
  [anon_sym_mut] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_readH] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_mutH] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_read_SLASHimm] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_read] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_iso] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_recMdf] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_imm] = {
    .visible = true,
    .named = false,
  },
  [sym_topDec] = {
    .visible = true,
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
  [sym_alias] = {
    .visible = true,
    .named = true,
  },
  [sym_concreteType] = {
    .visible = true,
    .named = true,
  },
  [sym_concreteTypes] = {
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
  [aux_sym__fullPkgName_repeat1] = {
    .visible = false,
    .named = false,
  },
  [aux_sym_packagePath_repeat1] = {
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
    {field_generic, 1},
    {field_name, 0},
  [4] =
    {field_name, 1},
    {field_package, 0},
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
  [5] = 2,
  [6] = 6,
  [7] = 7,
  [8] = 8,
  [9] = 9,
  [10] = 10,
  [11] = 11,
  [12] = 12,
  [13] = 13,
  [14] = 14,
  [15] = 13,
  [16] = 16,
  [17] = 17,
  [18] = 18,
  [19] = 19,
  [20] = 20,
  [21] = 14,
  [22] = 22,
  [23] = 23,
  [24] = 24,
  [25] = 25,
  [26] = 19,
  [27] = 27,
  [28] = 20,
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
  [39] = 31,
  [40] = 36,
  [41] = 41,
  [42] = 42,
  [43] = 43,
  [44] = 44,
  [45] = 45,
  [46] = 32,
  [47] = 47,
  [48] = 33,
  [49] = 34,
  [50] = 50,
  [51] = 51,
  [52] = 50,
  [53] = 53,
};

static bool ts_lex(TSLexer *lexer, TSStateId state) {
  START_LEXER();
  eof = lexer->eof(lexer);
  switch (state) {
    case 0:
      if (eof) ADVANCE(34);
      ADVANCE_MAP(
        ',', 41,
        '.', 37,
        '[', 44,
        ']', 45,
        '_', 5,
        'a', 23,
        'i', 24,
        'm', 33,
        'p', 7,
        'r', 15,
        't', 28,
      );
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(0);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(43);
      END_STATE();
    case 1:
      if (lookahead == ' ') ADVANCE(36);
      if (lookahead == '[') ADVANCE(44);
      if (('\t' <= lookahead && lookahead <= '\r')) SKIP(1);
      END_STATE();
    case 2:
      if (lookahead == 'D') ADVANCE(17);
      END_STATE();
    case 3:
      if (lookahead == 'M') ADVANCE(14);
      END_STATE();
    case 4:
      if (lookahead == ']') ADVANCE(45);
      if (lookahead == '_') ADVANCE(6);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(4);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(43);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(38);
      END_STATE();
    case 5:
      if (lookahead == '_') ADVANCE(5);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(43);
      END_STATE();
    case 6:
      if (lookahead == '_') ADVANCE(6);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(43);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(38);
      END_STATE();
    case 7:
      if (lookahead == 'a') ADVANCE(11);
      END_STATE();
    case 8:
      if (lookahead == 'a') ADVANCE(13);
      if (lookahead == 'c') ADVANCE(3);
      END_STATE();
    case 9:
      if (lookahead == 'a') ADVANCE(31);
      END_STATE();
    case 10:
      if (lookahead == 'a') ADVANCE(19);
      END_STATE();
    case 11:
      if (lookahead == 'c') ADVANCE(22);
      END_STATE();
    case 12:
      if (lookahead == 'c') ADVANCE(54);
      END_STATE();
    case 13:
      if (lookahead == 'd') ADVANCE(50);
      END_STATE();
    case 14:
      if (lookahead == 'd') ADVANCE(18);
      END_STATE();
    case 15:
      if (lookahead == 'e') ADVANCE(8);
      END_STATE();
    case 16:
      if (lookahead == 'e') ADVANCE(35);
      END_STATE();
    case 17:
      if (lookahead == 'e') ADVANCE(12);
      END_STATE();
    case 18:
      if (lookahead == 'f') ADVANCE(52);
      END_STATE();
    case 19:
      if (lookahead == 'g') ADVANCE(16);
      END_STATE();
    case 20:
      if (lookahead == 'i') ADVANCE(9);
      END_STATE();
    case 21:
      if (lookahead == 'i') ADVANCE(27);
      END_STATE();
    case 22:
      if (lookahead == 'k') ADVANCE(10);
      END_STATE();
    case 23:
      if (lookahead == 'l') ADVANCE(20);
      if (lookahead == 's') ADVANCE(40);
      END_STATE();
    case 24:
      if (lookahead == 'm') ADVANCE(25);
      if (lookahead == 's') ADVANCE(29);
      END_STATE();
    case 25:
      if (lookahead == 'm') ADVANCE(53);
      END_STATE();
    case 26:
      if (lookahead == 'm') ADVANCE(49);
      END_STATE();
    case 27:
      if (lookahead == 'm') ADVANCE(26);
      END_STATE();
    case 28:
      if (lookahead == 'o') ADVANCE(30);
      END_STATE();
    case 29:
      if (lookahead == 'o') ADVANCE(51);
      END_STATE();
    case 30:
      if (lookahead == 'p') ADVANCE(2);
      END_STATE();
    case 31:
      if (lookahead == 's') ADVANCE(39);
      END_STATE();
    case 32:
      if (lookahead == 't') ADVANCE(46);
      END_STATE();
    case 33:
      if (lookahead == 'u') ADVANCE(32);
      END_STATE();
    case 34:
      ACCEPT_TOKEN(ts_builtin_sym_end);
      END_STATE();
    case 35:
      ACCEPT_TOKEN(anon_sym_package);
      END_STATE();
    case 36:
      ACCEPT_TOKEN(anon_sym_SPACE);
      if (lookahead == ' ') ADVANCE(36);
      END_STATE();
    case 37:
      ACCEPT_TOKEN(anon_sym_DOT);
      END_STATE();
    case 38:
      ACCEPT_TOKEN(sym__pkgName);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(38);
      END_STATE();
    case 39:
      ACCEPT_TOKEN(anon_sym_alias);
      END_STATE();
    case 40:
      ACCEPT_TOKEN(anon_sym_as);
      END_STATE();
    case 41:
      ACCEPT_TOKEN(anon_sym_COMMA);
      END_STATE();
    case 42:
      ACCEPT_TOKEN(sym_typeName);
      if (lookahead == '\'') ADVANCE(42);
      END_STATE();
    case 43:
      ACCEPT_TOKEN(sym_typeName);
      if (lookahead == '\'') ADVANCE(42);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(43);
      END_STATE();
    case 44:
      ACCEPT_TOKEN(anon_sym_LBRACK);
      END_STATE();
    case 45:
      ACCEPT_TOKEN(anon_sym_RBRACK);
      END_STATE();
    case 46:
      ACCEPT_TOKEN(anon_sym_mut);
      if (lookahead == 'H') ADVANCE(48);
      END_STATE();
    case 47:
      ACCEPT_TOKEN(anon_sym_readH);
      END_STATE();
    case 48:
      ACCEPT_TOKEN(anon_sym_mutH);
      END_STATE();
    case 49:
      ACCEPT_TOKEN(anon_sym_read_SLASHimm);
      END_STATE();
    case 50:
      ACCEPT_TOKEN(anon_sym_read);
      if (lookahead == '/') ADVANCE(21);
      if (lookahead == 'H') ADVANCE(47);
      END_STATE();
    case 51:
      ACCEPT_TOKEN(anon_sym_iso);
      END_STATE();
    case 52:
      ACCEPT_TOKEN(anon_sym_recMdf);
      END_STATE();
    case 53:
      ACCEPT_TOKEN(anon_sym_imm);
      END_STATE();
    case 54:
      ACCEPT_TOKEN(sym_topDec);
      END_STATE();
    default:
      return false;
  }
}

static const TSLexMode ts_lex_modes[STATE_COUNT] = {
  [0] = {.lex_state = 0},
  [1] = {.lex_state = 0},
  [2] = {.lex_state = 4},
  [3] = {.lex_state = 0},
  [4] = {.lex_state = 0},
  [5] = {.lex_state = 4},
  [6] = {.lex_state = 4},
  [7] = {.lex_state = 0},
  [8] = {.lex_state = 0},
  [9] = {.lex_state = 4},
  [10] = {.lex_state = 0},
  [11] = {.lex_state = 0},
  [12] = {.lex_state = 0},
  [13] = {.lex_state = 0},
  [14] = {.lex_state = 0},
  [15] = {.lex_state = 1},
  [16] = {.lex_state = 4},
  [17] = {.lex_state = 0},
  [18] = {.lex_state = 0},
  [19] = {.lex_state = 0},
  [20] = {.lex_state = 0},
  [21] = {.lex_state = 1},
  [22] = {.lex_state = 0},
  [23] = {.lex_state = 0},
  [24] = {.lex_state = 4},
  [25] = {.lex_state = 0},
  [26] = {.lex_state = 0},
  [27] = {.lex_state = 0},
  [28] = {.lex_state = 0},
  [29] = {.lex_state = 4},
  [30] = {.lex_state = 0},
  [31] = {.lex_state = 0},
  [32] = {.lex_state = 0},
  [33] = {.lex_state = 0},
  [34] = {.lex_state = 0},
  [35] = {.lex_state = 4},
  [36] = {.lex_state = 0},
  [37] = {.lex_state = 4},
  [38] = {.lex_state = 0},
  [39] = {.lex_state = 1},
  [40] = {.lex_state = 1},
  [41] = {.lex_state = 0},
  [42] = {.lex_state = 1},
  [43] = {.lex_state = 1},
  [44] = {.lex_state = 0},
  [45] = {.lex_state = 1},
  [46] = {.lex_state = 1},
  [47] = {.lex_state = 1},
  [48] = {.lex_state = 1},
  [49] = {.lex_state = 1},
  [50] = {.lex_state = 0},
  [51] = {.lex_state = 0},
  [52] = {.lex_state = 0},
  [53] = {.lex_state = 0},
};

static const uint16_t ts_parse_table[LARGE_STATE_COUNT][SYMBOL_COUNT] = {
  [0] = {
    [ts_builtin_sym_end] = ACTIONS(1),
    [anon_sym_package] = ACTIONS(1),
    [anon_sym_DOT] = ACTIONS(1),
    [anon_sym_alias] = ACTIONS(1),
    [anon_sym_as] = ACTIONS(1),
    [anon_sym_COMMA] = ACTIONS(1),
    [sym_typeName] = ACTIONS(1),
    [anon_sym_LBRACK] = ACTIONS(1),
    [anon_sym_RBRACK] = ACTIONS(1),
    [anon_sym_mut] = ACTIONS(1),
    [anon_sym_readH] = ACTIONS(1),
    [anon_sym_mutH] = ACTIONS(1),
    [anon_sym_read_SLASHimm] = ACTIONS(1),
    [anon_sym_read] = ACTIONS(1),
    [anon_sym_iso] = ACTIONS(1),
    [anon_sym_recMdf] = ACTIONS(1),
    [anon_sym_imm] = ACTIONS(1),
    [sym_topDec] = ACTIONS(1),
  },
  [1] = {
    [sym_source_file] = STATE(51),
    [sym_package] = STATE(3),
    [anon_sym_package] = ACTIONS(3),
  },
};

static const uint16_t ts_small_parse_table[] = {
  [0] = 6,
    ACTIONS(5), 1,
      sym__pkgName,
    ACTIONS(7), 1,
      sym_typeName,
    ACTIONS(9), 1,
      anon_sym_RBRACK,
    STATE(20), 1,
      sym_concreteType,
    STATE(24), 1,
      aux_sym_packagePath_repeat1,
    STATE(50), 1,
      sym_packagePath,
  [19] = 5,
    ACTIONS(11), 1,
      ts_builtin_sym_end,
    ACTIONS(13), 1,
      anon_sym_alias,
    ACTIONS(15), 1,
      sym_topDec,
    STATE(27), 1,
      aux_sym_source_file_repeat2,
    STATE(4), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [36] = 5,
    ACTIONS(13), 1,
      anon_sym_alias,
    ACTIONS(17), 1,
      ts_builtin_sym_end,
    ACTIONS(19), 1,
      sym_topDec,
    STATE(17), 1,
      aux_sym_source_file_repeat2,
    STATE(10), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [53] = 6,
    ACTIONS(5), 1,
      sym__pkgName,
    ACTIONS(7), 1,
      sym_typeName,
    ACTIONS(21), 1,
      anon_sym_RBRACK,
    STATE(24), 1,
      aux_sym_packagePath_repeat1,
    STATE(28), 1,
      sym_concreteType,
    STATE(50), 1,
      sym_packagePath,
  [72] = 5,
    ACTIONS(5), 1,
      sym__pkgName,
    ACTIONS(7), 1,
      sym_typeName,
    STATE(24), 1,
      aux_sym_packagePath_repeat1,
    STATE(30), 1,
      sym_concreteType,
    STATE(50), 1,
      sym_packagePath,
  [88] = 3,
    ACTIONS(25), 1,
      anon_sym_DOT,
    STATE(8), 1,
      aux_sym__fullPkgName_repeat1,
    ACTIONS(23), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [100] = 3,
    ACTIONS(25), 1,
      anon_sym_DOT,
    STATE(11), 1,
      aux_sym__fullPkgName_repeat1,
    ACTIONS(27), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [112] = 5,
    ACTIONS(5), 1,
      sym__pkgName,
    ACTIONS(29), 1,
      sym_typeName,
    STATE(24), 1,
      aux_sym_packagePath_repeat1,
    STATE(42), 1,
      sym_concreteType,
    STATE(52), 1,
      sym_packagePath,
  [128] = 3,
    ACTIONS(33), 1,
      anon_sym_alias,
    ACTIONS(31), 2,
      ts_builtin_sym_end,
      sym_topDec,
    STATE(10), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [140] = 3,
    ACTIONS(38), 1,
      anon_sym_DOT,
    STATE(11), 1,
      aux_sym__fullPkgName_repeat1,
    ACTIONS(36), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [152] = 1,
    ACTIONS(36), 4,
      ts_builtin_sym_end,
      anon_sym_DOT,
      anon_sym_alias,
      sym_topDec,
  [159] = 3,
    ACTIONS(43), 1,
      anon_sym_LBRACK,
    STATE(33), 1,
      sym_concreteTypes,
    ACTIONS(41), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [170] = 3,
    ACTIONS(43), 1,
      anon_sym_LBRACK,
    STATE(36), 1,
      sym_concreteTypes,
    ACTIONS(45), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [181] = 3,
    ACTIONS(41), 1,
      anon_sym_SPACE,
    ACTIONS(47), 1,
      anon_sym_LBRACK,
    STATE(48), 1,
      sym_concreteTypes,
  [191] = 3,
    ACTIONS(49), 1,
      sym__pkgName,
    ACTIONS(52), 1,
      sym_typeName,
    STATE(16), 1,
      aux_sym_packagePath_repeat1,
  [201] = 3,
    ACTIONS(54), 1,
      ts_builtin_sym_end,
    ACTIONS(56), 1,
      sym_topDec,
    STATE(18), 1,
      aux_sym_source_file_repeat2,
  [211] = 3,
    ACTIONS(58), 1,
      ts_builtin_sym_end,
    ACTIONS(60), 1,
      sym_topDec,
    STATE(18), 1,
      aux_sym_source_file_repeat2,
  [221] = 3,
    ACTIONS(63), 1,
      anon_sym_COMMA,
    ACTIONS(65), 1,
      anon_sym_RBRACK,
    STATE(23), 1,
      aux_sym_concreteTypes_repeat1,
  [231] = 3,
    ACTIONS(63), 1,
      anon_sym_COMMA,
    ACTIONS(67), 1,
      anon_sym_RBRACK,
    STATE(19), 1,
      aux_sym_concreteTypes_repeat1,
  [241] = 3,
    ACTIONS(45), 1,
      anon_sym_SPACE,
    ACTIONS(47), 1,
      anon_sym_LBRACK,
    STATE(40), 1,
      sym_concreteTypes,
  [251] = 1,
    ACTIONS(69), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [257] = 3,
    ACTIONS(71), 1,
      anon_sym_COMMA,
    ACTIONS(74), 1,
      anon_sym_RBRACK,
    STATE(23), 1,
      aux_sym_concreteTypes_repeat1,
  [267] = 3,
    ACTIONS(5), 1,
      sym__pkgName,
    ACTIONS(76), 1,
      sym_typeName,
    STATE(16), 1,
      aux_sym_packagePath_repeat1,
  [277] = 1,
    ACTIONS(78), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [283] = 3,
    ACTIONS(63), 1,
      anon_sym_COMMA,
    ACTIONS(80), 1,
      anon_sym_RBRACK,
    STATE(23), 1,
      aux_sym_concreteTypes_repeat1,
  [293] = 3,
    ACTIONS(17), 1,
      ts_builtin_sym_end,
    ACTIONS(56), 1,
      sym_topDec,
    STATE(18), 1,
      aux_sym_source_file_repeat2,
  [303] = 3,
    ACTIONS(63), 1,
      anon_sym_COMMA,
    ACTIONS(82), 1,
      anon_sym_RBRACK,
    STATE(26), 1,
      aux_sym_concreteTypes_repeat1,
  [313] = 1,
    ACTIONS(52), 2,
      sym__pkgName,
      sym_typeName,
  [318] = 1,
    ACTIONS(74), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [323] = 1,
    ACTIONS(84), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [328] = 1,
    ACTIONS(86), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [333] = 1,
    ACTIONS(88), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [338] = 1,
    ACTIONS(90), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [343] = 2,
    ACTIONS(92), 1,
      sym__pkgName,
    STATE(25), 1,
      sym__fullPkgName,
  [350] = 1,
    ACTIONS(94), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [355] = 1,
    ACTIONS(96), 1,
      sym__pkgName,
  [359] = 1,
    ACTIONS(98), 1,
      sym_typeName,
  [363] = 1,
    ACTIONS(84), 1,
      anon_sym_SPACE,
  [367] = 1,
    ACTIONS(94), 1,
      anon_sym_SPACE,
  [371] = 1,
    ACTIONS(100), 1,
      anon_sym_COMMA,
  [375] = 1,
    ACTIONS(102), 1,
      anon_sym_SPACE,
  [379] = 1,
    ACTIONS(104), 1,
      anon_sym_SPACE,
  [383] = 1,
    ACTIONS(106), 1,
      anon_sym_as,
  [387] = 1,
    ACTIONS(108), 1,
      anon_sym_SPACE,
  [391] = 1,
    ACTIONS(86), 1,
      anon_sym_SPACE,
  [395] = 1,
    ACTIONS(110), 1,
      anon_sym_SPACE,
  [399] = 1,
    ACTIONS(88), 1,
      anon_sym_SPACE,
  [403] = 1,
    ACTIONS(90), 1,
      anon_sym_SPACE,
  [407] = 1,
    ACTIONS(112), 1,
      sym_typeName,
  [411] = 1,
    ACTIONS(114), 1,
      ts_builtin_sym_end,
  [415] = 1,
    ACTIONS(116), 1,
      sym_typeName,
  [419] = 1,
    ACTIONS(118), 1,
      anon_sym_DOT,
};

static const uint32_t ts_small_parse_table_map[] = {
  [SMALL_STATE(2)] = 0,
  [SMALL_STATE(3)] = 19,
  [SMALL_STATE(4)] = 36,
  [SMALL_STATE(5)] = 53,
  [SMALL_STATE(6)] = 72,
  [SMALL_STATE(7)] = 88,
  [SMALL_STATE(8)] = 100,
  [SMALL_STATE(9)] = 112,
  [SMALL_STATE(10)] = 128,
  [SMALL_STATE(11)] = 140,
  [SMALL_STATE(12)] = 152,
  [SMALL_STATE(13)] = 159,
  [SMALL_STATE(14)] = 170,
  [SMALL_STATE(15)] = 181,
  [SMALL_STATE(16)] = 191,
  [SMALL_STATE(17)] = 201,
  [SMALL_STATE(18)] = 211,
  [SMALL_STATE(19)] = 221,
  [SMALL_STATE(20)] = 231,
  [SMALL_STATE(21)] = 241,
  [SMALL_STATE(22)] = 251,
  [SMALL_STATE(23)] = 257,
  [SMALL_STATE(24)] = 267,
  [SMALL_STATE(25)] = 277,
  [SMALL_STATE(26)] = 283,
  [SMALL_STATE(27)] = 293,
  [SMALL_STATE(28)] = 303,
  [SMALL_STATE(29)] = 313,
  [SMALL_STATE(30)] = 318,
  [SMALL_STATE(31)] = 323,
  [SMALL_STATE(32)] = 328,
  [SMALL_STATE(33)] = 333,
  [SMALL_STATE(34)] = 338,
  [SMALL_STATE(35)] = 343,
  [SMALL_STATE(36)] = 350,
  [SMALL_STATE(37)] = 355,
  [SMALL_STATE(38)] = 359,
  [SMALL_STATE(39)] = 363,
  [SMALL_STATE(40)] = 367,
  [SMALL_STATE(41)] = 371,
  [SMALL_STATE(42)] = 375,
  [SMALL_STATE(43)] = 379,
  [SMALL_STATE(44)] = 383,
  [SMALL_STATE(45)] = 387,
  [SMALL_STATE(46)] = 391,
  [SMALL_STATE(47)] = 395,
  [SMALL_STATE(48)] = 399,
  [SMALL_STATE(49)] = 403,
  [SMALL_STATE(50)] = 407,
  [SMALL_STATE(51)] = 411,
  [SMALL_STATE(52)] = 415,
  [SMALL_STATE(53)] = 419,
};

static const TSParseActionEntry ts_parse_actions[] = {
  [0] = {.entry = {.count = 0, .reusable = false}},
  [1] = {.entry = {.count = 1, .reusable = false}}, RECOVER(),
  [3] = {.entry = {.count = 1, .reusable = true}}, SHIFT(45),
  [5] = {.entry = {.count = 1, .reusable = true}}, SHIFT(53),
  [7] = {.entry = {.count = 1, .reusable = true}}, SHIFT(14),
  [9] = {.entry = {.count = 1, .reusable = true}}, SHIFT(34),
  [11] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 1, 0, 0),
  [13] = {.entry = {.count = 1, .reusable = true}}, SHIFT(43),
  [15] = {.entry = {.count = 1, .reusable = true}}, SHIFT(27),
  [17] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 2, 0, 0),
  [19] = {.entry = {.count = 1, .reusable = true}}, SHIFT(17),
  [21] = {.entry = {.count = 1, .reusable = true}}, SHIFT(49),
  [23] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fullPkgName, 1, 0, 0),
  [25] = {.entry = {.count = 1, .reusable = true}}, SHIFT(37),
  [27] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fullPkgName, 2, 0, 0),
  [29] = {.entry = {.count = 1, .reusable = true}}, SHIFT(21),
  [31] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0),
  [33] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0), SHIFT_REPEAT(43),
  [36] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__fullPkgName_repeat1, 2, 0, 0),
  [38] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__fullPkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(37),
  [41] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteType, 2, 0, 4),
  [43] = {.entry = {.count = 1, .reusable = true}}, SHIFT(2),
  [45] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteType, 1, 0, 2),
  [47] = {.entry = {.count = 1, .reusable = false}}, SHIFT(5),
  [49] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_packagePath_repeat1, 2, 0, 0), SHIFT_REPEAT(53),
  [52] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_packagePath_repeat1, 2, 0, 0),
  [54] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 3, 0, 0),
  [56] = {.entry = {.count = 1, .reusable = true}}, SHIFT(18),
  [58] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0),
  [60] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0), SHIFT_REPEAT(18),
  [63] = {.entry = {.count = 1, .reusable = true}}, SHIFT(6),
  [65] = {.entry = {.count = 1, .reusable = true}}, SHIFT(31),
  [67] = {.entry = {.count = 1, .reusable = true}}, SHIFT(32),
  [69] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_alias, 8, 0, 6),
  [71] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_concreteTypes_repeat1, 2, 0, 0), SHIFT_REPEAT(6),
  [74] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_concreteTypes_repeat1, 2, 0, 0),
  [76] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_packagePath, 1, 0, 0),
  [78] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_package, 3, 0, 1),
  [80] = {.entry = {.count = 1, .reusable = true}}, SHIFT(39),
  [82] = {.entry = {.count = 1, .reusable = true}}, SHIFT(46),
  [84] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteTypes, 4, 0, 0),
  [86] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteTypes, 3, 0, 0),
  [88] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteType, 3, 0, 5),
  [90] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteTypes, 2, 0, 0),
  [92] = {.entry = {.count = 1, .reusable = true}}, SHIFT(7),
  [94] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteType, 2, 0, 3),
  [96] = {.entry = {.count = 1, .reusable = true}}, SHIFT(12),
  [98] = {.entry = {.count = 1, .reusable = true}}, SHIFT(41),
  [100] = {.entry = {.count = 1, .reusable = true}}, SHIFT(22),
  [102] = {.entry = {.count = 1, .reusable = true}}, SHIFT(44),
  [104] = {.entry = {.count = 1, .reusable = true}}, SHIFT(9),
  [106] = {.entry = {.count = 1, .reusable = true}}, SHIFT(47),
  [108] = {.entry = {.count = 1, .reusable = true}}, SHIFT(35),
  [110] = {.entry = {.count = 1, .reusable = true}}, SHIFT(38),
  [112] = {.entry = {.count = 1, .reusable = true}}, SHIFT(13),
  [114] = {.entry = {.count = 1, .reusable = true}},  ACCEPT_INPUT(),
  [116] = {.entry = {.count = 1, .reusable = true}}, SHIFT(15),
  [118] = {.entry = {.count = 1, .reusable = true}}, SHIFT(29),
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
