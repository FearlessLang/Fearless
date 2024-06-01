#include "tree_sitter/parser.h"

#if defined(__GNUC__) || defined(__clang__)
#pragma GCC diagnostic ignored "-Wmissing-field-initializers"
#endif

#define LANGUAGE_VERSION 14
#define STATE_COUNT 71
#define LARGE_STATE_COUNT 2
#define SYMBOL_COUNT 43
#define ALIAS_COUNT 0
#define TOKEN_COUNT 24
#define EXTERNAL_TOKEN_COUNT 0
#define FIELD_COUNT 3
#define MAX_ALIAS_SEQUENCE_LENGTH 7
#define PRODUCTION_ID_COUNT 4

enum ts_symbol_identifiers {
  anon_sym_alias = 1,
  anon_sym_as = 2,
  anon_sym_COMMA = 3,
  anon_sym_DOT = 4,
  anon_sym_LBRACK = 5,
  anon_sym_RBRACK = 6,
  anon_sym_COLON = 7,
  anon_sym_mut = 8,
  anon_sym_readH = 9,
  anon_sym_mutH = 10,
  anon_sym_read_SLASHimm = 11,
  anon_sym_read = 12,
  anon_sym_iso = 13,
  anon_sym_recMdf = 14,
  anon_sym_imm = 15,
  sym_topDec = 16,
  anon_sym_package = 17,
  anon_sym_LF = 18,
  aux_sym__idLow_token1 = 19,
  aux_sym__idLow_token2 = 20,
  sym__idUp = 21,
  sym__idChar = 22,
  anon_sym_SQUOTE = 23,
  sym_source_file = 24,
  sym_alias = 25,
  sym_fullCN = 26,
  sym_mGen = 27,
  sym__genDecl = 28,
  sym__t = 29,
  sym_mdf = 30,
  sym_package = 31,
  sym_packagePath = 32,
  sym__px = 33,
  sym__idLow = 34,
  sym__fIdUp = 35,
  aux_sym_source_file_repeat1 = 36,
  aux_sym_source_file_repeat2 = 37,
  aux_sym_fullCN_repeat1 = 38,
  aux_sym_mGen_repeat1 = 39,
  aux_sym__genDecl_repeat1 = 40,
  aux_sym__px_repeat1 = 41,
  aux_sym__fIdUp_repeat1 = 42,
};

static const char * const ts_symbol_names[] = {
  [ts_builtin_sym_end] = "end",
  [anon_sym_alias] = "alias ",
  [anon_sym_as] = "as",
  [anon_sym_COMMA] = ",",
  [anon_sym_DOT] = ".",
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
  [sym_mGen] = "mGen",
  [sym__genDecl] = "_genDecl",
  [sym__t] = "_t",
  [sym_mdf] = "mdf",
  [sym_package] = "package",
  [sym_packagePath] = "packagePath",
  [sym__px] = "_px",
  [sym__idLow] = "_idLow",
  [sym__fIdUp] = "type",
  [aux_sym_source_file_repeat1] = "source_file_repeat1",
  [aux_sym_source_file_repeat2] = "source_file_repeat2",
  [aux_sym_fullCN_repeat1] = "fullCN_repeat1",
  [aux_sym_mGen_repeat1] = "mGen_repeat1",
  [aux_sym__genDecl_repeat1] = "_genDecl_repeat1",
  [aux_sym__px_repeat1] = "_px_repeat1",
  [aux_sym__fIdUp_repeat1] = "_fIdUp_repeat1",
};

static const TSSymbol ts_symbol_map[] = {
  [ts_builtin_sym_end] = ts_builtin_sym_end,
  [anon_sym_alias] = anon_sym_alias,
  [anon_sym_as] = anon_sym_as,
  [anon_sym_COMMA] = anon_sym_COMMA,
  [anon_sym_DOT] = anon_sym_DOT,
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
  [sym_mGen] = sym_mGen,
  [sym__genDecl] = sym__genDecl,
  [sym__t] = sym__t,
  [sym_mdf] = sym_mdf,
  [sym_package] = sym_package,
  [sym_packagePath] = sym_packagePath,
  [sym__px] = sym__px,
  [sym__idLow] = sym__idLow,
  [sym__fIdUp] = sym__fIdUp,
  [aux_sym_source_file_repeat1] = aux_sym_source_file_repeat1,
  [aux_sym_source_file_repeat2] = aux_sym_source_file_repeat2,
  [aux_sym_fullCN_repeat1] = aux_sym_fullCN_repeat1,
  [aux_sym_mGen_repeat1] = aux_sym_mGen_repeat1,
  [aux_sym__genDecl_repeat1] = aux_sym__genDecl_repeat1,
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
  [anon_sym_as] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_COMMA] = {
    .visible = true,
    .named = false,
  },
  [anon_sym_DOT] = {
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
  [aux_sym_mGen_repeat1] = {
    .visible = false,
    .named = false,
  },
  [aux_sym__genDecl_repeat1] = {
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
  [16] = 13,
  [17] = 17,
  [18] = 18,
  [19] = 11,
  [20] = 20,
  [21] = 8,
  [22] = 22,
  [23] = 23,
  [24] = 24,
  [25] = 25,
  [26] = 26,
  [27] = 27,
  [28] = 28,
  [29] = 29,
  [30] = 7,
  [31] = 31,
  [32] = 32,
  [33] = 33,
  [34] = 7,
  [35] = 35,
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
  [51] = 51,
  [52] = 37,
  [53] = 35,
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
  [64] = 64,
  [65] = 65,
  [66] = 66,
  [67] = 67,
  [68] = 68,
  [69] = 69,
  [70] = 70,
};

static bool ts_lex(TSLexer *lexer, TSStateId state) {
  START_LEXER();
  eof = lexer->eof(lexer);
  switch (state) {
    case 0:
      if (eof) ADVANCE(38);
      if (lookahead == '\'') ADVANCE(65);
      if (lookahead == ',') ADVANCE(41);
      if (lookahead == '.') ADVANCE(42);
      if (lookahead == ':') ADVANCE(45);
      if (lookahead == '[') ADVANCE(43);
      if (lookahead == ']') ADVANCE(44);
      if (lookahead == 'a') ADVANCE(64);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(0);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('b' <= lookahead && lookahead <= 'z')) ADVANCE(63);
      END_STATE();
    case 1:
      if (lookahead == '\n') ADVANCE(56);
      if (lookahead == '.') ADVANCE(42);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(1);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(63);
      END_STATE();
    case 2:
      if (lookahead == ' ') ADVANCE(39);
      END_STATE();
    case 3:
      if (lookahead == ' ') ADVANCE(55);
      END_STATE();
    case 4:
      if (lookahead == '\'') ADVANCE(65);
      if (lookahead == ',') ADVANCE(41);
      if (lookahead == '.') ADVANCE(42);
      if (lookahead == ':') ADVANCE(45);
      if (lookahead == '[') ADVANCE(43);
      if (lookahead == ']') ADVANCE(44);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(4);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(63);
      END_STATE();
    case 5:
      if (lookahead == ',') ADVANCE(41);
      if (lookahead == ']') ADVANCE(44);
      if (lookahead == '_') ADVANCE(9);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(5);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(62);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(57);
      END_STATE();
    case 6:
      if (lookahead == 'D') ADVANCE(19);
      END_STATE();
    case 7:
      if (lookahead == 'M') ADVANCE(17);
      END_STATE();
    case 8:
      if (lookahead == ']') ADVANCE(44);
      if (lookahead == '_') ADVANCE(9);
      if (lookahead == 'i') ADVANCE(59);
      if (lookahead == 'm') ADVANCE(60);
      if (lookahead == 'r') ADVANCE(58);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(8);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(57);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(62);
      END_STATE();
    case 9:
      if (lookahead == '_') ADVANCE(9);
      if (('0' <= lookahead && lookahead <= '9')) ADVANCE(61);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(62);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(57);
      END_STATE();
    case 10:
      if (lookahead == 'a') ADVANCE(14);
      END_STATE();
    case 11:
      if (lookahead == 'a') ADVANCE(16);
      if (lookahead == 'c') ADVANCE(7);
      END_STATE();
    case 12:
      if (lookahead == 'a') ADVANCE(34);
      END_STATE();
    case 13:
      if (lookahead == 'a') ADVANCE(22);
      END_STATE();
    case 14:
      if (lookahead == 'c') ADVANCE(25);
      END_STATE();
    case 15:
      if (lookahead == 'c') ADVANCE(54);
      END_STATE();
    case 16:
      if (lookahead == 'd') ADVANCE(50);
      END_STATE();
    case 17:
      if (lookahead == 'd') ADVANCE(21);
      END_STATE();
    case 18:
      if (lookahead == 'e') ADVANCE(11);
      END_STATE();
    case 19:
      if (lookahead == 'e') ADVANCE(15);
      END_STATE();
    case 20:
      if (lookahead == 'e') ADVANCE(3);
      END_STATE();
    case 21:
      if (lookahead == 'f') ADVANCE(52);
      END_STATE();
    case 22:
      if (lookahead == 'g') ADVANCE(20);
      END_STATE();
    case 23:
      if (lookahead == 'i') ADVANCE(12);
      END_STATE();
    case 24:
      if (lookahead == 'i') ADVANCE(30);
      END_STATE();
    case 25:
      if (lookahead == 'k') ADVANCE(13);
      END_STATE();
    case 26:
      if (lookahead == 'l') ADVANCE(23);
      END_STATE();
    case 27:
      if (lookahead == 'm') ADVANCE(28);
      if (lookahead == 's') ADVANCE(32);
      END_STATE();
    case 28:
      if (lookahead == 'm') ADVANCE(53);
      END_STATE();
    case 29:
      if (lookahead == 'm') ADVANCE(49);
      END_STATE();
    case 30:
      if (lookahead == 'm') ADVANCE(29);
      END_STATE();
    case 31:
      if (lookahead == 'o') ADVANCE(33);
      END_STATE();
    case 32:
      if (lookahead == 'o') ADVANCE(51);
      END_STATE();
    case 33:
      if (lookahead == 'p') ADVANCE(6);
      END_STATE();
    case 34:
      if (lookahead == 's') ADVANCE(2);
      END_STATE();
    case 35:
      if (lookahead == 't') ADVANCE(46);
      END_STATE();
    case 36:
      if (lookahead == 'u') ADVANCE(35);
      END_STATE();
    case 37:
      if (eof) ADVANCE(38);
      ADVANCE_MAP(
        ',', 41,
        ']', 44,
        'a', 26,
        'i', 27,
        'm', 36,
        'p', 10,
        'r', 18,
        't', 31,
      );
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(37);
      END_STATE();
    case 38:
      ACCEPT_TOKEN(ts_builtin_sym_end);
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
      ACCEPT_TOKEN(anon_sym_DOT);
      END_STATE();
    case 43:
      ACCEPT_TOKEN(anon_sym_LBRACK);
      END_STATE();
    case 44:
      ACCEPT_TOKEN(anon_sym_RBRACK);
      END_STATE();
    case 45:
      ACCEPT_TOKEN(anon_sym_COLON);
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
      if (lookahead == '/') ADVANCE(24);
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
    case 55:
      ACCEPT_TOKEN(anon_sym_package);
      END_STATE();
    case 56:
      ACCEPT_TOKEN(anon_sym_LF);
      if (lookahead == '\n') ADVANCE(56);
      END_STATE();
    case 57:
      ACCEPT_TOKEN(aux_sym__idLow_token1);
      END_STATE();
    case 58:
      ACCEPT_TOKEN(aux_sym__idLow_token1);
      if (lookahead == 'e') ADVANCE(11);
      END_STATE();
    case 59:
      ACCEPT_TOKEN(aux_sym__idLow_token1);
      if (lookahead == 'm') ADVANCE(28);
      if (lookahead == 's') ADVANCE(32);
      END_STATE();
    case 60:
      ACCEPT_TOKEN(aux_sym__idLow_token1);
      if (lookahead == 'u') ADVANCE(35);
      END_STATE();
    case 61:
      ACCEPT_TOKEN(aux_sym__idLow_token2);
      END_STATE();
    case 62:
      ACCEPT_TOKEN(sym__idUp);
      END_STATE();
    case 63:
      ACCEPT_TOKEN(sym__idChar);
      END_STATE();
    case 64:
      ACCEPT_TOKEN(sym__idChar);
      if (lookahead == 's') ADVANCE(40);
      END_STATE();
    case 65:
      ACCEPT_TOKEN(anon_sym_SQUOTE);
      END_STATE();
    default:
      return false;
  }
}

static const TSLexMode ts_lex_modes[STATE_COUNT] = {
  [0] = {.lex_state = 0},
  [1] = {.lex_state = 37},
  [2] = {.lex_state = 8},
  [3] = {.lex_state = 8},
  [4] = {.lex_state = 37},
  [5] = {.lex_state = 37},
  [6] = {.lex_state = 5},
  [7] = {.lex_state = 4},
  [8] = {.lex_state = 4},
  [9] = {.lex_state = 5},
  [10] = {.lex_state = 5},
  [11] = {.lex_state = 4},
  [12] = {.lex_state = 5},
  [13] = {.lex_state = 5},
  [14] = {.lex_state = 0},
  [15] = {.lex_state = 0},
  [16] = {.lex_state = 5},
  [17] = {.lex_state = 0},
  [18] = {.lex_state = 5},
  [19] = {.lex_state = 0},
  [20] = {.lex_state = 5},
  [21] = {.lex_state = 0},
  [22] = {.lex_state = 37},
  [23] = {.lex_state = 37},
  [24] = {.lex_state = 0},
  [25] = {.lex_state = 37},
  [26] = {.lex_state = 0},
  [27] = {.lex_state = 0},
  [28] = {.lex_state = 5},
  [29] = {.lex_state = 5},
  [30] = {.lex_state = 0},
  [31] = {.lex_state = 0},
  [32] = {.lex_state = 0},
  [33] = {.lex_state = 0},
  [34] = {.lex_state = 1},
  [35] = {.lex_state = 1},
  [36] = {.lex_state = 0},
  [37] = {.lex_state = 1},
  [38] = {.lex_state = 37},
  [39] = {.lex_state = 5},
  [40] = {.lex_state = 0},
  [41] = {.lex_state = 0},
  [42] = {.lex_state = 37},
  [43] = {.lex_state = 0},
  [44] = {.lex_state = 37},
  [45] = {.lex_state = 37},
  [46] = {.lex_state = 0},
  [47] = {.lex_state = 0},
  [48] = {.lex_state = 0},
  [49] = {.lex_state = 37},
  [50] = {.lex_state = 0},
  [51] = {.lex_state = 37},
  [52] = {.lex_state = 4},
  [53] = {.lex_state = 4},
  [54] = {.lex_state = 0},
  [55] = {.lex_state = 37},
  [56] = {.lex_state = 0},
  [57] = {.lex_state = 0},
  [58] = {.lex_state = 0},
  [59] = {.lex_state = 0},
  [60] = {.lex_state = 0},
  [61] = {.lex_state = 0},
  [62] = {.lex_state = 0},
  [63] = {.lex_state = 1},
  [64] = {.lex_state = 1},
  [65] = {.lex_state = 0},
  [66] = {.lex_state = 0},
  [67] = {.lex_state = 0},
  [68] = {.lex_state = 0},
  [69] = {.lex_state = 0},
  [70] = {.lex_state = 1},
};

static const uint16_t ts_parse_table[LARGE_STATE_COUNT][SYMBOL_COUNT] = {
  [0] = {
    [ts_builtin_sym_end] = ACTIONS(1),
    [anon_sym_as] = ACTIONS(1),
    [anon_sym_COMMA] = ACTIONS(1),
    [anon_sym_DOT] = ACTIONS(1),
    [anon_sym_LBRACK] = ACTIONS(1),
    [anon_sym_RBRACK] = ACTIONS(1),
    [anon_sym_COLON] = ACTIONS(1),
    [sym__idChar] = ACTIONS(1),
    [anon_sym_SQUOTE] = ACTIONS(1),
  },
  [1] = {
    [sym_source_file] = STATE(66),
    [sym_package] = STATE(23),
    [anon_sym_package] = ACTIONS(3),
  },
};

static const uint16_t ts_small_parse_table[] = {
  [0] = 14,
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
    STATE(13), 1,
      aux_sym_fullCN_repeat1,
    STATE(24), 1,
      sym__fIdUp,
    STATE(26), 1,
      sym_fullCN,
    STATE(40), 1,
      sym__genDecl,
    STATE(41), 1,
      sym__t,
    STATE(53), 1,
      sym__idLow,
    STATE(69), 1,
      sym__px,
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
  [49] = 13,
    ACTIONS(11), 1,
      aux_sym__idLow_token1,
    ACTIONS(13), 1,
      aux_sym__idLow_token2,
    ACTIONS(15), 1,
      sym__idUp,
    STATE(6), 1,
      sym_mdf,
    STATE(13), 1,
      aux_sym_fullCN_repeat1,
    STATE(24), 1,
      sym__fIdUp,
    STATE(26), 1,
      sym_fullCN,
    STATE(41), 1,
      sym__t,
    STATE(53), 1,
      sym__idLow,
    STATE(62), 1,
      sym__genDecl,
    STATE(69), 1,
      sym__px,
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
  [95] = 5,
    STATE(56), 1,
      sym_mdf,
    STATE(57), 1,
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
  [118] = 4,
    STATE(61), 1,
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
  [138] = 7,
    ACTIONS(15), 1,
      sym__idUp,
    STATE(13), 1,
      aux_sym_fullCN_repeat1,
    STATE(24), 1,
      sym__fIdUp,
    STATE(27), 1,
      sym_fullCN,
    STATE(53), 1,
      sym__idLow,
    STATE(69), 1,
      sym__px,
    ACTIONS(13), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [161] = 3,
    ACTIONS(23), 1,
      sym__idChar,
    STATE(7), 1,
      aux_sym__px_repeat1,
    ACTIONS(21), 6,
      anon_sym_COMMA,
      anon_sym_DOT,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
      anon_sym_SQUOTE,
  [176] = 5,
    ACTIONS(28), 1,
      sym__idChar,
    ACTIONS(30), 1,
      anon_sym_SQUOTE,
    STATE(7), 1,
      aux_sym__px_repeat1,
    STATE(17), 1,
      aux_sym__fIdUp_repeat1,
    ACTIONS(26), 4,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [195] = 7,
    ACTIONS(15), 1,
      sym__idUp,
    STATE(13), 1,
      aux_sym_fullCN_repeat1,
    STATE(24), 1,
      sym__fIdUp,
    STATE(43), 1,
      sym_fullCN,
    STATE(53), 1,
      sym__idLow,
    STATE(69), 1,
      sym__px,
    ACTIONS(13), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [218] = 7,
    ACTIONS(32), 1,
      sym__idUp,
    STATE(16), 1,
      aux_sym_fullCN_repeat1,
    STATE(24), 1,
      sym__fIdUp,
    STATE(47), 1,
      sym_fullCN,
    STATE(53), 1,
      sym__idLow,
    STATE(69), 1,
      sym__px,
    ACTIONS(13), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [241] = 5,
    ACTIONS(36), 1,
      sym__idChar,
    ACTIONS(38), 1,
      anon_sym_SQUOTE,
    STATE(8), 1,
      aux_sym__px_repeat1,
    STATE(14), 1,
      aux_sym__fIdUp_repeat1,
    ACTIONS(34), 4,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [260] = 7,
    ACTIONS(15), 1,
      sym__idUp,
    STATE(13), 1,
      aux_sym_fullCN_repeat1,
    STATE(24), 1,
      sym__fIdUp,
    STATE(50), 1,
      sym_fullCN,
    STATE(53), 1,
      sym__idLow,
    STATE(69), 1,
      sym__px,
    ACTIONS(13), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [283] = 6,
    ACTIONS(15), 1,
      sym__idUp,
    STATE(18), 1,
      aux_sym_fullCN_repeat1,
    STATE(31), 1,
      sym__fIdUp,
    STATE(53), 1,
      sym__idLow,
    STATE(69), 1,
      sym__px,
    ACTIONS(13), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [303] = 3,
    ACTIONS(40), 1,
      anon_sym_SQUOTE,
    STATE(15), 1,
      aux_sym__fIdUp_repeat1,
    ACTIONS(26), 5,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [317] = 3,
    ACTIONS(44), 1,
      anon_sym_SQUOTE,
    STATE(15), 1,
      aux_sym__fIdUp_repeat1,
    ACTIONS(42), 5,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [331] = 6,
    ACTIONS(32), 1,
      sym__idUp,
    STATE(18), 1,
      aux_sym_fullCN_repeat1,
    STATE(31), 1,
      sym__fIdUp,
    STATE(53), 1,
      sym__idLow,
    STATE(69), 1,
      sym__px,
    ACTIONS(13), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [351] = 3,
    ACTIONS(40), 1,
      anon_sym_SQUOTE,
    STATE(15), 1,
      aux_sym__fIdUp_repeat1,
    ACTIONS(47), 5,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [365] = 5,
    ACTIONS(52), 1,
      sym__idUp,
    STATE(18), 1,
      aux_sym_fullCN_repeat1,
    STATE(53), 1,
      sym__idLow,
    STATE(69), 1,
      sym__px,
    ACTIONS(49), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [382] = 5,
    ACTIONS(38), 1,
      anon_sym_SQUOTE,
    ACTIONS(54), 1,
      sym__idChar,
    STATE(14), 1,
      aux_sym__fIdUp_repeat1,
    STATE(21), 1,
      aux_sym__px_repeat1,
    ACTIONS(34), 2,
      anon_sym_as,
      anon_sym_LBRACK,
  [399] = 5,
    STATE(29), 1,
      aux_sym_fullCN_repeat1,
    STATE(35), 1,
      sym__idLow,
    STATE(64), 1,
      sym__px,
    STATE(70), 1,
      sym_packagePath,
    ACTIONS(56), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [416] = 5,
    ACTIONS(30), 1,
      anon_sym_SQUOTE,
    ACTIONS(58), 1,
      sym__idChar,
    STATE(17), 1,
      aux_sym__fIdUp_repeat1,
    STATE(30), 1,
      aux_sym__px_repeat1,
    ACTIONS(26), 2,
      anon_sym_as,
      anon_sym_LBRACK,
  [433] = 5,
    ACTIONS(60), 1,
      ts_builtin_sym_end,
    ACTIONS(62), 1,
      anon_sym_alias,
    ACTIONS(64), 1,
      sym_topDec,
    STATE(42), 1,
      aux_sym_source_file_repeat2,
    STATE(25), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [450] = 5,
    ACTIONS(62), 1,
      anon_sym_alias,
    ACTIONS(66), 1,
      ts_builtin_sym_end,
    ACTIONS(68), 1,
      sym_topDec,
    STATE(45), 1,
      aux_sym_source_file_repeat2,
    STATE(22), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [467] = 1,
    ACTIONS(70), 5,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [475] = 3,
    ACTIONS(74), 1,
      anon_sym_alias,
    ACTIONS(72), 2,
      ts_builtin_sym_end,
      sym_topDec,
    STATE(25), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [487] = 3,
    ACTIONS(79), 1,
      anon_sym_LBRACK,
    STATE(46), 1,
      sym_mGen,
    ACTIONS(77), 3,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [499] = 3,
    ACTIONS(79), 1,
      anon_sym_LBRACK,
    STATE(58), 1,
      sym_mGen,
    ACTIONS(81), 3,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [511] = 1,
    ACTIONS(83), 5,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
      sym__idUp,
  [519] = 4,
    STATE(18), 1,
      aux_sym_fullCN_repeat1,
    STATE(35), 1,
      sym__idLow,
    STATE(63), 1,
      sym__px,
    ACTIONS(56), 2,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
  [533] = 3,
    ACTIONS(85), 1,
      sym__idChar,
    STATE(30), 1,
      aux_sym__px_repeat1,
    ACTIONS(21), 3,
      anon_sym_as,
      anon_sym_LBRACK,
      anon_sym_SQUOTE,
  [545] = 1,
    ACTIONS(88), 5,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_LBRACK,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [553] = 1,
    ACTIONS(90), 4,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [560] = 1,
    ACTIONS(92), 4,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [567] = 4,
    ACTIONS(21), 1,
      anon_sym_LF,
    ACTIONS(94), 1,
      anon_sym_DOT,
    ACTIONS(96), 1,
      sym__idChar,
    STATE(34), 1,
      aux_sym__px_repeat1,
  [580] = 4,
    ACTIONS(99), 1,
      anon_sym_DOT,
    ACTIONS(101), 1,
      anon_sym_LF,
    ACTIONS(103), 1,
      sym__idChar,
    STATE(37), 1,
      aux_sym__px_repeat1,
  [593] = 1,
    ACTIONS(105), 4,
      anon_sym_as,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [600] = 4,
    ACTIONS(107), 1,
      anon_sym_DOT,
    ACTIONS(109), 1,
      anon_sym_LF,
    ACTIONS(111), 1,
      sym__idChar,
    STATE(34), 1,
      aux_sym__px_repeat1,
  [613] = 1,
    ACTIONS(113), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [619] = 1,
    ACTIONS(52), 3,
      aux_sym__idLow_token1,
      aux_sym__idLow_token2,
      sym__idUp,
  [625] = 3,
    ACTIONS(115), 1,
      anon_sym_COMMA,
    ACTIONS(117), 1,
      anon_sym_RBRACK,
    STATE(48), 1,
      aux_sym_mGen_repeat1,
  [635] = 2,
    ACTIONS(121), 1,
      anon_sym_COLON,
    ACTIONS(119), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [643] = 3,
    ACTIONS(123), 1,
      ts_builtin_sym_end,
    ACTIONS(125), 1,
      sym_topDec,
    STATE(51), 1,
      aux_sym_source_file_repeat2,
  [653] = 3,
    ACTIONS(79), 1,
      anon_sym_LBRACK,
    ACTIONS(127), 1,
      anon_sym_COMMA,
    STATE(68), 1,
      sym_mGen,
  [663] = 1,
    ACTIONS(129), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [669] = 3,
    ACTIONS(60), 1,
      ts_builtin_sym_end,
    ACTIONS(125), 1,
      sym_topDec,
    STATE(51), 1,
      aux_sym_source_file_repeat2,
  [679] = 1,
    ACTIONS(81), 3,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [685] = 3,
    ACTIONS(79), 1,
      anon_sym_LBRACK,
    ACTIONS(131), 1,
      anon_sym_as,
    STATE(67), 1,
      sym_mGen,
  [695] = 3,
    ACTIONS(115), 1,
      anon_sym_COMMA,
    ACTIONS(133), 1,
      anon_sym_RBRACK,
    STATE(54), 1,
      aux_sym_mGen_repeat1,
  [705] = 1,
    ACTIONS(135), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [711] = 3,
    ACTIONS(79), 1,
      anon_sym_LBRACK,
    ACTIONS(137), 1,
      anon_sym_COMMA,
    STATE(65), 1,
      sym_mGen,
  [721] = 3,
    ACTIONS(139), 1,
      ts_builtin_sym_end,
    ACTIONS(141), 1,
      sym_topDec,
    STATE(51), 1,
      aux_sym_source_file_repeat2,
  [731] = 3,
    ACTIONS(28), 1,
      sym__idChar,
    ACTIONS(109), 1,
      anon_sym_DOT,
    STATE(7), 1,
      aux_sym__px_repeat1,
  [741] = 3,
    ACTIONS(101), 1,
      anon_sym_DOT,
    ACTIONS(144), 1,
      sym__idChar,
    STATE(52), 1,
      aux_sym__px_repeat1,
  [751] = 3,
    ACTIONS(146), 1,
      anon_sym_COMMA,
    ACTIONS(149), 1,
      anon_sym_RBRACK,
    STATE(54), 1,
      aux_sym_mGen_repeat1,
  [761] = 1,
    ACTIONS(151), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [767] = 2,
    STATE(60), 1,
      aux_sym__genDecl_repeat1,
    ACTIONS(153), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [775] = 2,
    STATE(59), 1,
      aux_sym__genDecl_repeat1,
    ACTIONS(153), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [783] = 1,
    ACTIONS(155), 3,
      anon_sym_COMMA,
      anon_sym_RBRACK,
      anon_sym_COLON,
  [789] = 3,
    ACTIONS(157), 1,
      anon_sym_COMMA,
    ACTIONS(160), 1,
      anon_sym_RBRACK,
    STATE(59), 1,
      aux_sym__genDecl_repeat1,
  [799] = 2,
    STATE(59), 1,
      aux_sym__genDecl_repeat1,
    ACTIONS(162), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [807] = 1,
    ACTIONS(160), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [812] = 1,
    ACTIONS(149), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [817] = 2,
    ACTIONS(164), 1,
      anon_sym_DOT,
    ACTIONS(166), 1,
      anon_sym_LF,
  [824] = 2,
    ACTIONS(164), 1,
      anon_sym_DOT,
    ACTIONS(168), 1,
      anon_sym_LF,
  [831] = 1,
    ACTIONS(170), 1,
      anon_sym_COMMA,
  [835] = 1,
    ACTIONS(172), 1,
      ts_builtin_sym_end,
  [839] = 1,
    ACTIONS(174), 1,
      anon_sym_as,
  [843] = 1,
    ACTIONS(137), 1,
      anon_sym_COMMA,
  [847] = 1,
    ACTIONS(176), 1,
      anon_sym_DOT,
  [851] = 1,
    ACTIONS(178), 1,
      anon_sym_LF,
};

static const uint32_t ts_small_parse_table_map[] = {
  [SMALL_STATE(2)] = 0,
  [SMALL_STATE(3)] = 49,
  [SMALL_STATE(4)] = 95,
  [SMALL_STATE(5)] = 118,
  [SMALL_STATE(6)] = 138,
  [SMALL_STATE(7)] = 161,
  [SMALL_STATE(8)] = 176,
  [SMALL_STATE(9)] = 195,
  [SMALL_STATE(10)] = 218,
  [SMALL_STATE(11)] = 241,
  [SMALL_STATE(12)] = 260,
  [SMALL_STATE(13)] = 283,
  [SMALL_STATE(14)] = 303,
  [SMALL_STATE(15)] = 317,
  [SMALL_STATE(16)] = 331,
  [SMALL_STATE(17)] = 351,
  [SMALL_STATE(18)] = 365,
  [SMALL_STATE(19)] = 382,
  [SMALL_STATE(20)] = 399,
  [SMALL_STATE(21)] = 416,
  [SMALL_STATE(22)] = 433,
  [SMALL_STATE(23)] = 450,
  [SMALL_STATE(24)] = 467,
  [SMALL_STATE(25)] = 475,
  [SMALL_STATE(26)] = 487,
  [SMALL_STATE(27)] = 499,
  [SMALL_STATE(28)] = 511,
  [SMALL_STATE(29)] = 519,
  [SMALL_STATE(30)] = 533,
  [SMALL_STATE(31)] = 545,
  [SMALL_STATE(32)] = 553,
  [SMALL_STATE(33)] = 560,
  [SMALL_STATE(34)] = 567,
  [SMALL_STATE(35)] = 580,
  [SMALL_STATE(36)] = 593,
  [SMALL_STATE(37)] = 600,
  [SMALL_STATE(38)] = 613,
  [SMALL_STATE(39)] = 619,
  [SMALL_STATE(40)] = 625,
  [SMALL_STATE(41)] = 635,
  [SMALL_STATE(42)] = 643,
  [SMALL_STATE(43)] = 653,
  [SMALL_STATE(44)] = 663,
  [SMALL_STATE(45)] = 669,
  [SMALL_STATE(46)] = 679,
  [SMALL_STATE(47)] = 685,
  [SMALL_STATE(48)] = 695,
  [SMALL_STATE(49)] = 705,
  [SMALL_STATE(50)] = 711,
  [SMALL_STATE(51)] = 721,
  [SMALL_STATE(52)] = 731,
  [SMALL_STATE(53)] = 741,
  [SMALL_STATE(54)] = 751,
  [SMALL_STATE(55)] = 761,
  [SMALL_STATE(56)] = 767,
  [SMALL_STATE(57)] = 775,
  [SMALL_STATE(58)] = 783,
  [SMALL_STATE(59)] = 789,
  [SMALL_STATE(60)] = 799,
  [SMALL_STATE(61)] = 807,
  [SMALL_STATE(62)] = 812,
  [SMALL_STATE(63)] = 817,
  [SMALL_STATE(64)] = 824,
  [SMALL_STATE(65)] = 831,
  [SMALL_STATE(66)] = 835,
  [SMALL_STATE(67)] = 839,
  [SMALL_STATE(68)] = 843,
  [SMALL_STATE(69)] = 847,
  [SMALL_STATE(70)] = 851,
};

static const TSParseActionEntry ts_parse_actions[] = {
  [0] = {.entry = {.count = 0, .reusable = false}},
  [1] = {.entry = {.count = 1, .reusable = false}}, RECOVER(),
  [3] = {.entry = {.count = 1, .reusable = true}}, SHIFT(20),
  [5] = {.entry = {.count = 1, .reusable = true}}, SHIFT(32),
  [7] = {.entry = {.count = 1, .reusable = false}}, SHIFT(28),
  [9] = {.entry = {.count = 1, .reusable = true}}, SHIFT(28),
  [11] = {.entry = {.count = 1, .reusable = false}}, SHIFT(53),
  [13] = {.entry = {.count = 1, .reusable = true}}, SHIFT(53),
  [15] = {.entry = {.count = 1, .reusable = true}}, SHIFT(11),
  [17] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__genDecl, 2, 0, 0),
  [19] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__genDecl_repeat1, 1, 0, 0),
  [21] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__px_repeat1, 2, 0, 0),
  [23] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__px_repeat1, 2, 0, 0), SHIFT_REPEAT(7),
  [26] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fIdUp, 2, 0, 0),
  [28] = {.entry = {.count = 1, .reusable = true}}, SHIFT(7),
  [30] = {.entry = {.count = 1, .reusable = true}}, SHIFT(17),
  [32] = {.entry = {.count = 1, .reusable = true}}, SHIFT(19),
  [34] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fIdUp, 1, 0, 0),
  [36] = {.entry = {.count = 1, .reusable = true}}, SHIFT(8),
  [38] = {.entry = {.count = 1, .reusable = true}}, SHIFT(14),
  [40] = {.entry = {.count = 1, .reusable = true}}, SHIFT(15),
  [42] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__fIdUp_repeat1, 2, 0, 0),
  [44] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__fIdUp_repeat1, 2, 0, 0), SHIFT_REPEAT(15),
  [47] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fIdUp, 3, 0, 0),
  [49] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_fullCN_repeat1, 2, 0, 0), SHIFT_REPEAT(53),
  [52] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_fullCN_repeat1, 2, 0, 0),
  [54] = {.entry = {.count = 1, .reusable = false}}, SHIFT(21),
  [56] = {.entry = {.count = 1, .reusable = true}}, SHIFT(35),
  [58] = {.entry = {.count = 1, .reusable = false}}, SHIFT(30),
  [60] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 2, 0, 0),
  [62] = {.entry = {.count = 1, .reusable = true}}, SHIFT(10),
  [64] = {.entry = {.count = 1, .reusable = true}}, SHIFT(42),
  [66] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 1, 0, 0),
  [68] = {.entry = {.count = 1, .reusable = true}}, SHIFT(45),
  [70] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_fullCN, 1, 0, 2),
  [72] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0),
  [74] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0), SHIFT_REPEAT(10),
  [77] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__t, 1, 0, 0),
  [79] = {.entry = {.count = 1, .reusable = true}}, SHIFT(2),
  [81] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__t, 2, 0, 0),
  [83] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_mdf, 1, 0, 0),
  [85] = {.entry = {.count = 2, .reusable = false}}, REDUCE(aux_sym__px_repeat1, 2, 0, 0), SHIFT_REPEAT(30),
  [88] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_fullCN, 2, 0, 3),
  [90] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_mGen, 2, 0, 0),
  [92] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_mGen, 3, 0, 0),
  [94] = {.entry = {.count = 1, .reusable = false}}, REDUCE(aux_sym__px_repeat1, 2, 0, 0),
  [96] = {.entry = {.count = 2, .reusable = false}}, REDUCE(aux_sym__px_repeat1, 2, 0, 0), SHIFT_REPEAT(34),
  [99] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym__px, 1, 0, 0),
  [101] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__px, 1, 0, 0),
  [103] = {.entry = {.count = 1, .reusable = false}}, SHIFT(37),
  [105] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_mGen, 4, 0, 0),
  [107] = {.entry = {.count = 1, .reusable = false}}, REDUCE(sym__px, 2, 0, 0),
  [109] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__px, 2, 0, 0),
  [111] = {.entry = {.count = 1, .reusable = false}}, SHIFT(34),
  [113] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_alias, 6, 0, 0),
  [115] = {.entry = {.count = 1, .reusable = true}}, SHIFT(3),
  [117] = {.entry = {.count = 1, .reusable = true}}, SHIFT(33),
  [119] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__genDecl, 1, 0, 0),
  [121] = {.entry = {.count = 1, .reusable = true}}, SHIFT(4),
  [123] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 3, 0, 0),
  [125] = {.entry = {.count = 1, .reusable = true}}, SHIFT(51),
  [127] = {.entry = {.count = 1, .reusable = true}}, SHIFT(44),
  [129] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_alias, 5, 0, 0),
  [131] = {.entry = {.count = 1, .reusable = true}}, SHIFT(9),
  [133] = {.entry = {.count = 1, .reusable = true}}, SHIFT(36),
  [135] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_package, 3, 0, 1),
  [137] = {.entry = {.count = 1, .reusable = true}}, SHIFT(38),
  [139] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0),
  [141] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0), SHIFT_REPEAT(51),
  [144] = {.entry = {.count = 1, .reusable = true}}, SHIFT(52),
  [146] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_mGen_repeat1, 2, 0, 0), SHIFT_REPEAT(3),
  [149] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_mGen_repeat1, 2, 0, 0),
  [151] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_alias, 7, 0, 0),
  [153] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__genDecl, 3, 0, 0),
  [155] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__t, 3, 0, 0),
  [157] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__genDecl_repeat1, 2, 0, 0), SHIFT_REPEAT(5),
  [160] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__genDecl_repeat1, 2, 0, 0),
  [162] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__genDecl, 4, 0, 0),
  [164] = {.entry = {.count = 1, .reusable = false}}, SHIFT(39),
  [166] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_packagePath, 2, 0, 0),
  [168] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_packagePath, 1, 0, 0),
  [170] = {.entry = {.count = 1, .reusable = true}}, SHIFT(55),
  [172] = {.entry = {.count = 1, .reusable = true}},  ACCEPT_INPUT(),
  [174] = {.entry = {.count = 1, .reusable = true}}, SHIFT(12),
  [176] = {.entry = {.count = 1, .reusable = true}}, SHIFT(39),
  [178] = {.entry = {.count = 1, .reusable = true}}, SHIFT(49),
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
