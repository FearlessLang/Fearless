#include "tree_sitter/parser.h"

#if defined(__GNUC__) || defined(__clang__)
#pragma GCC diagnostic ignored "-Wmissing-field-initializers"
#endif

#define LANGUAGE_VERSION 14
#define STATE_COUNT 76
#define LARGE_STATE_COUNT 2
#define SYMBOL_COUNT 43
#define ALIAS_COUNT 0
#define TOKEN_COUNT 24
#define EXTERNAL_TOKEN_COUNT 0
#define FIELD_COUNT 3
#define MAX_ALIAS_SEQUENCE_LENGTH 7
#define PRODUCTION_ID_COUNT 4

enum ts_symbol_identifiers {
  anon_sym_package = 1,
  anon_sym_LF = 2,
  anon_sym_DOT = 3,
  anon_sym_alias = 4,
  anon_sym_as = 5,
  anon_sym_COMMA = 6,
  anon_sym_LBRACK = 7,
  anon_sym_RBRACK = 8,
  anon_sym_COLON = 9,
  anon_sym_mut = 10,
  anon_sym_readH = 11,
  anon_sym_mutH = 12,
  anon_sym_read_SLASHimm = 13,
  anon_sym_read = 14,
  anon_sym_iso = 15,
  anon_sym_recMdf = 16,
  anon_sym_imm = 17,
  sym_topDec = 18,
  aux_sym__idLow_token1 = 19,
  aux_sym__idLow_token2 = 20,
  sym__idUp = 21,
  sym__idChar = 22,
  anon_sym_SQUOTE = 23,
  sym_source_file = 24,
  sym_package = 25,
  sym_packagePath = 26,
  sym__pkgName = 27,
  sym_alias = 28,
  sym_fullCN = 29,
  sym_mGen = 30,
  sym__genDecl = 31,
  sym__t = 32,
  sym_mdf = 33,
  sym__idLow = 34,
  sym__fIdUp = 35,
  aux_sym_source_file_repeat1 = 36,
  aux_sym_source_file_repeat2 = 37,
  aux_sym_packagePath_repeat1 = 38,
  aux_sym__pkgName_repeat1 = 39,
  aux_sym_mGen_repeat1 = 40,
  aux_sym__genDecl_repeat1 = 41,
  aux_sym__fIdUp_repeat1 = 42,
};

static const char * const ts_symbol_names[] = {
  [ts_builtin_sym_end] = "end",
  [anon_sym_package] = "package ",
  [anon_sym_LF] = "\n",
  [anon_sym_DOT] = ".",
  [anon_sym_alias] = "alias ",
  [anon_sym_as] = "as",
  [anon_sym_COMMA] = ",",
  [anon_sym_LBRACK] = "[",
  [anon_sym_RBRACK] = "]",
  [anon_sym_COLON] = ":",
  [anon_sym_mut] = "mut",
  [anon_sym_readH] = "readH",
  [anon_sym_mutH] = "mutH",
  [anon_sym_read_SLASHimm] = "read/imm",
  [anon_sym_read] = "read",
  [anon_sym_iso] = "iso",
  [anon_sym_recMdf] = "recMdf",
  [anon_sym_imm] = "imm",
  [sym_topDec] = "topDec",
  [aux_sym__idLow_token1] = "_idLow_token1",
  [aux_sym__idLow_token2] = "_idLow_token2",
  [sym__idUp] = "_idUp",
  [sym__idChar] = "_idChar",
  [anon_sym_SQUOTE] = "'",
  [sym_source_file] = "source_file",
  [sym_package] = "package",
  [sym_packagePath] = "packagePath",
  [sym__pkgName] = "_pkgName",
  [sym_alias] = "alias",
  [sym_fullCN] = "fullCN",
  [sym_mGen] = "mGen",
  [sym__genDecl] = "_genDecl",
  [sym__t] = "_t",
  [sym_mdf] = "mdf",
  [sym__idLow] = "_idLow",
  [sym__fIdUp] = "type",
  [aux_sym_source_file_repeat1] = "source_file_repeat1",
  [aux_sym_source_file_repeat2] = "source_file_repeat2",
  [aux_sym_packagePath_repeat1] = "packagePath_repeat1",
  [aux_sym__pkgName_repeat1] = "_pkgName_repeat1",
  [aux_sym_mGen_repeat1] = "mGen_repeat1",
  [aux_sym__genDecl_repeat1] = "_genDecl_repeat1",
  [aux_sym__fIdUp_repeat1] = "_fIdUp_repeat1",
};

static const TSSymbol ts_symbol_map[] = {
  [ts_builtin_sym_end] = ts_builtin_sym_end,
  [anon_sym_package] = anon_sym_package,
  [anon_sym_LF] = anon_sym_LF,
  [anon_sym_DOT] = anon_sym_DOT,
  [anon_sym_alias] = anon_sym_alias,
  [anon_sym_as] = anon_sym_as,
  [anon_sym_COMMA] = anon_sym_COMMA,
  [anon_sym_LBRACK] = anon_sym_LBRACK,
  [anon_sym_RBRACK] = anon_sym_RBRACK,
  [anon_sym_COLON] = anon_sym_COLON,
  [anon_sym_mut] = anon_sym_mut,
  [anon_sym_readH] = anon_sym_readH,
  [anon_sym_mutH] = anon_sym_mutH,
  [anon_sym_read_SLASHimm] = anon_sym_read_SLASHimm,
  [anon_sym_read] = anon_sym_read,
  [anon_sym_iso] = anon_sym_iso,
  [anon_sym_recMdf] = anon_sym_recMdf,
  [anon_sym_imm] = anon_sym_imm,
  [sym_topDec] = sym_topDec,
  [aux_sym__idLow_token1] = aux_sym__idLow_token1,
  [aux_sym__idLow_token2] = aux_sym__idLow_token2,
  [sym__idUp] = sym__idUp,
  [sym__idChar] = sym__idChar,
  [anon_sym_SQUOTE] = anon_sym_SQUOTE,
  [sym_source_file] = sym_source_file,
  [sym_package] = sym_package,
  [sym_packagePath] = sym_packagePath,
  [sym__pkgName] = sym__pkgName,
  [sym_alias] = sym_alias,
  [sym_fullCN] = sym_fullCN,
  [sym_mGen] = sym_mGen,
  [sym__genDecl] = sym__genDecl,
  [sym__t] = sym__t,
  [sym_mdf] = sym_mdf,
  [sym__idLow] = sym__idLow,
  [sym__fIdUp] = sym__fIdUp,
  [aux_sym_source_file_repeat1] = aux_sym_source_file_repeat1,
  [aux_sym_source_file_repeat2] = aux_sym_source_file_repeat2,
  [aux_sym_packagePath_repeat1] = aux_sym_packagePath_repeat1,
  [aux_sym__pkgName_repeat1] = aux_sym__pkgName_repeat1,
  [aux_sym_mGen_repeat1] = aux_sym_mGen_repeat1,
  [aux_sym__genDecl_repeat1] = aux_sym__genDecl_repeat1,
  [aux_sym__fIdUp_repeat1] = aux_sym__fIdUp_repeat1,
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
  [sym_fullCN] = {
    .visible = true,
    .named = true,
  },
  [sym_mGen] = {
    .visible = true,
    .named = true,
  },
  [sym__genDecl] = {
    .visible = false,
    .named = true,
  },
  [sym__t] = {
    .visible = false,
    .named = true,
  },
  [sym_mdf] = {
    .visible = true,
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
  [aux_sym_packagePath_repeat1] = {
    .visible = false,
    .named = false,
  },
  [aux_sym__pkgName_repeat1] = {
    .visible = false,
    .named = false,
  },
  [aux_sym_mGen_repeat1] = {
    .visible = false,
    .named = false,
  },
  [aux_sym__genDecl_repeat1] = {
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
  [3] = {.index = 2, .length = 3},
};

static const TSFieldMapEntry ts_field_map_entries[] = {
  [0] =
    {field_name, 1},
  [1] =
    {field_type, 0},
  [2] =
    {field_package, 0},
    {field_package, 1},
    {field_type, 2},
};

static const TSSymbol ts_alias_sequences[PRODUCTION_ID_COUNT][MAX_ALIAS_SEQUENCE_LENGTH] = {
  [0] = {0},
  [3] = {
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
  [8] = 8,
  [9] = 9,
  [10] = 10,
  [11] = 11,
  [12] = 12,
  [13] = 13,
  [14] = 14,
  [15] = 15,
  [16] = 12,
  [17] = 17,
  [18] = 18,
  [19] = 11,
  [20] = 20,
  [21] = 21,
  [22] = 22,
  [23] = 23,
  [24] = 24,
  [25] = 25,
  [26] = 26,
  [27] = 27,
  [28] = 28,
  [29] = 26,
  [30] = 10,
  [31] = 10,
  [32] = 32,
  [33] = 33,
  [34] = 34,
  [35] = 35,
  [36] = 36,
  [37] = 37,
  [38] = 38,
  [39] = 39,
  [40] = 40,
  [41] = 32,
  [42] = 42,
  [43] = 43,
  [44] = 44,
  [45] = 45,
  [46] = 46,
  [47] = 47,
  [48] = 48,
  [49] = 49,
  [50] = 50,
  [51] = 51,
  [52] = 34,
  [53] = 53,
  [54] = 54,
  [55] = 55,
  [56] = 56,
  [57] = 57,
  [58] = 58,
  [59] = 59,
  [60] = 60,
  [61] = 61,
  [62] = 62,
  [63] = 63,
  [64] = 62,
  [65] = 65,
  [66] = 66,
  [67] = 63,
  [68] = 68,
  [69] = 59,
  [70] = 70,
  [71] = 71,
  [72] = 72,
  [73] = 73,
  [74] = 74,
  [75] = 71,
};

static bool ts_lex(TSLexer *lexer, TSStateId state) {
  START_LEXER();
  eof = lexer->eof(lexer);
  switch (state) {
    case 0:
      if (eof) ADVANCE(39);
      if (lookahead == '\'') ADVANCE(66);
      if (lookahead == ',') ADVANCE(45);
      if (lookahead == '.') ADVANCE(42);
      if (lookahead == ':') ADVANCE(48);
      if (lookahead == '[') ADVANCE(46);
      if (lookahead == ']') ADVANCE(47);
      if (lookahead == 'a') ADVANCE(65);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(0);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('b' <= lookahead && lookahead <= 'z')) ADVANCE(64);
      END_STATE();
    case 1:
      if (lookahead == '\n') ADVANCE(41);
      if (lookahead == '.') ADVANCE(42);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(1);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(64);
      END_STATE();
    case 2:
      if (lookahead == ' ') ADVANCE(43);
      END_STATE();
    case 3:
      if (lookahead == ' ') ADVANCE(40);
      END_STATE();
    case 4:
      if (lookahead == '\'') ADVANCE(66);
      if (lookahead == ',') ADVANCE(45);
      if (lookahead == '.') ADVANCE(42);
      if (lookahead == ':') ADVANCE(48);
      if (lookahead == '[') ADVANCE(46);
      if (lookahead == ']') ADVANCE(47);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(4);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(64);
      END_STATE();
    case 5:
      if (lookahead == ',') ADVANCE(45);
      if (lookahead == ']') ADVANCE(47);
      if (lookahead == '_') ADVANCE(10);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(5);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(63);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(58);
      END_STATE();
    case 6:
      if (lookahead == 'D') ADVANCE(20);
      END_STATE();
    case 7:
      if (lookahead == 'M') ADVANCE(18);
      END_STATE();
    case 8:
      if (lookahead == ']') ADVANCE(47);
      if (lookahead == '_') ADVANCE(10);
      if (lookahead == 'i') ADVANCE(60);
      if (lookahead == 'm') ADVANCE(61);
      if (lookahead == 'r') ADVANCE(59);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(8);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(58);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(63);
      END_STATE();
    case 9:
      if (lookahead == '_') ADVANCE(9);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(63);
      END_STATE();
    case 10:
      if (lookahead == '_') ADVANCE(10);
      if (('0' <= lookahead && lookahead <= '9')) ADVANCE(62);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(63);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(58);
      END_STATE();
    case 11:
      if (lookahead == 'a') ADVANCE(15);
      END_STATE();
    case 12:
      if (lookahead == 'a') ADVANCE(17);
      if (lookahead == 'c') ADVANCE(7);
      END_STATE();
    case 13:
      if (lookahead == 'a') ADVANCE(35);
      END_STATE();
    case 14:
      if (lookahead == 'a') ADVANCE(23);
      END_STATE();
    case 15:
      if (lookahead == 'c') ADVANCE(26);
      END_STATE();
    case 16:
      if (lookahead == 'c') ADVANCE(57);
      END_STATE();
    case 17:
      if (lookahead == 'd') ADVANCE(53);
      END_STATE();
    case 18:
      if (lookahead == 'd') ADVANCE(22);
      END_STATE();
    case 19:
      if (lookahead == 'e') ADVANCE(12);
      END_STATE();
    case 20:
      if (lookahead == 'e') ADVANCE(16);
      END_STATE();
    case 21:
      if (lookahead == 'e') ADVANCE(3);
      END_STATE();
    case 22:
      if (lookahead == 'f') ADVANCE(55);
      END_STATE();
    case 23:
      if (lookahead == 'g') ADVANCE(21);
      END_STATE();
    case 24:
      if (lookahead == 'i') ADVANCE(13);
      END_STATE();
    case 25:
      if (lookahead == 'i') ADVANCE(31);
      END_STATE();
    case 26:
      if (lookahead == 'k') ADVANCE(14);
      END_STATE();
    case 27:
      if (lookahead == 'l') ADVANCE(24);
      END_STATE();
    case 28:
      if (lookahead == 'm') ADVANCE(29);
      if (lookahead == 's') ADVANCE(33);
      END_STATE();
    case 29:
      if (lookahead == 'm') ADVANCE(56);
      END_STATE();
    case 30:
      if (lookahead == 'm') ADVANCE(52);
      END_STATE();
    case 31:
      if (lookahead == 'm') ADVANCE(30);
      END_STATE();
    case 32:
      if (lookahead == 'o') ADVANCE(34);
      END_STATE();
    case 33:
      if (lookahead == 'o') ADVANCE(54);
      END_STATE();
    case 34:
      if (lookahead == 'p') ADVANCE(6);
      END_STATE();
    case 35:
      if (lookahead == 's') ADVANCE(2);
      END_STATE();
    case 36:
      if (lookahead == 't') ADVANCE(49);
      END_STATE();
    case 37:
      if (lookahead == 'u') ADVANCE(36);
      END_STATE();
    case 38:
      if (eof) ADVANCE(39);
      ADVANCE_MAP(
        ',', 45,
        ']', 47,
        '_', 9,
        'a', 27,
        'i', 28,
        'm', 37,
        'p', 11,
        'r', 19,
        't', 32,
      );
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(38);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(63);
      END_STATE();
    case 39:
      ACCEPT_TOKEN(ts_builtin_sym_end);
      END_STATE();
    case 40:
      ACCEPT_TOKEN(anon_sym_package);
      END_STATE();
    case 41:
      ACCEPT_TOKEN(anon_sym_LF);
      if (lookahead == '\n') ADVANCE(41);
      END_STATE();
    case 42:
      ACCEPT_TOKEN(anon_sym_DOT);
      END_STATE();
    case 43:
      ACCEPT_TOKEN(anon_sym_alias);
      END_STATE();
    case 44:
      ACCEPT_TOKEN(anon_sym_as);
      END_STATE();
    case 45:
      ACCEPT_TOKEN(anon_sym_COMMA);
      END_STATE();
    case 46:
      ACCEPT_TOKEN(anon_sym_LBRACK);
      END_STATE();
    case 47:
      ACCEPT_TOKEN(anon_sym_RBRACK);
      END_STATE();
    case 48:
      ACCEPT_TOKEN(anon_sym_COLON);
      END_STATE();
    case 49:
      ACCEPT_TOKEN(anon_sym_mut);
      if (lookahead == 'H') ADVANCE(51);
      END_STATE();
    case 50:
      ACCEPT_TOKEN(anon_sym_readH);
      END_STATE();
    case 51:
      ACCEPT_TOKEN(anon_sym_mutH);
      END_STATE();
    case 52:
      ACCEPT_TOKEN(anon_sym_read_SLASHimm);
      END_STATE();
    case 53:
      ACCEPT_TOKEN(anon_sym_read);
      if (lookahead == '/') ADVANCE(25);
      if (lookahead == 'H') ADVANCE(50);
      END_STATE();
    case 54:
      ACCEPT_TOKEN(anon_sym_iso);
      END_STATE();
    case 55:
      ACCEPT_TOKEN(anon_sym_recMdf);
      END_STATE();
    case 56:
      ACCEPT_TOKEN(anon_sym_imm);
      END_STATE();
    case 57:
      ACCEPT_TOKEN(sym_topDec);
      END_STATE();
    case 58:
      ACCEPT_TOKEN(aux_sym__idLow_token1);
      END_STATE();
    case 59:
      ACCEPT_TOKEN(aux_sym__idLow_token1);
      if (lookahead == 'e') ADVANCE(12);
      END_STATE();
    case 60:
      ACCEPT_TOKEN(aux_sym__idLow_token1);
      if (lookahead == 'm') ADVANCE(29);
      if (lookahead == 's') ADVANCE(33);
      END_STATE();
    case 61:
      ACCEPT_TOKEN(aux_sym__idLow_token1);
      if (lookahead == 'u') ADVANCE(36);
      END_STATE();
    case 62:
      ACCEPT_TOKEN(aux_sym__idLow_token2);
      END_STATE();
    case 63:
      ACCEPT_TOKEN(sym__idUp);
      END_STATE();
    case 64:
      ACCEPT_TOKEN(sym__idChar);
      END_STATE();
    case 65:
      ACCEPT_TOKEN(sym__idChar);
      if (lookahead == 's') ADVANCE(44);
      END_STATE();
    case 66:
      ACCEPT_TOKEN(anon_sym_SQUOTE);
      END_STATE();
    default:
      return false;
  }
}

static const TSLexMode ts_lex_modes[STATE_COUNT] = {
  [0] = {.lex_state = 0},
  [1] = {.lex_state = 38},
  [2] = {.lex_state = 8},
  [3] = {.lex_state = 8},
  [4] = {.lex_state = 38},
  [5] = {.lex_state = 38},
  [6] = {.lex_state = 5},
  [7] = {.lex_state = 5},
  [8] = {.lex_state = 5},
  [9] = {.lex_state = 5},
  [10] = {.lex_state = 4},
  [11] = {.lex_state = 4},
  [12] = {.lex_state = 4},
  [13] = {.lex_state = 0},
  [14] = {.lex_state = 0},
  [15] = {.lex_state = 0},
  [16] = {.lex_state = 0},
  [17] = {.lex_state = 5},
  [18] = {.lex_state = 38},
  [19] = {.lex_state = 0},
  [20] = {.lex_state = 38},
  [21] = {.lex_state = 0},
  [22] = {.lex_state = 5},
  [23] = {.lex_state = 0},
  [24] = {.lex_state = 0},
  [25] = {.lex_state = 38},
  [26] = {.lex_state = 5},
  [27] = {.lex_state = 0},
  [28] = {.lex_state = 5},
  [29] = {.lex_state = 5},
  [30] = {.lex_state = 0},
  [31] = {.lex_state = 1},
  [32] = {.lex_state = 1},
  [33] = {.lex_state = 0},
  [34] = {.lex_state = 1},
  [35] = {.lex_state = 0},
  [36] = {.lex_state = 0},
  [37] = {.lex_state = 0},
  [38] = {.lex_state = 38},
  [39] = {.lex_state = 0},
  [40] = {.lex_state = 0},
  [41] = {.lex_state = 4},
  [42] = {.lex_state = 38},
  [43] = {.lex_state = 38},
  [44] = {.lex_state = 38},
  [45] = {.lex_state = 0},
  [46] = {.lex_state = 0},
  [47] = {.lex_state = 0},
  [48] = {.lex_state = 0},
  [49] = {.lex_state = 38},
  [50] = {.lex_state = 38},
  [51] = {.lex_state = 0},
  [52] = {.lex_state = 4},
  [53] = {.lex_state = 0},
  [54] = {.lex_state = 38},
  [55] = {.lex_state = 0},
  [56] = {.lex_state = 0},
  [57] = {.lex_state = 0},
  [58] = {.lex_state = 0},
  [59] = {.lex_state = 1},
  [60] = {.lex_state = 0},
  [61] = {.lex_state = 0},
  [62] = {.lex_state = 38},
  [63] = {.lex_state = 1},
  [64] = {.lex_state = 38},
  [65] = {.lex_state = 5},
  [66] = {.lex_state = 0},
  [67] = {.lex_state = 0},
  [68] = {.lex_state = 0},
  [69] = {.lex_state = 0},
  [70] = {.lex_state = 0},
  [71] = {.lex_state = 0},
  [72] = {.lex_state = 0},
  [73] = {.lex_state = 0},
  [74] = {.lex_state = 1},
  [75] = {.lex_state = 0},
};

static const uint16_t ts_parse_table[LARGE_STATE_COUNT][SYMBOL_COUNT] = {
  [0] = {
    [ts_builtin_sym_end] = ACTIONS(1),
    [anon_sym_DOT] = ACTIONS(1),
    [anon_sym_as] = ACTIONS(1),
    [anon_sym_COMMA] = ACTIONS(1),
    [anon_sym_LBRACK] = ACTIONS(1),
    [anon_sym_RBRACK] = ACTIONS(1),
    [anon_sym_COLON] = ACTIONS(1),
    [sym__idChar] = ACTIONS(1),
    [anon_sym_SQUOTE] = ACTIONS(1),
  },
  [1] = {
    [sym_source_file] = STATE(70),
    [sym_package] = STATE(20),
    [anon_sym_package] = ACTIONS(3),
  },
};

static const uint16_t ts_small_parse_table[] = {
  [0] = 15,
    ACTIONS(5), 1,
      anon_sym_RBRACK,
    ACTIONS(11), 1,
      aux_sym__idLow_token1,
    ACTIONS(13), 1,
      aux_sym__idLow_token2,
    ACTIONS(15), 1,
      sym__idUp,
    STATE(6), 1,
      sym_mdf,
    STATE(21), 1,
      sym_fullCN,
    STATE(24), 1,
      sym__fIdUp,
    STATE(26), 1,
      aux_sym_packagePath_repeat1,
    STATE(39), 1,
      sym__genDecl,
    STATE(40), 1,
      sym__t,
    STATE(41), 1,
      sym__idLow,
    STATE(67), 1,
      sym__pkgName,
    STATE(75), 1,
      sym_packagePath,
    ACTIONS(7), 2,
      anon_sym_mut,
      anon_sym_read,
    ACTIONS(9), 6,
      anon_sym_readH,
      anon_sym_mutH,
      anon_sym_read_SLASHimm,
      anon_sym_iso,
      anon_sym_recMdf,
      anon_sym_imm,
  [52] = 14,
    ACTIONS(11), 1,
      aux_sym__idLow_token1,
    ACTIONS(13), 1,
      aux_sym__idLow_token2,
    ACTIONS(15), 1,
      sym__idUp,
    STATE(6), 1,
      sym_mdf,
    STATE(21), 1,
      sym_fullCN,
    STATE(24), 1,
      sym__fIdUp,
    STATE(26), 1,
      aux_sym_packagePath_repeat1,
    STATE(40), 1,
      sym__t,
    STATE(41), 1,
      sym__idLow,
    STATE(61), 1,
      sym__genDecl,
    STATE(67), 1,
      sym__pkgName,
    STATE(75), 1,
      sym_packagePath,
    ACTIONS(7), 2,
      anon_sym_mut,
      anon_sym_read,
    ACTIONS(9), 6,
      anon_sym_readH,
      anon_sym_mutH,
      anon_sym_read_SLASHimm,
      anon_sym_iso,
      anon_sym_recMdf,
      anon_sym_imm,
  [101] = 5,
    STATE(57), 1,
      sym_mdf,
    STATE(58), 1,
      aux_sym__genDecl_repeat1,
    ACTIONS(7), 2,
      anon_sym_mut,
      anon_sym_read,
    ACTIONS(17), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
    ACTIONS(9), 6,
      anon_sym_readH,
      anon_sym_mutH,
      anon_sym_read_SLASHimm,
      anon_sym_iso,
      anon_sym_recMdf,
      anon_sym_imm,
  [124] = 4,
    STATE(60), 1,
      sym_mdf,
    ACTIONS(7), 2,
      anon_sym_mut,
      anon_sym_read,
    ACTIONS(19), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
    ACTIONS(9), 6,
      anon_sym_readH,
      anon_sym_mutH,
      anon_sym_read_SLASHimm,
      anon_sym_iso,
      anon_sym_recMdf,
      anon_sym_imm,
  [144] = 8,
    ACTIONS(15), 1,
      sym__idUp,
    STATE(23), 1,
      sym_fullCN,
    STATE(24), 1,
      sym__fIdUp,
    STATE(26), 1,
      aux_sym_packagePath_repeat1,
    STATE(41), 1,
      sym__idLow,
    STATE(67), 1,
      sym__pkgName,
    STATE(75), 1,
      sym_packagePath,
    ACTIONS(13), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [170] = 8,
    ACTIONS(15), 1,
      sym__idUp,
    STATE(24), 1,
      sym__fIdUp,
    STATE(26), 1,
      aux_sym_packagePath_repeat1,
    STATE(41), 1,
      sym__idLow,
    STATE(47), 1,
      sym_fullCN,
    STATE(67), 1,
      sym__pkgName,
    STATE(75), 1,
      sym_packagePath,
    ACTIONS(13), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [196] = 8,
    ACTIONS(15), 1,
      sym__idUp,
    STATE(24), 1,
      sym__fIdUp,
    STATE(26), 1,
      aux_sym_packagePath_repeat1,
    STATE(41), 1,
      sym__idLow,
    STATE(51), 1,
      sym_fullCN,
    STATE(67), 1,
      sym__pkgName,
    STATE(75), 1,
      sym_packagePath,
    ACTIONS(13), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [222] = 8,
    ACTIONS(21), 1,
      sym__idUp,
    STATE(24), 1,
      sym__fIdUp,
    STATE(26), 1,
      aux_sym_packagePath_repeat1,
    STATE(37), 1,
      sym_fullCN,
    STATE(41), 1,
      sym__idLow,
    STATE(67), 1,
      sym__pkgName,
    STATE(71), 1,
      sym_packagePath,
    ACTIONS(13), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [248] = 3,
    ACTIONS(25), 1,
      sym__idChar,
    STATE(10), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(23), 6,
      anon_sym_DOT,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
      anon_sym_SQUOTE,
  [263] = 5,
    ACTIONS(30), 1,
      sym__idChar,
    ACTIONS(32), 1,
      anon_sym_SQUOTE,
    STATE(10), 1,
      aux_sym__pkgName_repeat1,
    STATE(14), 1,
      aux_sym__fIdUp_repeat1,
    ACTIONS(28), 4,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [282] = 5,
    ACTIONS(36), 1,
      sym__idChar,
    ACTIONS(38), 1,
      anon_sym_SQUOTE,
    STATE(11), 1,
      aux_sym__pkgName_repeat1,
    STATE(13), 1,
      aux_sym__fIdUp_repeat1,
    ACTIONS(34), 4,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [301] = 3,
    ACTIONS(40), 1,
      anon_sym_SQUOTE,
    STATE(15), 1,
      aux_sym__fIdUp_repeat1,
    ACTIONS(28), 5,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [315] = 3,
    ACTIONS(40), 1,
      anon_sym_SQUOTE,
    STATE(15), 1,
      aux_sym__fIdUp_repeat1,
    ACTIONS(42), 5,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [329] = 3,
    ACTIONS(46), 1,
      anon_sym_SQUOTE,
    STATE(15), 1,
      aux_sym__fIdUp_repeat1,
    ACTIONS(44), 5,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [343] = 5,
    ACTIONS(38), 1,
      anon_sym_SQUOTE,
    ACTIONS(49), 1,
      sym__idChar,
    STATE(13), 1,
      aux_sym__fIdUp_repeat1,
    STATE(19), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(34), 2,
      anon_sym_as,
      anon_sym_LBRACK,
  [360] = 5,
    STATE(29), 1,
      aux_sym_packagePath_repeat1,
    STATE(32), 1,
      sym__idLow,
    STATE(63), 1,
      sym__pkgName,
    STATE(74), 1,
      sym_packagePath,
    ACTIONS(51), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [377] = 5,
    ACTIONS(53), 1,
      ts_builtin_sym_end,
    ACTIONS(55), 1,
      anon_sym_alias,
    ACTIONS(57), 1,
      sym_topDec,
    STATE(50), 1,
      aux_sym_source_file_repeat2,
    STATE(25), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [394] = 5,
    ACTIONS(32), 1,
      anon_sym_SQUOTE,
    ACTIONS(59), 1,
      sym__idChar,
    STATE(14), 1,
      aux_sym__fIdUp_repeat1,
    STATE(30), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(28), 2,
      anon_sym_as,
      anon_sym_LBRACK,
  [411] = 5,
    ACTIONS(55), 1,
      anon_sym_alias,
    ACTIONS(61), 1,
      ts_builtin_sym_end,
    ACTIONS(63), 1,
      sym_topDec,
    STATE(44), 1,
      aux_sym_source_file_repeat2,
    STATE(18), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [428] = 3,
    ACTIONS(67), 1,
      anon_sym_LBRACK,
    STATE(45), 1,
      sym_mGen,
    ACTIONS(65), 3,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [440] = 4,
    STATE(22), 1,
      aux_sym_packagePath_repeat1,
    STATE(41), 1,
      sym__idLow,
    STATE(66), 1,
      sym__pkgName,
    ACTIONS(69), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [454] = 3,
    ACTIONS(67), 1,
      anon_sym_LBRACK,
    STATE(56), 1,
      sym_mGen,
    ACTIONS(72), 3,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [466] = 1,
    ACTIONS(74), 5,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [474] = 3,
    ACTIONS(78), 1,
      anon_sym_alias,
    ACTIONS(76), 2,
      ts_builtin_sym_end,
      sym_topDec,
    STATE(25), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [486] = 4,
    STATE(22), 1,
      aux_sym_packagePath_repeat1,
    STATE(41), 1,
      sym__idLow,
    STATE(69), 1,
      sym__pkgName,
    ACTIONS(13), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [500] = 1,
    ACTIONS(81), 5,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [508] = 1,
    ACTIONS(83), 5,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
      sym__idUp,
  [516] = 4,
    STATE(22), 1,
      aux_sym_packagePath_repeat1,
    STATE(32), 1,
      sym__idLow,
    STATE(59), 1,
      sym__pkgName,
    ACTIONS(51), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [530] = 3,
    ACTIONS(85), 1,
      sym__idChar,
    STATE(30), 1,
      aux_sym__pkgName_repeat1,
    ACTIONS(23), 3,
      anon_sym_as,
      anon_sym_LBRACK,
      anon_sym_SQUOTE,
  [542] = 4,
    ACTIONS(23), 1,
      anon_sym_LF,
    ACTIONS(88), 1,
      anon_sym_DOT,
    ACTIONS(90), 1,
      sym__idChar,
    STATE(31), 1,
      aux_sym__pkgName_repeat1,
  [555] = 4,
    ACTIONS(93), 1,
      anon_sym_LF,
    ACTIONS(95), 1,
      anon_sym_DOT,
    ACTIONS(97), 1,
      sym__idChar,
    STATE(34), 1,
      aux_sym__pkgName_repeat1,
  [568] = 1,
    ACTIONS(99), 4,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [575] = 4,
    ACTIONS(101), 1,
      anon_sym_LF,
    ACTIONS(103), 1,
      anon_sym_DOT,
    ACTIONS(105), 1,
      sym__idChar,
    STATE(31), 1,
      aux_sym__pkgName_repeat1,
  [588] = 1,
    ACTIONS(107), 4,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [595] = 1,
    ACTIONS(109), 4,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [602] = 3,
    ACTIONS(67), 1,
      anon_sym_LBRACK,
    ACTIONS(111), 1,
      anon_sym_as,
    STATE(73), 1,
      sym_mGen,
  [612] = 1,
    ACTIONS(113), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [618] = 3,
    ACTIONS(115), 1,
      anon_sym_COMMA,
    ACTIONS(117), 1,
      anon_sym_RBRACK,
    STATE(48), 1,
      aux_sym_mGen_repeat1,
  [628] = 2,
    ACTIONS(121), 1,
      anon_sym_COLON,
    ACTIONS(119), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [636] = 3,
    ACTIONS(93), 1,
      anon_sym_DOT,
    ACTIONS(123), 1,
      sym__idChar,
    STATE(52), 1,
      aux_sym__pkgName_repeat1,
  [646] = 1,
    ACTIONS(125), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [652] = 1,
    ACTIONS(127), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [658] = 3,
    ACTIONS(53), 1,
      ts_builtin_sym_end,
    ACTIONS(129), 1,
      sym_topDec,
    STATE(49), 1,
      aux_sym_source_file_repeat2,
  [668] = 1,
    ACTIONS(72), 3,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [674] = 3,
    ACTIONS(131), 1,
      anon_sym_COMMA,
    ACTIONS(134), 1,
      anon_sym_RBRACK,
    STATE(46), 1,
      aux_sym__genDecl_repeat1,
  [684] = 3,
    ACTIONS(67), 1,
      anon_sym_LBRACK,
    ACTIONS(136), 1,
      anon_sym_COMMA,
    STATE(72), 1,
      sym_mGen,
  [694] = 3,
    ACTIONS(115), 1,
      anon_sym_COMMA,
    ACTIONS(138), 1,
      anon_sym_RBRACK,
    STATE(55), 1,
      aux_sym_mGen_repeat1,
  [704] = 3,
    ACTIONS(140), 1,
      ts_builtin_sym_end,
    ACTIONS(142), 1,
      sym_topDec,
    STATE(49), 1,
      aux_sym_source_file_repeat2,
  [714] = 3,
    ACTIONS(129), 1,
      sym_topDec,
    ACTIONS(145), 1,
      ts_builtin_sym_end,
    STATE(49), 1,
      aux_sym_source_file_repeat2,
  [724] = 3,
    ACTIONS(67), 1,
      anon_sym_LBRACK,
    ACTIONS(147), 1,
      anon_sym_COMMA,
    STATE(68), 1,
      sym_mGen,
  [734] = 3,
    ACTIONS(30), 1,
      sym__idChar,
    ACTIONS(101), 1,
      anon_sym_DOT,
    STATE(10), 1,
      aux_sym__pkgName_repeat1,
  [744] = 2,
    STATE(46), 1,
      aux_sym__genDecl_repeat1,
    ACTIONS(149), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [752] = 1,
    ACTIONS(151), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [758] = 3,
    ACTIONS(153), 1,
      anon_sym_COMMA,
    ACTIONS(156), 1,
      anon_sym_RBRACK,
    STATE(55), 1,
      aux_sym_mGen_repeat1,
  [768] = 1,
    ACTIONS(158), 3,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [774] = 2,
    STATE(53), 1,
      aux_sym__genDecl_repeat1,
    ACTIONS(160), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [782] = 2,
    STATE(46), 1,
      aux_sym__genDecl_repeat1,
    ACTIONS(160), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [790] = 2,
    ACTIONS(162), 1,
      anon_sym_LF,
    ACTIONS(164), 1,
      anon_sym_DOT,
  [797] = 1,
    ACTIONS(134), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [802] = 1,
    ACTIONS(156), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [807] = 2,
    ACTIONS(21), 1,
      sym__idUp,
    STATE(27), 1,
      sym__fIdUp,
  [814] = 2,
    ACTIONS(164), 1,
      anon_sym_DOT,
    ACTIONS(166), 1,
      anon_sym_LF,
  [821] = 2,
    ACTIONS(15), 1,
      sym__idUp,
    STATE(27), 1,
      sym__fIdUp,
  [828] = 1,
    ACTIONS(168), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [833] = 1,
    ACTIONS(170), 1,
      anon_sym_DOT,
  [837] = 1,
    ACTIONS(166), 1,
      anon_sym_DOT,
  [841] = 1,
    ACTIONS(172), 1,
      anon_sym_COMMA,
  [845] = 1,
    ACTIONS(162), 1,
      anon_sym_DOT,
  [849] = 1,
    ACTIONS(174), 1,
      ts_builtin_sym_end,
  [853] = 1,
    ACTIONS(176), 1,
      anon_sym_DOT,
  [857] = 1,
    ACTIONS(147), 1,
      anon_sym_COMMA,
  [861] = 1,
    ACTIONS(178), 1,
      anon_sym_as,
  [865] = 1,
    ACTIONS(180), 1,
      anon_sym_LF,
  [869] = 1,
    ACTIONS(182), 1,
      anon_sym_DOT,
};

static const uint32_t ts_small_parse_table_map[] = {
  [SMALL_STATE(2)] = 0,
  [SMALL_STATE(3)] = 52,
  [SMALL_STATE(4)] = 101,
  [SMALL_STATE(5)] = 124,
  [SMALL_STATE(6)] = 144,
  [SMALL_STATE(7)] = 170,
  [SMALL_STATE(8)] = 196,
  [SMALL_STATE(9)] = 222,
  [SMALL_STATE(10)] = 248,
  [SMALL_STATE(11)] = 263,
  [SMALL_STATE(12)] = 282,
  [SMALL_STATE(13)] = 301,
  [SMALL_STATE(14)] = 315,
  [SMALL_STATE(15)] = 329,
  [SMALL_STATE(16)] = 343,
  [SMALL_STATE(17)] = 360,
  [SMALL_STATE(18)] = 377,
  [SMALL_STATE(19)] = 394,
  [SMALL_STATE(20)] = 411,
  [SMALL_STATE(21)] = 428,
  [SMALL_STATE(22)] = 440,
  [SMALL_STATE(23)] = 454,
  [SMALL_STATE(24)] = 466,
  [SMALL_STATE(25)] = 474,
  [SMALL_STATE(26)] = 486,
  [SMALL_STATE(27)] = 500,
  [SMALL_STATE(28)] = 508,
  [SMALL_STATE(29)] = 516,
  [SMALL_STATE(30)] = 530,
  [SMALL_STATE(31)] = 542,
  [SMALL_STATE(32)] = 555,
  [SMALL_STATE(33)] = 568,
  [SMALL_STATE(34)] = 575,
  [SMALL_STATE(35)] = 588,
  [SMALL_STATE(36)] = 595,
  [SMALL_STATE(37)] = 602,
  [SMALL_STATE(38)] = 612,
  [SMALL_STATE(39)] = 618,
  [SMALL_STATE(40)] = 628,
  [SMALL_STATE(41)] = 636,
  [SMALL_STATE(42)] = 646,
  [SMALL_STATE(43)] = 652,
  [SMALL_STATE(44)] = 658,
  [SMALL_STATE(45)] = 668,
  [SMALL_STATE(46)] = 674,
  [SMALL_STATE(47)] = 684,
  [SMALL_STATE(48)] = 694,
  [SMALL_STATE(49)] = 704,
  [SMALL_STATE(50)] = 714,
  [SMALL_STATE(51)] = 724,
  [SMALL_STATE(52)] = 734,
  [SMALL_STATE(53)] = 744,
  [SMALL_STATE(54)] = 752,
  [SMALL_STATE(55)] = 758,
  [SMALL_STATE(56)] = 768,
  [SMALL_STATE(57)] = 774,
  [SMALL_STATE(58)] = 782,
  [SMALL_STATE(59)] = 790,
  [SMALL_STATE(60)] = 797,
  [SMALL_STATE(61)] = 802,
  [SMALL_STATE(62)] = 807,
  [SMALL_STATE(63)] = 814,
  [SMALL_STATE(64)] = 821,
  [SMALL_STATE(65)] = 828,
  [SMALL_STATE(66)] = 833,
  [SMALL_STATE(67)] = 837,
  [SMALL_STATE(68)] = 841,
  [SMALL_STATE(69)] = 845,
  [SMALL_STATE(70)] = 849,
  [SMALL_STATE(71)] = 853,
  [SMALL_STATE(72)] = 857,
  [SMALL_STATE(73)] = 861,
  [SMALL_STATE(74)] = 865,
  [SMALL_STATE(75)] = 869,
};

static const TSParseActionEntry ts_parse_actions[] = {
  [0] = {.entry = {.count = 0, .reusable = false}},
  [1] = {.entry = {.count = 1, .reusable = false}}, RECOVER(),
  [3] = {.entry = {.count = 1, .reusable = true}}, SHIFT(17),
  [5] = {.entry = {.count = 1, .reusable = true}}, SHIFT(36),
  [7] = {.entry = {.count = 1, .reusable = false}}, SHIFT(28),
  [9] = {.entry = {.count = 1, .reusable = true}}, SHIFT(28),
  [11] = {.entry = {.count = 1, .reusable = false}}, SHIFT(41),
  [13] = {.entry = {.count = 1, .reusable = true}}, SHIFT(41),
  [15] = {.entry = {.count = 1, .reusable = true}}, SHIFT(12),
  [17] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__genDecl, 2, 0, 0),
  [19] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__genDecl_repeat1, 1, 0, 0),
  [21] = {.entry = {.count = 1, .reusable = true}}, SHIFT(16),
  [23] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0),
  [25] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(10),
  [28] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fIdUp, 2, 0, 0),
  [30] = {.entry = {.count = 1, .reusable = true}}, SHIFT(10),
  [32] = {.entry = {.count = 1, .reusable = true}}, SHIFT(14),
  [34] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fIdUp, 1, 0, 0),
  [36] = {.entry = {.count = 1, .reusable = true}}, SHIFT(11),
  [38] = {.entry = {.count = 1, .reusable = true}}, SHIFT(13),
  [40] = {.entry = {.count = 1, .reusable = true}}, SHIFT(15),
  [42] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fIdUp, 3, 0, 0),
  [44] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__fIdUp_repeat1, 2, 0, 0),
  [46] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__fIdUp_repeat1, 2, 0, 0), SHIFT_REPEAT(15),
  [49] = {.entry = {.count = 1, .reusable = false}}, SHIFT(19),
  [51] = {.entry = {.count = 1, .reusable = true}}, SHIFT(32),
  [53] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 2, 0, 0),
  [55] = {.entry = {.count = 1, .reusable = true}}, SHIFT(9),
  [57] = {.entry = {.count = 1, .reusable = true}}, SHIFT(50),
  [59] = {.entry = {.count = 1, .reusable = false}}, SHIFT(30),
  [61] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 1, 0, 0),
  [63] = {.entry = {.count = 1, .reusable = true}}, SHIFT(44),
  [65] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__t, 1, 0, 0),
  [67] = {.entry = {.count = 1, .reusable = true}}, SHIFT(2),
  [69] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_packagePath_repeat1, 2, 0, 0), SHIFT_REPEAT(41),
  [72] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__t, 2, 0, 0),
  [74] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_fullCN, 1, 0, 2),
  [76] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0),
  [78] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0), SHIFT_REPEAT(9),
  [81] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_fullCN, 3, 0, 3),
  [83] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_mdf, 1, 0, 0),
  [85] = {.entry = {.count = 2, .reusable = false}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(30),
  [88] = {.entry = {.count = 1, .reusable = false}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0),
  [90] = {.entry = {.count = 2, .reusable = false}}, REDUCE(aux_sym__pkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(31),
  [93] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__pkgName, 1, 0, 0),
  [95] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym__pkgName, 1, 0, 0),
  [97] = {.entry = {.count = 1, .reusable = false}}, SHIFT(34),
  [99] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_mGen, 4, 0, 0),
  [101] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__pkgName, 2, 0, 0),
  [103] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym__pkgName, 2, 0, 0),
  [105] = {.entry = {.count = 1, .reusable = false}}, SHIFT(31),
  [107] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_mGen, 3, 0, 0),
  [109] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_mGen, 2, 0, 0),
  [111] = {.entry = {.count = 1, .reusable = true}}, SHIFT(7),
  [113] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_alias, 6, 0, 0),
  [115] = {.entry = {.count = 1, .reusable = true}}, SHIFT(3),
  [117] = {.entry = {.count = 1, .reusable = true}}, SHIFT(35),
  [119] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__genDecl, 1, 0, 0),
  [121] = {.entry = {.count = 1, .reusable = true}}, SHIFT(4),
  [123] = {.entry = {.count = 1, .reusable = true}}, SHIFT(52),
  [125] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_alias, 7, 0, 0),
  [127] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_alias, 5, 0, 0),
  [129] = {.entry = {.count = 1, .reusable = true}}, SHIFT(49),
  [131] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__genDecl_repeat1, 2, 0, 0), SHIFT_REPEAT(5),
  [134] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__genDecl_repeat1, 2, 0, 0),
  [136] = {.entry = {.count = 1, .reusable = true}}, SHIFT(43),
  [138] = {.entry = {.count = 1, .reusable = true}}, SHIFT(33),
  [140] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0),
  [142] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0), SHIFT_REPEAT(49),
  [145] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 3, 0, 0),
  [147] = {.entry = {.count = 1, .reusable = true}}, SHIFT(38),
  [149] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__genDecl, 4, 0, 0),
  [151] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_package, 3, 0, 1),
  [153] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_mGen_repeat1, 2, 0, 0), SHIFT_REPEAT(3),
  [156] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_mGen_repeat1, 2, 0, 0),
  [158] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__t, 3, 0, 0),
  [160] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__genDecl, 3, 0, 0),
  [162] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_packagePath, 2, 0, 0),
  [164] = {.entry = {.count = 1, .reusable = false}}, SHIFT(65),
  [166] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_packagePath, 1, 0, 0),
  [168] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_packagePath_repeat1, 2, 0, 0),
  [170] = {.entry = {.count = 1, .reusable = true}}, SHIFT(65),
  [172] = {.entry = {.count = 1, .reusable = true}}, SHIFT(42),
  [174] = {.entry = {.count = 1, .reusable = true}},  ACCEPT_INPUT(),
  [176] = {.entry = {.count = 1, .reusable = true}}, SHIFT(62),
  [178] = {.entry = {.count = 1, .reusable = true}}, SHIFT(8),
  [180] = {.entry = {.count = 1, .reusable = true}}, SHIFT(54),
  [182] = {.entry = {.count = 1, .reusable = true}}, SHIFT(64),
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
