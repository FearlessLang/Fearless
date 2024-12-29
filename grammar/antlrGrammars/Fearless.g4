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
ColonColon: '::';
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

//Note: defining both \ and \" or \` as valid options do not work.
//This would allow the parses to 'chose' between closing string or not.
//This have unpredictable results
fragment EscapeSequence: '\\' ('"' | '`' | '\\' | 'n' | 'r' | 't' | 'b' | 'f' | 'u{'('0'..'9')+'}' );//others? \u{..}?
//Note: some of those should only be for unicode
fragment CHARInString: 'A'..'Z'|'a'..'z'|'0'..'9' | '(' | ')' | '[' | ']' | '<' | '>' | '&' | '|' | '*' | '+' | '-' | '=' | '/' | '!' | '?' | ';' | ':' | ',' | '.' | ' ' | '~' | '@' | '#' | '$' | '%' | '^' | '_' | '{' | '}' | '\'';
fragment CHARInStringSingle: CHARInString | EscapeSequence | '"';
fragment CHARInStringDouble: CHARInString | EscapeSequence | '`';  
fragment FStringSingle: '`' CHARInStringSingle* '`';
fragment FStringDouble: '"'  CHARInStringDouble* '"';

//TODO: this need to instead allow unicode will look like [...]-" | '\\"'
fragment CHARInStringMulti://TODO: this need to instead allow unicode
'A'..'Z'|'a'..'z'|'0'..'9' | '(' | ')' | '[' | ']' | '<' | '>' | '&' | '|' | '*' | '+' | '-' | '=' | '/' | '!' | '?' | ';' | ':' | ',' | '.' | ' ' | '~' | '@' | '#' | '$' | '%' | '`' | '^' | '_' | '\\' | '{' | '}' | '"' | '\'';//no \n
fragment StringMultiLine:'|'+ ('`'|'"') CHARInStringMulti* '\n';

fragment Unders:   '_'*;
fragment NumSym:   '+'|'-'|'/'|'.';
fragment NumStart: Unders NumSym* '0'..'9';
fragment UpStart:  Unders NumSym* 'A'..'Z';
fragment LowStart: Unders 'a'..'z';
fragment Start:    UpStart | NumStart 
              |    FStringSingle | FStringDouble;
fragment IdUnit:   Start | LowStart;
fragment TypeName: Start IdUnit* '\''*;

fragment FIdLow:LowStart ('0'..'9'|'A'..'Z'|'a'..'z'|'_')*;
X:FIdLow ('\'')*;
SelfX:'\'' FIdLow;
MName: '.' FIdLow ('\'')*;
CCMName:'::'FIdLow ('\'')*;
FStringMulti: StringMultiLine+;

BlockComment: '/*' (BlockComment|.)*? '*/' -> channel(HIDDEN);
// nesting comments allowed
LineComment: '//' .*? ('\n'|EOF) -> channel(HIDDEN);

fragment SyInM:
'+' | '-' | '/' | '\\' | '|' | '!' | '@' | '#' | '$' | '%' | '^' | '&' | '?' | '~' | '<' | '>' | '=';//no ':', '*'
fragment SyInMExtra: ':'* SyInM; 
//  excluding = alone and excluding containing //, because they are defined first
SysInM: SyInMExtra* (SyInM|'*')+ | SyInMExtra+;
FullCN: (FIdLow '.')* TypeName;

Whitespace: ('\t' | ' ' | '\n' )-> channel(HIDDEN);

//GRAMMAR
fullCN:FullCN;
x: X| Underscore;
m: SysInM | MName;
mdf: Mut | ReadH | MutH | ReadImm | Read | Iso | Imm | ;

roundE : OR e CR;

genDecl : fullCN Colon mdf (Comma mdf)* | fullCN (Colon SysInM | );//generic declaration
//the code will check that SysInM is only either '**' or '*'

mGen   : | OS (genDecl (Comma genDecl)*)? CS;

actualGen   : | OS (t (Comma t)*)? CS;

topDec : fullCN mGen Colon (t (Comma t)* Comma?)? OC bblock CC;
lambda : mdf topDec | (t | mdf) OC bblock CC | t;

bblock :
       | SelfX? singleM
       | SelfX? (meth (Comma meth)*)? Comma?
       | (CCMName|SysInM) actualGen pOp*
       | (CCMName|SysInM) actualGen OR (e (Comma e)+)? CR pOp*
       | (CCMName|SysInM) actualGen atomE pOp*
       | (CCMName|SysInM) actualGen x Eq atomE pOp*       
       //parser will check that SysInM starts with ::
       | ColonColon;

t      : mdf fullCN actualGen;
//we recognize if fullCN is an X after parsing
singleM: (x (Comma x)*)? Arrow e | e;
meth   : sig | sig Arrow e | m OR (x (Comma x)*)? CR Arrow e | m (x (Comma x)*)? Arrow e;
sig    : mdf m mGen (OR gamma CR)? Colon t | mdf m mGen gamma Colon t;
gamma  : (x Colon t (Comma x Colon t)*)?;
alias  : Alias fullCN actualGen As fullCN Comma;
fStringMulti:FStringMulti;
atomE : x | roundE | lambda | fStringMulti;
e : atomE pOp*;
pOp : m actualGen 
    | m actualGen OR (e (Comma e)+)? CR
    | m actualGen atomE 
    | m actualGen x Eq atomE pOp*;

nudeE : e EOF;
nudeX : x EOF;
nudeM : m EOF;
nudeFullCN : fullCN EOF;
nudeT : t EOF;

Pack: 'package ' (FIdLow '.')* FIdLow '\n';
nudeProgram: Pack alias* topDec* EOF;