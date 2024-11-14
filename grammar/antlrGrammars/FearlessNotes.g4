grammar Fearless;
@header {package generated;}

//TOKENS
Mut:'mut';
MutH:'mutH';
Read:'read';
ReadImm:'read/imm';
ReadH:'readH';
Iso:'iso';
Imm:'imm';
Eq:'=';
Alias: 'alias';
As: 'as';

OC:'{';
CC:'}';

OS:'[';
CS:']';

OR:'(';
CR:')';

Comma:',';
Colon:':';
Arrow:'->';

Underscore:'_';

fragment CHAR:
'A'..'Z'|'a'..'z'|'0'..'9' | '(' | ')' | '[' | ']' | '<' | '>' | '&' | '|' | '*' | '+' | '-' | '=' | '/' | '!' | '?' | ';' | ':' | ',' | '.' | ' ' | '~' | '@' | '#' | '$' | '%' | '`' | '^' | '_' | '\\' | '{' | '}' | '"' | '\'' | '\n';

fragment CHARInStringSingle:
'A'..'Z'|'a'..'z'|'0'..'9' | '(' | ')' | '[' | ']' | '<' | '>' | '&' | '|' | '*' | '+' | '-' | '=' | '/' | '!' | '?' | ';' | ':' | ',' | '.' | ' ' | '~' | '@' | '#' | '$' | '%' | '^' | '_' | '\\' | '{' | '}' | '"'|'\'';//no \n and ` by itself
fragment CHARInStringDouble://TODO: this need to instead allow unicode
'A'..'Z'|'a'..'z'|'0'..'9' | '(' | ')' | '[' | ']' | '<' | '>' | '&' | '|' | '*' | '+' | '-' | '=' | '/' | '!' | '?' | ';' | ':' | ',' | '.' | ' ' | '~' | '@' | '#' | '$' | '%' | '`' | '^' | '_' | '\\' | '{' | '}' | '\\"'|'\'';//no \n and " by itself

fragment CHARInStringSingleMulti://TODO: this need to instead allow unicode
'A'..'Z'|'a'..'z'|'0'..'9' | '(' | ')' | '[' | ']' | '<' | '>' | '&' | '|' | '*' | '+' | '-' | '=' | '/' | '!' | '?' | ';' | ':' | ',' | '.' | ' ' | '~' | '@' | '#' | '$' | '%' | '`' | '^' | '_' | '\\' | '{' | '}' | '"' | '\'';//no \n

fragment CHARInStringSingleRawTerm:
'A'..'Z'|'a'..'z'|'0'..'9' | '(' | ')' | '[' | ']' | '<' | '>' | '&' | '|' | '*' | '+' | '-' | '=' | '/' | '!' | '?' | ';' | ':' | ',' | '.' | ' ' | '~' | '@' | '$' | '%' | '`' | '^' | '_' | '\\' | '{' | '}' | '"' | '\'';//no # \n

fragment CHARInStringSingleRaw1:
  CHARInStringSingleRaw0 | '\'#';
fragment CHARInStringSingleRaw2:
  CHARInStringSingleRaw1 | '\'##';
fragment CHARInStringSingleRaw3:
  CHARInStringSingleRaw2 | '\'###';
fragment CHARInStringSingleRaw4:
  CHARInStringSingleRaw3 | '\'####';
//May expand more

fragment StringMultiLine:'#'+ ('`'|'"') CHARInStringMulti* '\n';
fragment FStringMulti: StringMultiLine+;
fragment FStringSingle: '`' CHARInStringSingle* '`';
fragment FStringDouble: '"'  CHARInStringDouble* '"';

fragment Unders:   '_'*;
fragment NumSym:   '+'|'-'|'/'|'.';
fragment NumStart: Unders NumSym? '0'..'9';
fragment UpStart:  Unders ('A'..'Z');
fragment LowStart: Unders ('a'..'z');
fragment Start:    UpStart | NumStart 
              |    FStringSingle | FStringDouble;
fragment IdUnit:   Start | LowStart;
fragment TypeName: Start IdUnit* '\''*;

fragment FIdLow:LowStart ('0'..'9'|'A'..'Z''a'..'z'|'_')*;
X:FIdLow ('\'')*;
SelfX:'\'' FIdLow;
MName: '.' FIdLow;
CCMName:'::'FIdLow;

BlockComment: '/*' (BlockComment|.)*? '*/' -> channel(HIDDEN);
// nesting comments allowed
LineComment: '//' .*? ('\n'|EOF) -> channel(HIDDEN);
fragment SyInM:
'+' | '-' | '*' | '/' | '\\' | '|' | '!' | '@' | '#' | '$' | '%' | '^' | '&' | '?' | '~' | '<' | '>' | '=';//no ':'
fragment SyInMExtra:
'+' | '-' | '*' | '/' | '\\' | '|' | '!' | '@' | '#' | '$' | '%' | '^' | '&' | '?' | '~' | '<' | '>' | '=' | ':';
//  excluding = alone and excluding containing //, because they are defined first
SysInM: SyInMExtra* SyInM;
FullCN: (FIdLow '.')* TypeName;

Whitespace: ('\t' | ' ' | '\n' )-> channel(HIDDEN);

//GRAMMAR
fullCN:FullCN;
x: X| Underscore;
m: SysInM | MName;
mdf: Mut | ReadH | MutH | ReadImm | Read | Iso | Imm | ;

roundE : OR e CR;
genDecl : t Colon mdf (Comma mdf)* | t;//generic declaration
mGen   : | OS (genDecl (Comma genDecl)*)? CS;
lambda : mdf topDec | mdf block;
block  : (t (Comma t)*)? OC bblock CC | t;
bblock :
       | SelfX? singleM  
       | SelfX? (meth ((Comma|'\n') meth)*)? Comma?
       | (CCMName|SysInM) pOp*
       //parser will check that SysInM starts with ::
       | '::';
t      : mdf fullCN mGen;
//we recognize if fullCN is an X after parsing
singleM: (x (Comma x)*)? Arrow e | e;
meth   : sig | sig Arrow e | m OR (x (Comma x)*)? CR Arrow e | m (x (Comma x)*)? Arrow e;
sig    : mdf m mGen (OR gamma CR)? Colon t | mdf m mGen gamma Colon t;
gamma  : (x Colon t (Comma x Colon t)*)?;
topDec : fullCN mGen Colon block;
alias  : Alias fullCN mGen As fullCN mGen Comma;

atomE : x | roundE | lambda;
postE : atomE pOp*;
pOp : m mGen | m mGen OR (e (Comma e)+)? CR | m mGen OR x Eq e CR callOp*;
e: postE callOp*;
callOp: m mGen (x Eq)? postE;

nudeE : e EOF;
nudeX : x EOF;
nudeM : m EOF;
nudeFullCN : fullCN EOF;
nudeT : t EOF;

Pack: 'package ' (FIdLow '.')* FIdLow '\n';
nudeProgram: Pack alias* topDec* EOF;