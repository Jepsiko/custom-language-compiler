import java.util.regex.PatternSyntaxException;

%%// Options of the scanner

%class LexicalAnalyzer	//Name
%unicode			//Use unicode
%line				//Use line counter (yyline variable)
%column			//Use character counter by line (yycolumn variable)
%function nextToken
%type Symbol
%yylexthrow PatternSyntaxException

%eofval{//code to execute after scanning
    return new Symbol(LexicalUnit.END_OF_STREAM, yyline, yycolumn);
%eofval}

////////////////////////////////
//Extended Regular Expressions//
////////////////////////////////

EndOfLine = "\r"?"\n"
Alpha = [a-z]|[A-Z]
Num = [0-9]
AlphaNum = {Alpha}|{Num}
VarName = {Alpha} {AlphaNum}*
Number = "-"?{Num}+
Space		= (\t | \f | " ")
Separator = ({Space}|{EndOfLine})
Any = ([^"\n""\r"])*
UpToEnd = ({Space}{Any}{EndOfLine}) | ({EndOfLine})

//////////
//States//
//////////

%xstate YYINITIAL, LONG_COMMENT

%%//Identification of tokens and actions

<YYINITIAL>{
   "co"{UpToEnd}      {}
   "CO"{Separator}    {yybegin(LONG_COMMENT);}

   "/="          {return new Symbol(LexicalUnit.DIFFERENT, yyline, yycolumn, yytext());}
   "="           {return new Symbol(LexicalUnit.EQUAL, yyline, yycolumn, yytext());}
   ">="          {return new Symbol(LexicalUnit.GREATER_EQUAL, yyline, yycolumn, yytext());}
   "<="          {return new Symbol(LexicalUnit.SMALLER_EQUAL, yyline, yycolumn, yytext());}
   ">"           {return new Symbol(LexicalUnit.GREATER, yyline, yycolumn, yytext());}
   "<"           {return new Symbol(LexicalUnit.SMALLER, yyline, yycolumn, yytext());}

   ";"           {return new Symbol(LexicalUnit.SEMICOLON, yyline, yycolumn, yytext());}
   ":="          {return new Symbol(LexicalUnit.ASSIGN, yyline, yycolumn, yytext());}
   "("           {return new Symbol(LexicalUnit.LEFT_PARENTHESIS, yyline, yycolumn, yytext());}
   ")"           {return new Symbol(LexicalUnit.RIGHT_PARENTHESIS, yyline, yycolumn, yytext());}

   {Number}      {return new Symbol(LexicalUnit.NUMBER, yyline, yycolumn, new Integer(yytext()));}

   // Minus after Number because the minus alone is included in Number.
   // "-" is interpreted as a minus only if it is not directly followed by digits.
   "-"           {return new Symbol(LexicalUnit.MINUS, yyline, yycolumn, yytext());}
   "+"           {return new Symbol(LexicalUnit.PLUS, yyline, yycolumn, yytext());}
   "*"           {return new Symbol(LexicalUnit.TIMES, yyline, yycolumn, yytext());}
   "/"           {return new Symbol(LexicalUnit.DIVIDE, yyline, yycolumn, yytext());}

   "begin"       {return new Symbol(LexicalUnit.BEG, yyline, yycolumn, yytext());}
   "end"         {return new Symbol(LexicalUnit.END, yyline, yycolumn, yytext());}
   "if"          {return new Symbol(LexicalUnit.IF, yyline, yycolumn, yytext());}
   "then"        {return new Symbol(LexicalUnit.THEN, yyline, yycolumn, yytext());}
   "endif"       {return new Symbol(LexicalUnit.ENDIF, yyline, yycolumn, yytext());}
   "else"        {return new Symbol(LexicalUnit.ELSE, yyline, yycolumn, yytext());}
   "not"         {return new Symbol(LexicalUnit.NOT, yyline, yycolumn, yytext());}
   "and"         {return new Symbol(LexicalUnit.AND, yyline, yycolumn, yytext());}
   "or"          {return new Symbol(LexicalUnit.OR, yyline, yycolumn, yytext());}
   "while"       {return new Symbol(LexicalUnit.WHILE, yyline, yycolumn, yytext());}
   "do"          {return new Symbol(LexicalUnit.DO, yyline, yycolumn, yytext());}
   "endwhile"    {return new Symbol(LexicalUnit.ENDWHILE, yyline, yycolumn, yytext());}
   "for"         {return new Symbol(LexicalUnit.FOR, yyline, yycolumn, yytext());}
   "from"        {return new Symbol(LexicalUnit.FROM, yyline, yycolumn, yytext());}
   "by"          {return new Symbol(LexicalUnit.BY, yyline, yycolumn, yytext());}
   "to"          {return new Symbol(LexicalUnit.TO, yyline, yycolumn, yytext());}
   "print"       {return new Symbol(LexicalUnit.PRINT, yyline, yycolumn, yytext());}
   "read"        {return new Symbol(LexicalUnit.READ, yyline, yycolumn, yytext());}

   {VarName}     {return new Symbol(LexicalUnit.VARNAME, yyline, yycolumn, yytext());}

   {Separator}  {}
   [^]			 {throw new PatternSyntaxException("Unmatched token, out of symbols", yytext(), yyline);}	//unmatched token gives an error
}

<LONG_COMMENT>{
   [^]			        {}
   "CO"{Separator}     {yybegin(YYINITIAL);}
   <<EOF>>              {throw new PatternSyntaxException("A comment is never closed.",yytext(),yyline);}
}