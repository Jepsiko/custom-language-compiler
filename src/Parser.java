import java.util.ArrayList;
import java.util.List;

/**
 *
 */
class Parser {
    private final java.util.List<Symbol> symbols;
    private final boolean v;

    private int l;
    private boolean syntaxError;

    Parser(java.util.List<Symbol> symbols) {
        this(symbols, false);
    }

    Parser(java.util.List<Symbol> symbols, boolean verbose) {
        this.symbols = symbols;
        this.v = verbose;
        this.l = 0;
        this.syntaxError = false;
    }

    ParseTree parse() {
        ParseTree parseTree = Program();
        if (!v)
            System.out.println();

        if (syntaxError)
            return null;
        else
            return parseTree;
    }

    private ParseTree Program() {
        List<ParseTree> children = new ArrayList<>();

        // [1] <Program> -> begin <Code> end
        if (lookahead() == LexicalUnit.BEG) {
            print(1, "<Program> -> begin <Code> end");

            children.add(match(LexicalUnit.BEG));
            children.add(Code());
            children.add(match(LexicalUnit.END));
            return new ParseTree("<Program>", children);
        }

        return null;
    }

    private ParseTree Code() {
        List<ParseTree> children = new ArrayList<>();

        // [2] <Code> -> ε
        if (
                lookahead() == LexicalUnit.END ||
                lookahead() == LexicalUnit.ENDIF ||
                lookahead() == LexicalUnit.ELSE ||
                lookahead() == LexicalUnit.ENDWHILE)
        {
            print(2, "<Code> -> ε");
            return null;
        }
        // [3] <Code> -> <InstList>
        else if (
                lookahead() == LexicalUnit.VARNAME ||
                lookahead() == LexicalUnit.IF ||
                lookahead() == LexicalUnit.WHILE ||
                lookahead() == LexicalUnit.FOR ||
                lookahead() == LexicalUnit.PRINT ||
                lookahead() == LexicalUnit.READ)
        {
            print(3, "<Code> -> <InstList>");

            children.add(InstList());
            return new ParseTree("<Code>", children);
        }

        return null;
    }

    private ParseTree InstList() {
        List<ParseTree> children = new ArrayList<>();

        // [4] <InstList> -> <Instruction> <NextInst>
        if (
                lookahead() == LexicalUnit.VARNAME ||
                lookahead() == LexicalUnit.IF ||
                lookahead() == LexicalUnit.WHILE ||
                lookahead() == LexicalUnit.FOR ||
                lookahead() == LexicalUnit.PRINT ||
                lookahead() == LexicalUnit.READ)
        {
            print(4, "<InstList> -> <Instruction> <NextInst>");

            children.add(Instruction());
            children.add(NextInst());
            return new ParseTree("<InstList>", children);
        }

        return null;
    }

    private ParseTree NextInst() {
        List<ParseTree> children = new ArrayList<>();

        // [5] <NextInst> -> ε
        if (
                lookahead() == LexicalUnit.END ||
                lookahead() == LexicalUnit.ENDIF ||
                lookahead() == LexicalUnit.ELSE ||
                lookahead() == LexicalUnit.ENDWHILE)
        {
            print(5, "<NextInst> -> ε");
            return null;
        }
        // [6] <NextInst> -> ; <InstList>
        else if (lookahead() == LexicalUnit.SEMICOLON) {
            print(6, "<NextInst> -> ; <InstList>");

            children.add(match(LexicalUnit.SEMICOLON));
            children.add(InstList());
            return new ParseTree("<NextInst>", children);
        }

        return null;
    }

    private ParseTree Instruction() {
        List<ParseTree> children = new ArrayList<>();

        // [7] <Instruction> -> <Assign>
        if (lookahead() == LexicalUnit.VARNAME) {
            print(7, "<Instruction> -> <Assign>");

            children.add(Assign());
            return new ParseTree("<Instruction>", children);
        }
        // [8] <Instruction> -> <If>
        else if (lookahead() == LexicalUnit.IF) {
            print(8, "<Instruction> -> <If>");

            children.add(If());
            return new ParseTree("<Instruction>", children);
        }
        // [9] <Instruction> -> <While>
        else if (lookahead() == LexicalUnit.WHILE) {
            print(9, "<Instruction> -> <While>");

            children.add(While());
            return new ParseTree("<Instruction>", children);
        }
        // [10] <Instruction> -> <For>
        else if (lookahead() == LexicalUnit.FOR) {
            print(10, "<Instruction> -> <For>");

            children.add(For());
            return new ParseTree("<Instruction>", children);
        }
        // [11] <Instruction> -> <Print>
        else if (lookahead() == LexicalUnit.PRINT) {
            print(11, "<Instruction> -> <Print>");

            children.add(Print());
            return new ParseTree("<Instruction>", children);
        }
        // [12] <Instruction> -> <Read>
        else if (lookahead() == LexicalUnit.READ) {
            print(12, "<Instruction> -> <Read>");

            children.add(Read());
            return new ParseTree("<Instruction>", children);
        }

        return null;
    }

    private ParseTree Assign() {
        List<ParseTree> children = new ArrayList<>();

        // [13] <Assign> -> [VarName] := <ExprArith>
        if (lookahead() == LexicalUnit.VARNAME) {
            print(13, "<Assign> -> [VarName] := <ExprArith>");

            children.add(match(LexicalUnit.VARNAME));
            children.add(match(LexicalUnit.ASSIGN));
            children.add(ExprArith());
            return new ParseTree("<Assign>", children);
        }

        return null;
    }

    private ParseTree ExprArith() {
        List<ParseTree> children = new ArrayList<>();

        // [14] <ExprArith> -> <Prod> <ExprArith'>
        if (
                lookahead() == LexicalUnit.VARNAME ||
                lookahead() == LexicalUnit.NUMBER ||
                lookahead() == LexicalUnit.MINUS ||
                lookahead() == LexicalUnit.LEFT_PARENTHESIS)
        {
            print(14, "<ExprArith> -> <Prod> <ExprArith'>");

            children.add(Prod());
            children.add(ExprArith_prime());
            return new ParseTree("<ExprArith>", children);
        }

        return null;
    }

    private ParseTree ExprArith_prime() {
        List<ParseTree> children = new ArrayList<>();

        // [15] <ExprArith'> -> + <Prod> <ExprArith'>
        if (lookahead() == LexicalUnit.PLUS) {
            print(15, "<ExprArith'> -> + <Prod> <ExprArith'>");

            children.add(match(LexicalUnit.PLUS));
            children.add(Prod());
            children.add(ExprArith_prime());
            return new ParseTree("<ExprArith'>", children);
        }
        // [16] <ExprArith'> -> - <Prod> <ExprArith'>
        else if (lookahead() == LexicalUnit.MINUS) {
            print(16, "<ExprArith'> -> - <Prod> <ExprArith'>");

            children.add(match(LexicalUnit.MINUS));
            children.add(Prod());
            children.add(ExprArith_prime());
            return new ParseTree("<ExprArith'>", children);
        }
        // [17] <ExprArith'> -> ε
        else if (
                lookahead() == LexicalUnit.END ||
                lookahead() == LexicalUnit.SEMICOLON ||
                lookahead() == LexicalUnit.THEN ||
                lookahead() == LexicalUnit.ENDIF ||
                lookahead() == LexicalUnit.ELSE ||
                lookahead() == LexicalUnit.OR ||
                lookahead() == LexicalUnit.AND ||
                lookahead() == LexicalUnit.EQUAL ||
                lookahead() == LexicalUnit.GREATER_EQUAL ||
                lookahead() == LexicalUnit.GREATER ||
                lookahead() == LexicalUnit.SMALLER_EQUAL ||
                lookahead() == LexicalUnit.SMALLER ||
                lookahead() == LexicalUnit.DIFFERENT ||
                lookahead() == LexicalUnit.DO ||
                lookahead() == LexicalUnit.ENDWHILE ||
                lookahead() == LexicalUnit.BY ||
                lookahead() == LexicalUnit.TO)
        {
            print(17, "<ExprArith'> -> ε");
            return null;
        }

        return null;
    }

    private ParseTree Prod() {
        List<ParseTree> children = new ArrayList<>();

        // [18] <Prod> -> <Atom> <Prod'>
        if (
                lookahead() == LexicalUnit.VARNAME ||
                lookahead() == LexicalUnit.NUMBER ||
                lookahead() == LexicalUnit.MINUS ||
                lookahead() == LexicalUnit.LEFT_PARENTHESIS)
        {
            print(18, "<Prod> -> <Atom> <Prod'>");

            children.add(Atom());
            children.add(Prod_prime());
            return new ParseTree("<Prod>", children);
        }

        return null;
    }

    private ParseTree Prod_prime() {
        List<ParseTree> children = new ArrayList<>();

        // [19] <Prod'> -> * <Atom> <Prod'>
        if (lookahead() == LexicalUnit.TIMES) {
            print(19, "<Prod'> -> * <Atom> <Prod'>");

            children.add(match(LexicalUnit.TIMES));
            children.add(Atom());
            children.add(Prod_prime());
            return new ParseTree("<Prod'>", children);
        }
        // [20] <Prod'> -> / <Atom> <Prod'>
        else if (lookahead() == LexicalUnit.DIVIDE) {
            print(20, "<Prod'> -> / <Atom> <Prod'>");

            children.add(match(LexicalUnit.DIVIDE));
            children.add(Atom());
            children.add(Prod_prime());
            return new ParseTree("<Prod'>", children);
        }
        // [21] <Prod'> -> ε
        else if (
                lookahead() == LexicalUnit.END ||
                lookahead() == LexicalUnit.SEMICOLON ||
                lookahead() == LexicalUnit.PLUS ||
                lookahead() == LexicalUnit.MINUS ||
                lookahead() == LexicalUnit.THEN ||
                lookahead() == LexicalUnit.ENDIF ||
                lookahead() == LexicalUnit.ELSE ||
                lookahead() == LexicalUnit.OR ||
                lookahead() == LexicalUnit.AND ||
                lookahead() == LexicalUnit.EQUAL ||
                lookahead() == LexicalUnit.GREATER_EQUAL ||
                lookahead() == LexicalUnit.GREATER ||
                lookahead() == LexicalUnit.SMALLER_EQUAL ||
                lookahead() == LexicalUnit.SMALLER ||
                lookahead() == LexicalUnit.DIFFERENT ||
                lookahead() == LexicalUnit.DO ||
                lookahead() == LexicalUnit.ENDWHILE ||
                lookahead() == LexicalUnit.BY ||
                lookahead() == LexicalUnit.TO)
        {
            print(21, "<Prod'> -> ε");
            return null;
        }

        return null;
    }

    private ParseTree Atom() {
        List<ParseTree> children = new ArrayList<>();

        // [22] <Atom> -> - <Atom>
        if (lookahead() == LexicalUnit.MINUS) {
            print(22, "<Atom> -> - <Atom>");

            children.add(match(LexicalUnit.MINUS));
            children.add(Atom());
            return new ParseTree("<Atom>", children);
        }
        // [23] <Atom> -> [Number]
        else if (lookahead() == LexicalUnit.NUMBER) {
            print(23, "<Atom> -> [Number]");

            children.add(match(LexicalUnit.NUMBER));
            return new ParseTree("<Atom>", children);
        }
        // [24] <Atom> -> [VarName]
        else if (lookahead() == LexicalUnit.VARNAME) {
            print(24, "<Atom> -> [VarName]");

            children.add(match(LexicalUnit.VARNAME));
            return new ParseTree("<Atom>", children);
        }
        // [25] <Atom> -> ( <ExprArith> )
        else if (lookahead() == LexicalUnit.LEFT_PARENTHESIS) {
            print(25, "<Atom> -> ( <ExprArith> )");

            children.add(match(LexicalUnit.LEFT_PARENTHESIS));
            children.add(ExprArith());
            children.add(match(LexicalUnit.RIGHT_PARENTHESIS));
            return new ParseTree("<Atom>", children);
        }

        return null;
    }

    private ParseTree If() {
        List<ParseTree> children = new ArrayList<>();

        // [26] <If> -> if <Cond> then <Code> <IfSeq>
        if (lookahead() == LexicalUnit.IF) {
            print(26, "<If> -> if <Cond> then <Code> <IfSeq>");

            children.add(match(LexicalUnit.IF));
            children.add(Cond());
            children.add(match(LexicalUnit.THEN));
            children.add(Code());
            children.add(IfSeq());
            return new ParseTree("<If>", children);
        }

        return null;
    }

    private ParseTree IfSeq() {
        List<ParseTree> children = new ArrayList<>();

        // [27] <IfSeq> -> endif
        if (lookahead() == LexicalUnit.ENDIF) {
            print(27, "<IfSeq> -> endif");

            children.add(match(LexicalUnit.ENDIF));
            return new ParseTree("<IfSeq>", children);
        }
        // [28] <IfSeq> -> else <Code> endif
        else if (lookahead() == LexicalUnit.ELSE) {
            print(28, "<IfSeq> -> else <Code> endif");

            children.add(match(LexicalUnit.ELSE));
            children.add(Code());
            children.add(match(LexicalUnit.ENDIF));
            return new ParseTree("<IfSeq>", children);
        }

        return null;
    }

    private ParseTree Cond() {
        List<ParseTree> children = new ArrayList<>();

        // [29] <Cond> -> <CondAnd> <Cond'>
        if (
                lookahead() == LexicalUnit.VARNAME ||
                lookahead() == LexicalUnit.NUMBER ||
                lookahead() == LexicalUnit.MINUS ||
                lookahead() == LexicalUnit.LEFT_PARENTHESIS ||
                lookahead() == LexicalUnit.NOT)
        {
            print(29, "<Cond> -> <CondAnd> <Cond'>");

            children.add(CondAnd());
            children.add(Cond_prime());
            return new ParseTree("<Cond>", children);
        }

        return null;
    }

    private ParseTree Cond_prime() {
        List<ParseTree> children = new ArrayList<>();

        // [30] <Cond'> -> or <CondAnd> <Cond'>
        if (lookahead() == LexicalUnit.OR) {
            print(30, "<Cond'> -> or <CondAnd> <Cond'>");

            children.add(match(LexicalUnit.OR));
            children.add(CondAnd());
            children.add(Cond_prime());
            return new ParseTree("<Cond'>", children);
        }
        // [31] <Cond'> -> ε
        else if (
                lookahead() == LexicalUnit.THEN ||
                lookahead() == LexicalUnit.DO)
        {
            print(31, "<Cond'> -> ε");
            return null;
        }

        return null;
    }

    private ParseTree CondAnd() {
        List<ParseTree> children = new ArrayList<>();

        // [32] <CondAnd> -> <SimpleCond> <CondAnd'>
        if (
                lookahead() == LexicalUnit.VARNAME ||
                lookahead() == LexicalUnit.NUMBER ||
                lookahead() == LexicalUnit.MINUS ||
                lookahead() == LexicalUnit.LEFT_PARENTHESIS ||
                lookahead() == LexicalUnit.NOT)
        {
            print(32, "<CondAnd> -> <SimpleCond> <CondAnd'>");

            children.add(SimpleCond());
            children.add(CondAnd_prime());
            return new ParseTree("<CondAnd>", children);
        }

        return null;
    }

    private ParseTree CondAnd_prime() {
        List<ParseTree> children = new ArrayList<>();

        // [33] <CondAnd'> -> and <SimpleCond> <CondAnd'>
        if (lookahead() == LexicalUnit.AND) {
            print(33, "<CondAnd'> -> and <SimpleCond> <CondAnd'>");

            children.add(match(LexicalUnit.AND));
            children.add(SimpleCond());
            children.add(CondAnd_prime());
            return new ParseTree("<CondAnd'>", children);
        }
        // [34] <CondAnd'> -> ε
        else if (
                lookahead() == LexicalUnit.THEN ||
                lookahead() == LexicalUnit.DO ||
                lookahead() == LexicalUnit.OR)
        {
            print(34, "<CondAnd'> -> ε");
            return null;
        }

        return null;
    }

    private ParseTree SimpleCond() {
        List<ParseTree> children = new ArrayList<>();

        // [35] <SimpleCond> -> <ExprArith> <Comp> <ExprArith>
        if (
                lookahead() == LexicalUnit.VARNAME ||
                lookahead() == LexicalUnit.NUMBER ||
                lookahead() == LexicalUnit.MINUS ||
                lookahead() == LexicalUnit.LEFT_PARENTHESIS)
        {
            print(35, "<SimpleCond> -> <ExprArith> <Comp> <ExprArith>");

            children.add(ExprArith());
            children.add(Comp());
            children.add(ExprArith());
            return new ParseTree("<SimpleCond>", children);
        }
        // [36] <SimpleCond> -> not <SimpleCond>
        else if (lookahead() == LexicalUnit.NOT) {
            print(36, "<SimpleCond> -> not <SimpleCond>");

            children.add(match(LexicalUnit.NOT));
            children.add(SimpleCond());
            return new ParseTree("<SimpleCond>", children);
        }

        return null;
    }

    private ParseTree Comp() {
        List<ParseTree> children = new ArrayList<>();

        // [37] <Comp> -> =
        if (lookahead() == LexicalUnit.EQUAL) {
            print(37, "<Comp> -> =");

            children.add(match(LexicalUnit.EQUAL));
            return new ParseTree("<Comp>", children);
        }
        // [38] <Comp> -> >=
        else if (lookahead() == LexicalUnit.GREATER_EQUAL) {
            print(38, "<Comp> -> >=");

            children.add(match(LexicalUnit.GREATER_EQUAL));
            return new ParseTree("<Comp>", children);
        }
        // [39] <Comp> -> >
        else if (lookahead() == LexicalUnit.GREATER) {
            print(39, "<Comp> -> >");

            children.add(match(LexicalUnit.GREATER));
            return new ParseTree("<Comp>", children);
        }
        // [40] <Comp> -> <=
        else if (lookahead() == LexicalUnit.SMALLER_EQUAL) {
            print(40, "<Comp> -> <=");

            children.add(match(LexicalUnit.SMALLER_EQUAL));
            return new ParseTree("<Comp>", children);
        }
        // [41] <Comp> -> <
        else if (lookahead() == LexicalUnit.SMALLER) {
            print(41, "<Comp> -> <");

            children.add(match(LexicalUnit.SMALLER));
            return new ParseTree("<Comp>", children);
        }
        // [42] <Comp> -> /=
        else if (lookahead() == LexicalUnit.DIFFERENT) {
            print(42, "<Comp> -> /=");

            children.add(match(LexicalUnit.DIFFERENT));
            return new ParseTree("<Comp>", children);
        }

        return null;
    }

    private ParseTree While() {
        List<ParseTree> children = new ArrayList<>();

        // [43] <While> -> while <Cond> do <Code> endwhile
        if (lookahead() == LexicalUnit.WHILE) {
            print(43, "<While> -> while <Cond> do <Code> endwhile");

            children.add(match(LexicalUnit.WHILE));
            children.add(Cond());
            children.add(match(LexicalUnit.DO));
            children.add(Code());
            children.add(match(LexicalUnit.ENDWHILE));
            return new ParseTree("<While>", children);
        }

        return null;
    }

    private ParseTree For() {
        List<ParseTree> children = new ArrayList<>();

        // [44] <For> -> for [VarName] from <ExprArith> by <ExprArith> to <ExprArith> do <Code> endwhile
        if (lookahead() == LexicalUnit.FOR) {
            print(44, "<For> -> for [VarName] from <ExprArith> by <ExprArith> to " +
                    "<ExprArith> do <Code> endwhile");

            children.add(match(LexicalUnit.FOR));
            children.add(match(LexicalUnit.VARNAME));
            children.add(match(LexicalUnit.FROM));
            children.add(ExprArith());
            children.add(match(LexicalUnit.BY));
            children.add(ExprArith());
            children.add(match(LexicalUnit.TO));
            children.add(ExprArith());
            children.add(match(LexicalUnit.DO));
            children.add(Code());
            children.add(match(LexicalUnit.ENDWHILE));
            return new ParseTree("<For>", children);
        }

        return null;
    }

    private ParseTree Print() {
        List<ParseTree> children = new ArrayList<>();

        // [45] <Print> -> print([VarName])
        if (lookahead() == LexicalUnit.PRINT) {
            print(45, "<Print> -> print([VarName])");

            children.add(match(LexicalUnit.PRINT));
            children.add(match(LexicalUnit.LEFT_PARENTHESIS));
            children.add(match(LexicalUnit.VARNAME));
            children.add(match(LexicalUnit.RIGHT_PARENTHESIS));
            return new ParseTree("<Print>", children);
        }

        return null;
    }

    private ParseTree Read() {
        List<ParseTree> children = new ArrayList<>();

        // [46] <Read> -> read([VarName])
        if (lookahead() == LexicalUnit.READ) {
            print(46, "<Read> -> read([VarName])");

            children.add(match(LexicalUnit.READ));
            children.add(match(LexicalUnit.LEFT_PARENTHESIS));
            children.add(match(LexicalUnit.VARNAME));
            children.add(match(LexicalUnit.RIGHT_PARENTHESIS));
            return new ParseTree("<Read>", children);
        }

        return null;
    }

    private ParseTree match(LexicalUnit type)
    {
        Symbol terminal = symbols.get(l);
        if (terminal.getType() == type) {
            if (v) {
                System.out.println("Match : " + terminal);
            }
            l++;

            return new ParseTree(terminal.getValue().toString());
        }
        else if (!syntaxError) {
            if (!v) {
                System.out.println();
            }
            System.out.println("Syntax error at line " + terminal.getLine());
            syntaxError = true;
        }

        return null;
    }

    private LexicalUnit lookahead() {
        return symbols.get(l).getType();
    }

    private void print(int number, String rule) {
        if (v)
            System.out.println("[" + number + "] " + rule);
        else
            System.out.print(number + " ");
    }
}
