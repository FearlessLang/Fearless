grammar Fearless;
@header {package generated;}

//TOKENS
Mut:'mut';
Lent:'lent';
Read:'read';
Iso:'iso';
RecMdf:'recMdf';
Mdf:'mdf';
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

fragment IdUp: '_'* ('A'..'Z');
fragment IdLow: '_'* ('a'..'z') | '_'+ ('0'..'9');
fragment IdChar: 'a'..'z' | 'A'..'Z' | '_' | '0'..'9';
fragment CHAR:
'A'..'Z'|'a'..'z'|'0'..'9' | '(' | ')' | '[' | ']' | '<' | '>' | '&' | '|' | '*' | '+' | '-' | '=' | '/' | '!' | '?' | ';' | ':' | ',' | '.' | ' ' | '~' | '@' | '#' | '$' | '%' | '`' | '^' | '_' | '\\' | '{' | '}' | '"' | '\'' | '\n';
fragment CHARInStringSingle:
'A'..'Z'|'a'..'z'|'0'..'9' | '(' | ')' | '[' | ']' | '<' | '>' | '&' | '|' | '*' | '+' | '-' | '=' | '/' | '!' | '?' | ';' | ':' | ',' | '.' | ' ' | '~' | '@' | '#' | '$' | '%' | '`' | '^' | '_' | '\\' | '{' | '}' |       '\'';//no \n and " 
fragment CHARInStringMulti:
'A'..'Z'|'a'..'z'|'0'..'9' | '(' | ')' | '[' | ']' | '<' | '>' | '&' | '|' | '*' | '+' | '-' | '=' | '/' | '!' | '?' | ';' | ':' | ',' | '.' | ' ' | '~' | '@' | '#' | '$' | '%' | '`' | '^' | '_' | '\\' | '{' | '}' | '"' | '\'';//no \n

fragment StringMultiOpen:'"""' '\n';
fragment StringMultiClose:(' ')* '"""';
fragment StringMultiLine:(' ')* '|' CHARInStringMulti* '\n';
fragment FStringMulti: StringMultiOpen StringMultiLine+ StringMultiClose;
fragment FStringSingle: '"' CHARInStringSingle* '"';
fragment FNumber: '0'..'9' ('.'|'_'|'u'|'0'..'9')*; //flexible for more error messages
fragment FIdLow:IdLow IdChar* ('\'')*;
fragment FIdUp:IdUp IdChar* ('\'')*;
X:FIdLow;
MName: '.' FIdLow;

BlockComment: '/*' (BlockComment|.)*? '*/'	-> channel(HIDDEN) ; // nesting comments allowed
LineComment: '//' .*? ('\n'|EOF)				-> channel(HIDDEN) ;
fragment SyInM: '+' | '-' | '*' | '/' | '\\' | '|' | '!' | '@' | '#' | '$' | '%' | '^' | '&' | '?' | '~' | '<' | '>' | '=' | ':';
//  excluding = alone and excluding containing //, because they are defined first
SysInM:SyInM+;
fragment PX: IdLow IdChar*;
FullCN: (PX '.')* FIdUp | FStringMulti | FStringSingle | FNumber;

Whitespace: ( ' ' | '\n' )-> channel(HIDDEN);

//GRAMMAR
fullCN:FullCN;
x: X| Underscore;
m: SysInM | MName;
mdf: Mut | Lent | Read | Iso | RecMdf | Mdf | Imm | ;


roundE : OR e CR;
mGen   : | OS (t (Comma t)*)? CS;
lambda : mdf block;
block  : (t (Comma t)*)? OC bblock CC | t;
bblock : | OS x CS singleM | singleM | OS x CS (Comma meth)* Comma? | meth (Comma meth)* Comma?;

t      : mdf fullCN mGen; //we recognize if fullCN is an X after parsing
singleM: (x (Comma x)*)? Arrow e | e;
meth   : sig | sig Arrow e | m OR (x (Comma x)*)? CR Arrow e | m (x (Comma x)*)? Arrow e;
sig    : mdf m mGen (OR gamma CR)? Colon t | mdf m mGen gamma Colon t;
gamma  : (x Colon t (Comma x Colon t)*)?;
topDec : fullCN mGen Colon block;
alias  : Alias fullCN mGen As fullCN mGen Comma;

atomE : x | roundE | lambda;
postE : atomE pOp*;
pOp : m mGen | m mGen OR (e (Comma e)+)? CR | m mGen OR x Eq e CR; 
e: postE callOp*;
callOp: m mGen (x Eq)? postE;

nudeE : e EOF;

Pack: 'package ' (PX '.')* PX '\n';
nudeProgram: Pack alias* topDec* EOF;
