#include "tree_sitter/parser.h"

#if defined(__GNUC__) || defined(__clang__)
#pragma GCC diagnostic ignored "-Wmissing-field-initializers"
#endif

#define LANGUAGE_VERSION 14
#define STATE_COUNT 58
#define LARGE_STATE_COUNT 2
#define SYMBOL_COUNT 34
#define ALIAS_COUNT 0
#define TOKEN_COUNT 20
#define EXTERNAL_TOKEN_COUNT 0
#define FIELD_COUNT 6
#define MAX_ALIAS_SEQUENCE_LENGTH 8
#define PRODUCTION_ID_COUNT 9

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
  sym__noMdfConcreteType = 25,
  sym_concreteType = 26,
  sym_concreteTypes = 27,
  sym_mdf = 28,
  aux_sym_source_file_repeat1 = 29,
  aux_sym_source_file_repeat2 = 30,
  aux_sym__fullPkgName_repeat1 = 31,
  aux_sym_packagePath_repeat1 = 32,
  aux_sym_concreteTypes_repeat1 = 33,
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
  [sym__noMdfConcreteType] = "_noMdfConcreteType",
  [sym_concreteType] = "concreteType",
  [sym_concreteTypes] = "concreteTypes",
  [sym_mdf] = "mdf",
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
  [sym__noMdfConcreteType] = sym__noMdfConcreteType,
  [sym_concreteType] = sym_concreteType,
  [sym_concreteTypes] = sym_concreteTypes,
  [sym_mdf] = sym_mdf,
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
  [sym__noMdfConcreteType] = {
    .visible = false,
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
  [sym_mdf] = {
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
  field_mdf = 3,
  field_name = 4,
  field_package = 5,
  field_to = 6,
};

static const char * const ts_field_names[] = {
  [0] = NULL,
  [field_from] = "from",
  [field_generic] = "generic",
  [field_mdf] = "mdf",
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
  [6] = {.index = 9, .length = 3},
  [7] = {.index = 12, .length = 4},
  [8] = {.index = 16, .length = 5},
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
    {field_generic, 0, .inherited = true},
    {field_name, 0, .inherited = true},
    {field_package, 0, .inherited = true},
  [9] =
    {field_generic, 2},
    {field_name, 1},
    {field_package, 0},
  [12] =
    {field_generic, 1, .inherited = true},
    {field_mdf, 0},
    {field_name, 1, .inherited = true},
    {field_package, 1, .inherited = true},
  [16] =
    {field_from, 2},
    {field_generic, 2, .inherited = true},
    {field_name, 2, .inherited = true},
    {field_package, 2, .inherited = true},
    {field_to, 6},
};

static const TSSymbol ts_alias_sequences[PRODUCTION_ID_COUNT][MAX_ALIAS_SEQUENCE_LENGTH] = {
  [0] = {0},
  [8] = {
    [2] = sym_concreteType,
  },
};

static const uint16_t ts_non_terminal_alias_map[] = {
  sym__noMdfConcreteType, 2,
    sym__noMdfConcreteType,
    sym_concreteType,
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
  [13] = 13,
  [14] = 14,
  [15] = 15,
  [16] = 16,
  [17] = 17,
  [18] = 18,
  [19] = 19,
  [20] = 20,
  [21] = 14,
  [22] = 22,
  [23] = 23,
  [24] = 24,
  [25] = 19,
  [26] = 26,
  [27] = 13,
  [28] = 28,
  [29] = 20,
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
  [41] = 38,
  [42] = 42,
  [43] = 33,
  [44] = 44,
  [45] = 45,
  [46] = 46,
  [47] = 47,
  [48] = 48,
  [49] = 34,
  [50] = 36,
  [51] = 51,
  [52] = 35,
  [53] = 53,
  [54] = 46,
  [55] = 55,
  [56] = 56,
  [57] = 57,
};

static bool ts_lex(TSLexer *lexer, TSStateId state) {
  START_LEXER();
  eof = lexer->eof(lexer);
  switch (state) {
    case 0:
      if (eof) ADVANCE(35);
      ADVANCE_MAP(
        ',', 53,
        '.', 38,
        '[', 56,
        ']', 57,
        '_', 5,
        'a', 24,
        'i', 25,
        'm', 34,
        'p', 8,
        'r', 16,
        't', 29,
      );
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(0);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(55);
      END_STATE();
    case 1:
      if (lookahead == ' ') ADVANCE(37);
      if (lookahead == '[') ADVANCE(56);
      if (('\t' <= lookahead && lookahead <= '\r')) SKIP(1);
      END_STATE();
    case 2:
      if (lookahead == 'D') ADVANCE(18);
      END_STATE();
    case 3:
      if (lookahead == 'M') ADVANCE(15);
      END_STATE();
    case 4:
      if (lookahead == ']') ADVANCE(57);
      if (lookahead == '_') ADVANCE(7);
      if (lookahead == 'i') ADVANCE(45);
      if (lookahead == 'm') ADVANCE(49);
      if (lookahead == 'r') ADVANCE(43);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(4);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(55);
      END_STATE();
    case 5:
      if (lookahead == '_') ADVANCE(5);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(55);
      END_STATE();
    case 6:
      if (lookahead == '_') ADVANCE(7);
      if (('\t' <= lookahead && lookahead <= '\r') ||
          lookahead == ' ') SKIP(6);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(55);
      if (('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 7:
      if (lookahead == '_') ADVANCE(7);
      if (('A' <= lookahead && lookahead <= 'Z')) ADVANCE(55);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 8:
      if (lookahead == 'a') ADVANCE(12);
      END_STATE();
    case 9:
      if (lookahead == 'a') ADVANCE(14);
      if (lookahead == 'c') ADVANCE(3);
      END_STATE();
    case 10:
      if (lookahead == 'a') ADVANCE(32);
      END_STATE();
    case 11:
      if (lookahead == 'a') ADVANCE(20);
      END_STATE();
    case 12:
      if (lookahead == 'c') ADVANCE(23);
      END_STATE();
    case 13:
      if (lookahead == 'c') ADVANCE(73);
      END_STATE();
    case 14:
      if (lookahead == 'd') ADVANCE(65);
      END_STATE();
    case 15:
      if (lookahead == 'd') ADVANCE(19);
      END_STATE();
    case 16:
      if (lookahead == 'e') ADVANCE(9);
      END_STATE();
    case 17:
      if (lookahead == 'e') ADVANCE(36);
      END_STATE();
    case 18:
      if (lookahead == 'e') ADVANCE(13);
      END_STATE();
    case 19:
      if (lookahead == 'f') ADVANCE(69);
      END_STATE();
    case 20:
      if (lookahead == 'g') ADVANCE(17);
      END_STATE();
    case 21:
      if (lookahead == 'i') ADVANCE(10);
      END_STATE();
    case 22:
      if (lookahead == 'i') ADVANCE(28);
      END_STATE();
    case 23:
      if (lookahead == 'k') ADVANCE(11);
      END_STATE();
    case 24:
      if (lookahead == 'l') ADVANCE(21);
      if (lookahead == 's') ADVANCE(52);
      END_STATE();
    case 25:
      if (lookahead == 'm') ADVANCE(26);
      if (lookahead == 's') ADVANCE(30);
      END_STATE();
    case 26:
      if (lookahead == 'm') ADVANCE(71);
      END_STATE();
    case 27:
      if (lookahead == 'm') ADVANCE(64);
      END_STATE();
    case 28:
      if (lookahead == 'm') ADVANCE(27);
      END_STATE();
    case 29:
      if (lookahead == 'o') ADVANCE(31);
      END_STATE();
    case 30:
      if (lookahead == 'o') ADVANCE(67);
      END_STATE();
    case 31:
      if (lookahead == 'p') ADVANCE(2);
      END_STATE();
    case 32:
      if (lookahead == 's') ADVANCE(51);
      END_STATE();
    case 33:
      if (lookahead == 't') ADVANCE(58);
      END_STATE();
    case 34:
      if (lookahead == 'u') ADVANCE(33);
      END_STATE();
    case 35:
      ACCEPT_TOKEN(ts_builtin_sym_end);
      END_STATE();
    case 36:
      ACCEPT_TOKEN(anon_sym_package);
      END_STATE();
    case 37:
      ACCEPT_TOKEN(anon_sym_SPACE);
      if (lookahead == ' ') ADVANCE(37);
      END_STATE();
    case 38:
      ACCEPT_TOKEN(anon_sym_DOT);
      END_STATE();
    case 39:
      ACCEPT_TOKEN(sym__pkgName);
      if (lookahead == 'M') ADVANCE(42);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 40:
      ACCEPT_TOKEN(sym__pkgName);
      if (lookahead == 'a') ADVANCE(41);
      if (lookahead == 'c') ADVANCE(39);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('b' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 41:
      ACCEPT_TOKEN(sym__pkgName);
      if (lookahead == 'd') ADVANCE(66);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 42:
      ACCEPT_TOKEN(sym__pkgName);
      if (lookahead == 'd') ADVANCE(44);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 43:
      ACCEPT_TOKEN(sym__pkgName);
      if (lookahead == 'e') ADVANCE(40);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 44:
      ACCEPT_TOKEN(sym__pkgName);
      if (lookahead == 'f') ADVANCE(70);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 45:
      ACCEPT_TOKEN(sym__pkgName);
      if (lookahead == 'm') ADVANCE(46);
      if (lookahead == 's') ADVANCE(47);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 46:
      ACCEPT_TOKEN(sym__pkgName);
      if (lookahead == 'm') ADVANCE(72);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 47:
      ACCEPT_TOKEN(sym__pkgName);
      if (lookahead == 'o') ADVANCE(68);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 48:
      ACCEPT_TOKEN(sym__pkgName);
      if (lookahead == 't') ADVANCE(59);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 49:
      ACCEPT_TOKEN(sym__pkgName);
      if (lookahead == 'u') ADVANCE(48);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 50:
      ACCEPT_TOKEN(sym__pkgName);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 51:
      ACCEPT_TOKEN(anon_sym_alias);
      END_STATE();
    case 52:
      ACCEPT_TOKEN(anon_sym_as);
      END_STATE();
    case 53:
      ACCEPT_TOKEN(anon_sym_COMMA);
      END_STATE();
    case 54:
      ACCEPT_TOKEN(sym_typeName);
      if (lookahead == '\'') ADVANCE(54);
      END_STATE();
    case 55:
      ACCEPT_TOKEN(sym_typeName);
      if (lookahead == '\'') ADVANCE(54);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(55);
      END_STATE();
    case 56:
      ACCEPT_TOKEN(anon_sym_LBRACK);
      END_STATE();
    case 57:
      ACCEPT_TOKEN(anon_sym_RBRACK);
      END_STATE();
    case 58:
      ACCEPT_TOKEN(anon_sym_mut);
      if (lookahead == 'H') ADVANCE(62);
      END_STATE();
    case 59:
      ACCEPT_TOKEN(anon_sym_mut);
      if (lookahead == 'H') ADVANCE(63);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 60:
      ACCEPT_TOKEN(anon_sym_readH);
      END_STATE();
    case 61:
      ACCEPT_TOKEN(anon_sym_readH);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 62:
      ACCEPT_TOKEN(anon_sym_mutH);
      END_STATE();
    case 63:
      ACCEPT_TOKEN(anon_sym_mutH);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 64:
      ACCEPT_TOKEN(anon_sym_read_SLASHimm);
      END_STATE();
    case 65:
      ACCEPT_TOKEN(anon_sym_read);
      if (lookahead == '/') ADVANCE(22);
      if (lookahead == 'H') ADVANCE(60);
      END_STATE();
    case 66:
      ACCEPT_TOKEN(anon_sym_read);
      if (lookahead == '/') ADVANCE(22);
      if (lookahead == 'H') ADVANCE(61);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 67:
      ACCEPT_TOKEN(anon_sym_iso);
      END_STATE();
    case 68:
      ACCEPT_TOKEN(anon_sym_iso);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 69:
      ACCEPT_TOKEN(anon_sym_recMdf);
      END_STATE();
    case 70:
      ACCEPT_TOKEN(anon_sym_recMdf);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 71:
      ACCEPT_TOKEN(anon_sym_imm);
      END_STATE();
    case 72:
      ACCEPT_TOKEN(anon_sym_imm);
      if (('0' <= lookahead && lookahead <= '9') ||
          ('A' <= lookahead && lookahead <= 'Z') ||
          lookahead == '_' ||
          ('a' <= lookahead && lookahead <= 'z')) ADVANCE(50);
      END_STATE();
    case 73:
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
  [3] = {.lex_state = 4},
  [4] = {.lex_state = 4},
  [5] = {.lex_state = 0},
  [6] = {.lex_state = 0},
  [7] = {.lex_state = 0},
  [8] = {.lex_state = 6},
  [9] = {.lex_state = 0},
  [10] = {.lex_state = 6},
  [11] = {.lex_state = 0},
  [12] = {.lex_state = 0},
  [13] = {.lex_state = 0},
  [14] = {.lex_state = 0},
  [15] = {.lex_state = 0},
  [16] = {.lex_state = 6},
  [17] = {.lex_state = 0},
  [18] = {.lex_state = 0},
  [19] = {.lex_state = 0},
  [20] = {.lex_state = 0},
  [21] = {.lex_state = 1},
  [22] = {.lex_state = 0},
  [23] = {.lex_state = 0},
  [24] = {.lex_state = 6},
  [25] = {.lex_state = 0},
  [26] = {.lex_state = 0},
  [27] = {.lex_state = 1},
  [28] = {.lex_state = 0},
  [29] = {.lex_state = 0},
  [30] = {.lex_state = 0},
  [31] = {.lex_state = 6},
  [32] = {.lex_state = 0},
  [33] = {.lex_state = 0},
  [34] = {.lex_state = 0},
  [35] = {.lex_state = 0},
  [36] = {.lex_state = 0},
  [37] = {.lex_state = 6},
  [38] = {.lex_state = 0},
  [39] = {.lex_state = 6},
  [40] = {.lex_state = 0},
  [41] = {.lex_state = 1},
  [42] = {.lex_state = 0},
  [43] = {.lex_state = 1},
  [44] = {.lex_state = 1},
  [45] = {.lex_state = 0},
  [46] = {.lex_state = 0},
  [47] = {.lex_state = 1},
  [48] = {.lex_state = 1},
  [49] = {.lex_state = 1},
  [50] = {.lex_state = 1},
  [51] = {.lex_state = 1},
  [52] = {.lex_state = 1},
  [53] = {.lex_state = 0},
  [54] = {.lex_state = 0},
  [55] = {.lex_state = 0},
  [56] = {.lex_state = 0},
  [57] = {.lex_state = 6},
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
    [sym_source_file] = STATE(55),
    [sym_package] = STATE(5),
    [anon_sym_package] = ACTIONS(3),
  },
};

static const uint16_t ts_small_parse_table[] = {
  [0] = 10,
    ACTIONS(5), 1,
      sym__pkgName,
    ACTIONS(7), 1,
      sym_typeName,
    ACTIONS(9), 1,
      anon_sym_RBRACK,
    ACTIONS(13), 1,
      anon_sym_read_SLASHimm,
    STATE(8), 1,
      sym_mdf,
    STATE(20), 1,
      sym_concreteType,
    STATE(24), 1,
      aux_sym_packagePath_repeat1,
    STATE(32), 1,
      sym__noMdfConcreteType,
    STATE(54), 1,
      sym_packagePath,
    ACTIONS(11), 7,
      anon_sym_mut,
      anon_sym_readH,
      anon_sym_mutH,
      anon_sym_read,
      anon_sym_iso,
      anon_sym_recMdf,
      anon_sym_imm,
  [37] = 10,
    ACTIONS(5), 1,
      sym__pkgName,
    ACTIONS(7), 1,
      sym_typeName,
    ACTIONS(13), 1,
      anon_sym_read_SLASHimm,
    ACTIONS(15), 1,
      anon_sym_RBRACK,
    STATE(8), 1,
      sym_mdf,
    STATE(24), 1,
      aux_sym_packagePath_repeat1,
    STATE(29), 1,
      sym_concreteType,
    STATE(32), 1,
      sym__noMdfConcreteType,
    STATE(54), 1,
      sym_packagePath,
    ACTIONS(11), 7,
      anon_sym_mut,
      anon_sym_readH,
      anon_sym_mutH,
      anon_sym_read,
      anon_sym_iso,
      anon_sym_recMdf,
      anon_sym_imm,
  [74] = 9,
    ACTIONS(5), 1,
      sym__pkgName,
    ACTIONS(7), 1,
      sym_typeName,
    ACTIONS(13), 1,
      anon_sym_read_SLASHimm,
    STATE(8), 1,
      sym_mdf,
    STATE(24), 1,
      aux_sym_packagePath_repeat1,
    STATE(30), 1,
      sym_concreteType,
    STATE(32), 1,
      sym__noMdfConcreteType,
    STATE(54), 1,
      sym_packagePath,
    ACTIONS(11), 7,
      anon_sym_mut,
      anon_sym_readH,
      anon_sym_mutH,
      anon_sym_read,
      anon_sym_iso,
      anon_sym_recMdf,
      anon_sym_imm,
  [108] = 5,
    ACTIONS(17), 1,
      ts_builtin_sym_end,
    ACTIONS(19), 1,
      anon_sym_alias,
    ACTIONS(21), 1,
      sym_topDec,
    STATE(17), 1,
      aux_sym_source_file_repeat2,
    STATE(6), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [125] = 5,
    ACTIONS(19), 1,
      anon_sym_alias,
    ACTIONS(23), 1,
      ts_builtin_sym_end,
    ACTIONS(25), 1,
      sym_topDec,
    STATE(26), 1,
      aux_sym_source_file_repeat2,
    STATE(11), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [142] = 3,
    ACTIONS(29), 1,
      anon_sym_DOT,
    STATE(12), 1,
      aux_sym__fullPkgName_repeat1,
    ACTIONS(27), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [154] = 5,
    ACTIONS(7), 1,
      sym_typeName,
    ACTIONS(31), 1,
      sym__pkgName,
    STATE(24), 1,
      aux_sym_packagePath_repeat1,
    STATE(40), 1,
      sym__noMdfConcreteType,
    STATE(54), 1,
      sym_packagePath,
  [170] = 3,
    ACTIONS(29), 1,
      anon_sym_DOT,
    STATE(7), 1,
      aux_sym__fullPkgName_repeat1,
    ACTIONS(33), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [182] = 5,
    ACTIONS(31), 1,
      sym__pkgName,
    ACTIONS(35), 1,
      sym_typeName,
    STATE(24), 1,
      aux_sym_packagePath_repeat1,
    STATE(44), 1,
      sym__noMdfConcreteType,
    STATE(46), 1,
      sym_packagePath,
  [198] = 3,
    ACTIONS(39), 1,
      anon_sym_alias,
    ACTIONS(37), 2,
      ts_builtin_sym_end,
      sym_topDec,
    STATE(11), 2,
      sym_alias,
      aux_sym_source_file_repeat1,
  [210] = 3,
    ACTIONS(44), 1,
      anon_sym_DOT,
    STATE(12), 1,
      aux_sym__fullPkgName_repeat1,
    ACTIONS(42), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [222] = 3,
    ACTIONS(49), 1,
      anon_sym_LBRACK,
    STATE(35), 1,
      sym_concreteTypes,
    ACTIONS(47), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [233] = 3,
    ACTIONS(49), 1,
      anon_sym_LBRACK,
    STATE(38), 1,
      sym_concreteTypes,
    ACTIONS(51), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [244] = 1,
    ACTIONS(42), 4,
      ts_builtin_sym_end,
      anon_sym_DOT,
      anon_sym_alias,
      sym_topDec,
  [251] = 3,
    ACTIONS(53), 1,
      sym__pkgName,
    ACTIONS(56), 1,
      sym_typeName,
    STATE(16), 1,
      aux_sym_packagePath_repeat1,
  [261] = 3,
    ACTIONS(23), 1,
      ts_builtin_sym_end,
    ACTIONS(58), 1,
      sym_topDec,
    STATE(18), 1,
      aux_sym_source_file_repeat2,
  [271] = 3,
    ACTIONS(60), 1,
      ts_builtin_sym_end,
    ACTIONS(62), 1,
      sym_topDec,
    STATE(18), 1,
      aux_sym_source_file_repeat2,
  [281] = 3,
    ACTIONS(65), 1,
      anon_sym_COMMA,
    ACTIONS(67), 1,
      anon_sym_RBRACK,
    STATE(23), 1,
      aux_sym_concreteTypes_repeat1,
  [291] = 3,
    ACTIONS(65), 1,
      anon_sym_COMMA,
    ACTIONS(69), 1,
      anon_sym_RBRACK,
    STATE(19), 1,
      aux_sym_concreteTypes_repeat1,
  [301] = 3,
    ACTIONS(51), 1,
      anon_sym_SPACE,
    ACTIONS(71), 1,
      anon_sym_LBRACK,
    STATE(41), 1,
      sym_concreteTypes,
  [311] = 1,
    ACTIONS(73), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [317] = 3,
    ACTIONS(75), 1,
      anon_sym_COMMA,
    ACTIONS(78), 1,
      anon_sym_RBRACK,
    STATE(23), 1,
      aux_sym_concreteTypes_repeat1,
  [327] = 3,
    ACTIONS(31), 1,
      sym__pkgName,
    ACTIONS(80), 1,
      sym_typeName,
    STATE(16), 1,
      aux_sym_packagePath_repeat1,
  [337] = 3,
    ACTIONS(65), 1,
      anon_sym_COMMA,
    ACTIONS(82), 1,
      anon_sym_RBRACK,
    STATE(23), 1,
      aux_sym_concreteTypes_repeat1,
  [347] = 3,
    ACTIONS(58), 1,
      sym_topDec,
    ACTIONS(84), 1,
      ts_builtin_sym_end,
    STATE(18), 1,
      aux_sym_source_file_repeat2,
  [357] = 3,
    ACTIONS(47), 1,
      anon_sym_SPACE,
    ACTIONS(71), 1,
      anon_sym_LBRACK,
    STATE(52), 1,
      sym_concreteTypes,
  [367] = 1,
    ACTIONS(86), 3,
      ts_builtin_sym_end,
      anon_sym_alias,
      sym_topDec,
  [373] = 3,
    ACTIONS(65), 1,
      anon_sym_COMMA,
    ACTIONS(88), 1,
      anon_sym_RBRACK,
    STATE(25), 1,
      aux_sym_concreteTypes_repeat1,
  [383] = 1,
    ACTIONS(78), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [388] = 1,
    ACTIONS(90), 2,
      sym__pkgName,
      sym_typeName,
  [393] = 1,
    ACTIONS(92), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [398] = 1,
    ACTIONS(94), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [403] = 1,
    ACTIONS(96), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [408] = 1,
    ACTIONS(98), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [413] = 1,
    ACTIONS(100), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [418] = 2,
    ACTIONS(102), 1,
      sym__pkgName,
    STATE(28), 1,
      sym__fullPkgName,
  [425] = 1,
    ACTIONS(104), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [430] = 1,
    ACTIONS(56), 2,
      sym__pkgName,
      sym_typeName,
  [435] = 1,
    ACTIONS(106), 2,
      anon_sym_COMMA,
      anon_sym_RBRACK,
  [440] = 1,
    ACTIONS(104), 1,
      anon_sym_SPACE,
  [444] = 1,
    ACTIONS(108), 1,
      sym_typeName,
  [448] = 1,
    ACTIONS(94), 1,
      anon_sym_SPACE,
  [452] = 1,
    ACTIONS(110), 1,
      anon_sym_SPACE,
  [456] = 1,
    ACTIONS(112), 1,
      anon_sym_COMMA,
  [460] = 1,
    ACTIONS(114), 1,
      sym_typeName,
  [464] = 1,
    ACTIONS(116), 1,
      anon_sym_SPACE,
  [468] = 1,
    ACTIONS(118), 1,
      anon_sym_SPACE,
  [472] = 1,
    ACTIONS(96), 1,
      anon_sym_SPACE,
  [476] = 1,
    ACTIONS(100), 1,
      anon_sym_SPACE,
  [480] = 1,
    ACTIONS(120), 1,
      anon_sym_SPACE,
  [484] = 1,
    ACTIONS(98), 1,
      anon_sym_SPACE,
  [488] = 1,
    ACTIONS(122), 1,
      anon_sym_as,
  [492] = 1,
    ACTIONS(124), 1,
      sym_typeName,
  [496] = 1,
    ACTIONS(126), 1,
      ts_builtin_sym_end,
  [500] = 1,
    ACTIONS(128), 1,
      anon_sym_DOT,
  [504] = 1,
    ACTIONS(130), 1,
      sym__pkgName,
};

static const uint32_t ts_small_parse_table_map[] = {
  [SMALL_STATE(2)] = 0,
  [SMALL_STATE(3)] = 37,
  [SMALL_STATE(4)] = 74,
  [SMALL_STATE(5)] = 108,
  [SMALL_STATE(6)] = 125,
  [SMALL_STATE(7)] = 142,
  [SMALL_STATE(8)] = 154,
  [SMALL_STATE(9)] = 170,
  [SMALL_STATE(10)] = 182,
  [SMALL_STATE(11)] = 198,
  [SMALL_STATE(12)] = 210,
  [SMALL_STATE(13)] = 222,
  [SMALL_STATE(14)] = 233,
  [SMALL_STATE(15)] = 244,
  [SMALL_STATE(16)] = 251,
  [SMALL_STATE(17)] = 261,
  [SMALL_STATE(18)] = 271,
  [SMALL_STATE(19)] = 281,
  [SMALL_STATE(20)] = 291,
  [SMALL_STATE(21)] = 301,
  [SMALL_STATE(22)] = 311,
  [SMALL_STATE(23)] = 317,
  [SMALL_STATE(24)] = 327,
  [SMALL_STATE(25)] = 337,
  [SMALL_STATE(26)] = 347,
  [SMALL_STATE(27)] = 357,
  [SMALL_STATE(28)] = 367,
  [SMALL_STATE(29)] = 373,
  [SMALL_STATE(30)] = 383,
  [SMALL_STATE(31)] = 388,
  [SMALL_STATE(32)] = 393,
  [SMALL_STATE(33)] = 398,
  [SMALL_STATE(34)] = 403,
  [SMALL_STATE(35)] = 408,
  [SMALL_STATE(36)] = 413,
  [SMALL_STATE(37)] = 418,
  [SMALL_STATE(38)] = 425,
  [SMALL_STATE(39)] = 430,
  [SMALL_STATE(40)] = 435,
  [SMALL_STATE(41)] = 440,
  [SMALL_STATE(42)] = 444,
  [SMALL_STATE(43)] = 448,
  [SMALL_STATE(44)] = 452,
  [SMALL_STATE(45)] = 456,
  [SMALL_STATE(46)] = 460,
  [SMALL_STATE(47)] = 464,
  [SMALL_STATE(48)] = 468,
  [SMALL_STATE(49)] = 472,
  [SMALL_STATE(50)] = 476,
  [SMALL_STATE(51)] = 480,
  [SMALL_STATE(52)] = 484,
  [SMALL_STATE(53)] = 488,
  [SMALL_STATE(54)] = 492,
  [SMALL_STATE(55)] = 496,
  [SMALL_STATE(56)] = 500,
  [SMALL_STATE(57)] = 504,
};

static const TSParseActionEntry ts_parse_actions[] = {
  [0] = {.entry = {.count = 0, .reusable = false}},
  [1] = {.entry = {.count = 1, .reusable = false}}, RECOVER(),
  [3] = {.entry = {.count = 1, .reusable = true}}, SHIFT(48),
  [5] = {.entry = {.count = 1, .reusable = false}}, SHIFT(56),
  [7] = {.entry = {.count = 1, .reusable = true}}, SHIFT(14),
  [9] = {.entry = {.count = 1, .reusable = true}}, SHIFT(36),
  [11] = {.entry = {.count = 1, .reusable = false}}, SHIFT(31),
  [13] = {.entry = {.count = 1, .reusable = true}}, SHIFT(31),
  [15] = {.entry = {.count = 1, .reusable = true}}, SHIFT(50),
  [17] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 1, 0, 0),
  [19] = {.entry = {.count = 1, .reusable = true}}, SHIFT(47),
  [21] = {.entry = {.count = 1, .reusable = true}}, SHIFT(17),
  [23] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 2, 0, 0),
  [25] = {.entry = {.count = 1, .reusable = true}}, SHIFT(26),
  [27] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fullPkgName, 2, 0, 0),
  [29] = {.entry = {.count = 1, .reusable = true}}, SHIFT(57),
  [31] = {.entry = {.count = 1, .reusable = true}}, SHIFT(56),
  [33] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__fullPkgName, 1, 0, 0),
  [35] = {.entry = {.count = 1, .reusable = true}}, SHIFT(21),
  [37] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0),
  [39] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat1, 2, 0, 0), SHIFT_REPEAT(47),
  [42] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym__fullPkgName_repeat1, 2, 0, 0),
  [44] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym__fullPkgName_repeat1, 2, 0, 0), SHIFT_REPEAT(57),
  [47] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__noMdfConcreteType, 2, 0, 4),
  [49] = {.entry = {.count = 1, .reusable = true}}, SHIFT(2),
  [51] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__noMdfConcreteType, 1, 0, 2),
  [53] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_packagePath_repeat1, 2, 0, 0), SHIFT_REPEAT(56),
  [56] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_packagePath_repeat1, 2, 0, 0),
  [58] = {.entry = {.count = 1, .reusable = true}}, SHIFT(18),
  [60] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0),
  [62] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_source_file_repeat2, 2, 0, 0), SHIFT_REPEAT(18),
  [65] = {.entry = {.count = 1, .reusable = true}}, SHIFT(4),
  [67] = {.entry = {.count = 1, .reusable = true}}, SHIFT(33),
  [69] = {.entry = {.count = 1, .reusable = true}}, SHIFT(34),
  [71] = {.entry = {.count = 1, .reusable = false}}, SHIFT(3),
  [73] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_alias, 8, 0, 8),
  [75] = {.entry = {.count = 2, .reusable = true}}, REDUCE(aux_sym_concreteTypes_repeat1, 2, 0, 0), SHIFT_REPEAT(4),
  [78] = {.entry = {.count = 1, .reusable = true}}, REDUCE(aux_sym_concreteTypes_repeat1, 2, 0, 0),
  [80] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_packagePath, 1, 0, 0),
  [82] = {.entry = {.count = 1, .reusable = true}}, SHIFT(43),
  [84] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_source_file, 3, 0, 0),
  [86] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_package, 3, 0, 1),
  [88] = {.entry = {.count = 1, .reusable = true}}, SHIFT(49),
  [90] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_mdf, 1, 0, 0),
  [92] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteType, 1, 0, 5),
  [94] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteTypes, 4, 0, 0),
  [96] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteTypes, 3, 0, 0),
  [98] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__noMdfConcreteType, 3, 0, 6),
  [100] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteTypes, 2, 0, 0),
  [102] = {.entry = {.count = 1, .reusable = true}}, SHIFT(9),
  [104] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym__noMdfConcreteType, 2, 0, 3),
  [106] = {.entry = {.count = 1, .reusable = true}}, REDUCE(sym_concreteType, 2, 0, 7),
  [108] = {.entry = {.count = 1, .reusable = true}}, SHIFT(45),
  [110] = {.entry = {.count = 1, .reusable = true}}, SHIFT(53),
  [112] = {.entry = {.count = 1, .reusable = true}}, SHIFT(22),
  [114] = {.entry = {.count = 1, .reusable = true}}, SHIFT(27),
  [116] = {.entry = {.count = 1, .reusable = true}}, SHIFT(10),
  [118] = {.entry = {.count = 1, .reusable = true}}, SHIFT(37),
  [120] = {.entry = {.count = 1, .reusable = true}}, SHIFT(42),
  [122] = {.entry = {.count = 1, .reusable = true}}, SHIFT(51),
  [124] = {.entry = {.count = 1, .reusable = true}}, SHIFT(13),
  [126] = {.entry = {.count = 1, .reusable = true}},  ACCEPT_INPUT(),
  [128] = {.entry = {.count = 1, .reusable = true}}, SHIFT(39),
  [130] = {.entry = {.count = 1, .reusable = true}}, SHIFT(15),
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
