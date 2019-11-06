//import java_cup.runtime.*; uncomment if you use CUP

%%// Options of the scanner

%class Lexer	//Name
%unicode			//Use unicode
%line				//Use line counter (yyline variable)
%column			//Use character counter by line (yycolumn variable)

//you can use either %cup or %standalone
//   %standalone is for a Scanner which works alone and scan a file
//   %cup is to interact with a CUP parser. In this case, you have to return
//        a Symbol object (defined in the CUP library) for each action.
//        Two constructors:
//                          1. Symbol(int id,int line, int column)
//                          2. Symbol(int id,int line, int column,Object value)
%standalone

////////
//CODE//
////////
%init{//code to execute before scanning
%init}

%{//adding Java code (methods, inner classes, ...)
    java.util.List<Symbol> identifiers = new java.util.ArrayList<Symbol>();

    void sortIdentifiers() { // Bubble sort algorithm
        boolean sorted = false;
        Symbol temp;
        while(!sorted) {
            sorted = true;
            for (int i = 0; i < identifiers.size()-1; i++) {
                String varname1 = (String)identifiers.get(i).getValue();
                String varname2 = (String)identifiers.get(i+1).getValue();
                if (varname1.compareTo(varname2) > 0) {
                    temp = identifiers.get(i);
                    identifiers.set(i, identifiers.get(i+1));
                    identifiers.set(i+1, temp);
                    sorted = false;
                }
            }
        }
    }

    boolean alreadyInIdentifiers(String varname) {
        for (Symbol symbol : identifiers) {
            if (((String)symbol.getValue()).compareTo(varname) == 0) {
                return true;
            }
        }
        return false;
    }

    void analyze(LexicalUnit type, String token) {
        Symbol symbol = new Symbol(type, yyline, yycolumn, token);
        System.out.println(symbol);

        if (symbol.getType() == LexicalUnit.VARNAME && !alreadyInIdentifiers(token)) {
             identifiers.add(symbol);
        }
    }
%}

%eof{//code to execute after scanning
    System.out.println("\nIdentifiers");

    sortIdentifiers();
    for (Symbol symbol : identifiers) {
        System.out.println((String)symbol.getValue() + "\t" + symbol.getLine());
    }
%eof}

////////////////////////////////
//Extended Regular Expressions//
////////////////////////////////

EndOfLine = "\r"?"\n"
Alpha = [a-z]|[A-Z]
Num = [0-9]
AlphaNum = {Alpha}|{Num}
VarName = {Alpha} {AlphaNum}*
Number = "-"?{Num}+
Separation = (" "|{EndOfLine})

//////////
//States//
//////////

%xstate YYINITIAL, SHORT_COMMENT, LONG_COMMENT, LONG_COMMENT_EMPTY

%%//Identification of tokens and actions

<YYINITIAL>{
   "co "               {yybegin(SHORT_COMMENT);}
   "CO"{Separation}    {yybegin(LONG_COMMENT_EMPTY);}

   "/="          {analyze(LexicalUnit.DIFFERENT, yytext());}
   "="           {analyze(LexicalUnit.EQUAL, yytext());}
   ">="          {analyze(LexicalUnit.GREATER_EQUAL, yytext());}
   "<="          {analyze(LexicalUnit.SMALLER_EQUAL, yytext());}
   ">"           {analyze(LexicalUnit.GREATER, yytext());}
   "<"           {analyze(LexicalUnit.SMALLER, yytext());}

   ";"           {analyze(LexicalUnit.SEMICOLON, yytext());}
   ":="          {analyze(LexicalUnit.ASSIGN, yytext());}
   "("           {analyze(LexicalUnit.LEFT_PARENTHESIS, yytext());}
   ")"           {analyze(LexicalUnit.RIGHT_PARENTHESIS, yytext());}

   {Number}      {analyze(LexicalUnit.NUMBER, yytext());}

   // Minus after Number because the minus alone is included in Number.
   // "-" is interpreted as a minus only if it is not directly followed by digits.
   "-"           {analyze(LexicalUnit.MINUS, yytext());}
   "+"           {analyze(LexicalUnit.PLUS, yytext());}
   "*"           {analyze(LexicalUnit.TIMES, yytext());}
   "/"           {analyze(LexicalUnit.DIVIDE, yytext());}

   "begin"       {analyze(LexicalUnit.BEG, yytext());}
   "end"         {analyze(LexicalUnit.END, yytext());}
   "if"          {analyze(LexicalUnit.IF, yytext());}
   "then"        {analyze(LexicalUnit.THEN, yytext());}
   "endif"       {analyze(LexicalUnit.ENDIF, yytext());}
   "else"        {analyze(LexicalUnit.ELSE, yytext());}
   "not"         {analyze(LexicalUnit.NOT, yytext());}
   "and"         {analyze(LexicalUnit.AND, yytext());}
   "or"          {analyze(LexicalUnit.OR, yytext());}
   "while"       {analyze(LexicalUnit.WHILE, yytext());}
   "do"          {analyze(LexicalUnit.DO, yytext());}
   "endwhile"    {analyze(LexicalUnit.ENDWHILE, yytext());}
   "for"         {analyze(LexicalUnit.FOR, yytext());}
   "from"        {analyze(LexicalUnit.FROM, yytext());}
   "by"          {analyze(LexicalUnit.BY, yytext());}
   "to"          {analyze(LexicalUnit.TO, yytext());}
   "print"       {analyze(LexicalUnit.PRINT, yytext());}
   "read"        {analyze(LexicalUnit.READ, yytext());}

   {VarName}     {analyze(LexicalUnit.VARNAME, yytext());}

   {EndOfLine}   {}
   .             {}
}

<SHORT_COMMENT>{
   {EndOfLine}   {yybegin(YYINITIAL);}
   .             {}
}

<LONG_COMMENT>{
   {Separation}"CO"{Separation} {yybegin(YYINITIAL);}
   {EndOfLine}                  {}
   .                            {}
}

// If we have those empty comments :
// CO CO
//
// CO
// CO
//
// The separators " " or "\n" are read in the YYINITIAL state.
// So it is unnecessary to have a separation before CO in this state
// because we only need one separator between two "CO" in an empty comment.
<LONG_COMMENT_EMPTY>{
   "CO"{Separation}             {yybegin(YYINITIAL);}
   {EndOfLine}                  {}
   .                            {yybegin(LONG_COMMENT);}
}