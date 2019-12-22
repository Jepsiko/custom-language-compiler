import java.util.ArrayList;
import java.util.List;

/**
 * A recursive descent parser for the compiler.
 * Each variable has a function associated which calls the other ones according to the action table.
 * If the rule contains terminals, each one of them will me matched.
 */
class Parser {
    /**
     * List of symbols for the input
     */
    private final java.util.List<Symbol> symbols;
    /**
     * Verbose boolean
     */
    private final boolean v;
    /**
     * Lookahead pointer
     */
    private int l;
    /**
     * Syntax Error boolean
     */
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

    /**
     * Parse the symbols and return the parse tree if there is no syntax error.
     *
     * @return the parse tree created during the parsing
     */
    ParseTree parse() {
        ParseTree parseTree = Program();

        if (syntaxError)
            return null;
        else
            return parseTree;
    }

    /**
     * Function representing the variable Program.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
    private ParseTree Program() {
        List<ParseTree> children = new ArrayList<>();

        // [1] <Program> -> begin <Code> end
        if (lookahead() == LexicalUnit.BEG) {
            print(1, "<Program> -> begin <Code> end");

            children.add(match(LexicalUnit.BEG));
            children.add(Code());
            children.add(match(LexicalUnit.END));
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Program>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable Code.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
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
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Code>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable InstList.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
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
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<InstList>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable NextInst.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
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
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<NextInst>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable Instruction.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
    private ParseTree Instruction() {
        List<ParseTree> children = new ArrayList<>();

        // [7] <Instruction> -> <Assign>
        if (lookahead() == LexicalUnit.VARNAME) {
            print(7, "<Instruction> -> <Assign>");

            children.add(Assign());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Instruction>"), children);
        }
        // [8] <Instruction> -> <If>
        else if (lookahead() == LexicalUnit.IF) {
            print(8, "<Instruction> -> <If>");

            children.add(If());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Instruction>"), children);
        }
        // [9] <Instruction> -> <While>
        else if (lookahead() == LexicalUnit.WHILE) {
            print(9, "<Instruction> -> <While>");

            children.add(While());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Instruction>"), children);
        }
        // [10] <Instruction> -> <For>
        else if (lookahead() == LexicalUnit.FOR) {
            print(10, "<Instruction> -> <For>");

            children.add(For());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Instruction>"), children);
        }
        // [11] <Instruction> -> <Print>
        else if (lookahead() == LexicalUnit.PRINT) {
            print(11, "<Instruction> -> <Print>");

            children.add(Print());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Instruction>"), children);
        }
        // [12] <Instruction> -> <Read>
        else if (lookahead() == LexicalUnit.READ) {
            print(12, "<Instruction> -> <Read>");

            children.add(Read());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Instruction>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable Assign.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
    private ParseTree Assign() {
        List<ParseTree> children = new ArrayList<>();

        // [13] <Assign> -> [VarName] := <ExprArith>
        if (lookahead() == LexicalUnit.VARNAME) {
            print(13, "<Assign> -> [VarName] := <ExprArith>");

            children.add(match(LexicalUnit.VARNAME));
            children.add(match(LexicalUnit.ASSIGN));
            children.add(ExprArith());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Assign>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable ExprArith.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
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
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<ExprArith>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable ExprArith'.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
    private ParseTree ExprArith_prime() {
        List<ParseTree> children = new ArrayList<>();

        // [15] <ExprArith'> -> + <Prod> <ExprArith'>
        if (lookahead() == LexicalUnit.PLUS) {
            print(15, "<ExprArith'> -> + <Prod> <ExprArith'>");

            children.add(match(LexicalUnit.PLUS));
            children.add(Prod());
            children.add(ExprArith_prime());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<ExprArith'>"), children);
        }
        // [16] <ExprArith'> -> - <Prod> <ExprArith'>
        else if (lookahead() == LexicalUnit.MINUS) {
            print(16, "<ExprArith'> -> - <Prod> <ExprArith'>");

            children.add(match(LexicalUnit.MINUS));
            children.add(Prod());
            children.add(ExprArith_prime());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<ExprArith'>"), children);
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

    /**
     * Function representing the variable Prod.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
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
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Prod>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable Prod'.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
    private ParseTree Prod_prime() {
        List<ParseTree> children = new ArrayList<>();

        // [19] <Prod'> -> * <Atom> <Prod'>
        if (lookahead() == LexicalUnit.TIMES) {
            print(19, "<Prod'> -> * <Atom> <Prod'>");

            children.add(match(LexicalUnit.TIMES));
            children.add(Atom());
            children.add(Prod_prime());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Prod'>"), children);
        }
        // [20] <Prod'> -> / <Atom> <Prod'>
        else if (lookahead() == LexicalUnit.DIVIDE) {
            print(20, "<Prod'> -> / <Atom> <Prod'>");

            children.add(match(LexicalUnit.DIVIDE));
            children.add(Atom());
            children.add(Prod_prime());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Prod'>"), children);
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

    /**
     * Function representing the variable Atom.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
    private ParseTree Atom() {
        List<ParseTree> children = new ArrayList<>();

        // [22] <Atom> -> - <Atom>
        if (lookahead() == LexicalUnit.MINUS) {
            print(22, "<Atom> -> - <Atom>");

            children.add(match(LexicalUnit.MINUS));
            children.add(Atom());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Atom>"), children);
        }
        // [23] <Atom> -> [Number]
        else if (lookahead() == LexicalUnit.NUMBER) {
            print(23, "<Atom> -> [Number]");

            children.add(match(LexicalUnit.NUMBER));
            return new ParseTree(new Symbol(null, "<Atom>"), children);
        }
        // [24] <Atom> -> [VarName]
        else if (lookahead() == LexicalUnit.VARNAME) {
            print(24, "<Atom> -> [VarName]");

            children.add(match(LexicalUnit.VARNAME));
            return new ParseTree(new Symbol(null, "<Atom>"), children);
        }
        // [25] <Atom> -> ( <ExprArith> )
        else if (lookahead() == LexicalUnit.LEFT_PARENTHESIS) {
            print(25, "<Atom> -> ( <ExprArith> )");

            children.add(match(LexicalUnit.LEFT_PARENTHESIS));
            children.add(ExprArith());
            children.add(match(LexicalUnit.RIGHT_PARENTHESIS));
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Atom>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable If.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
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
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<If>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable IfSeq.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
    private ParseTree IfSeq() {
        List<ParseTree> children = new ArrayList<>();

        // [27] <IfSeq> -> endif
        if (lookahead() == LexicalUnit.ENDIF) {
            print(27, "<IfSeq> -> endif");

            children.add(match(LexicalUnit.ENDIF));
            return new ParseTree(new Symbol(null, "<IfSeq>"), children);
        }
        // [28] <IfSeq> -> else <Code> endif
        else if (lookahead() == LexicalUnit.ELSE) {
            print(28, "<IfSeq> -> else <Code> endif");

            children.add(match(LexicalUnit.ELSE));
            children.add(Code());
            children.add(match(LexicalUnit.ENDIF));
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<IfSeq>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable Cond.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
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
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Cond>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable Cond'.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
    private ParseTree Cond_prime() {
        List<ParseTree> children = new ArrayList<>();

        // [30] <Cond'> -> or <CondAnd> <Cond'>
        if (lookahead() == LexicalUnit.OR) {
            print(30, "<Cond'> -> or <CondAnd> <Cond'>");

            children.add(match(LexicalUnit.OR));
            children.add(CondAnd());
            children.add(Cond_prime());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<Cond'>"), children);
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

    /**
     * Function representing the variable CondAnd.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
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
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<CondAnd>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable CondAnd'.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
    private ParseTree CondAnd_prime() {
        List<ParseTree> children = new ArrayList<>();

        // [33] <CondAnd'> -> and <SimpleCond> <CondAnd'>
        if (lookahead() == LexicalUnit.AND) {
            print(33, "<CondAnd'> -> and <SimpleCond> <CondAnd'>");

            children.add(match(LexicalUnit.AND));
            children.add(SimpleCond());
            children.add(CondAnd_prime());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<CondAnd'>"), children);
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

    /**
     * Function representing the variable SimpleCond.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
    private ParseTree SimpleCond() {
        List<ParseTree> children = new ArrayList<>();

        // [35] <SimpleCond> -> <ExprArith> <SimpleCond'>
        if (
                lookahead() == LexicalUnit.VARNAME ||
                lookahead() == LexicalUnit.NUMBER ||
                lookahead() == LexicalUnit.MINUS)
        {
            print(35, "<SimpleCond> -> <ExprArith> <SimpleCond'>");

            children.add(ExprArith());
            children.add(SimpleCond_prime());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<SimpleCond>"), children);
        }
        // [36] <SimpleCond> -> not <SimpleCond>
        else if (lookahead() == LexicalUnit.NOT) {
            print(36, "<SimpleCond> -> not <SimpleCond>");

            children.add(match(LexicalUnit.NOT));
            children.add(SimpleCond());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<SimpleCond>"), children);
        }
        // [47] <SimpleCond> -> (<Cond>)
        else if (lookahead() == LexicalUnit.LEFT_PARENTHESIS) {
            print(47, "<SimpleCond> -> (<Cond>)");

            children.add(match(LexicalUnit.LEFT_PARENTHESIS));
            children.add(Cond());
            children.add(match(LexicalUnit.RIGHT_PARENTHESIS));
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<SimpleCond>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable SimpleCond'.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
    private ParseTree SimpleCond_prime() {
        List<ParseTree> children = new ArrayList<>();

        // [48] <SimpleCond'> -> < <ExprArith> <SimpleCond'>
        if (lookahead() == LexicalUnit.SMALLER) {
            print(48, "<SimpleCond'> -> < <ExprArith> <SimpleCond'>");

            children.add(match(LexicalUnit.SMALLER));
            children.add(ExprArith());
            children.add(SimpleCond_prime());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<SimpleCond'>"), children);
        }
        // [49] <SimpleCond'> -> <= <ExprArith> <SimpleCond'>
        else if (lookahead() == LexicalUnit.SMALLER_EQUAL) {
            print(49, "<SimpleCond'> -> <= <ExprArith> <SimpleCond'>");

            children.add(match(LexicalUnit.SMALLER_EQUAL));
            children.add(ExprArith());
            children.add(SimpleCond_prime());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<SimpleCond'>"), children);
        }
        // [50] <SimpleCond'> -> = <ExprArith> <SimpleCond'>
        else if (lookahead() == LexicalUnit.EQUAL) {
            print(50, "<SimpleCond'> -> = <ExprArith> <SimpleCond'>");

            children.add(match(LexicalUnit.EQUAL));
            children.add(ExprArith());
            children.add(SimpleCond_prime());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<SimpleCond'>"), children);
        }
        // [51] <SimpleCond'> -> > <ExprArith> <SimpleCond'>
        else if (lookahead() == LexicalUnit.GREATER) {
            print(51, "<SimpleCond'> -> > <ExprArith> <SimpleCond'>");

            children.add(match(LexicalUnit.GREATER));
            children.add(ExprArith());
            children.add(SimpleCond_prime());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<SimpleCond'>"), children);
        }
        // [52] <SimpleCond'> -> >= <ExprArith> <SimpleCond'>
        else if (lookahead() == LexicalUnit.GREATER_EQUAL) {
            print(52, "<SimpleCond'> -> >= <ExprArith> <SimpleCond'>");

            children.add(match(LexicalUnit.GREATER_EQUAL));
            children.add(ExprArith());
            children.add(SimpleCond_prime());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<SimpleCond'>"), children);
        }
        // [53] <SimpleCond'> -> /= <ExprArith> <SimpleCond'>
        else if (lookahead() == LexicalUnit.DIFFERENT) {
            print(53, "<SimpleCond'> -> >= <ExprArith> <SimpleCond'>");

            children.add(match(LexicalUnit.DIFFERENT));
            children.add(ExprArith());
            children.add(SimpleCond_prime());
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<SimpleCond'>"), children);
        }
        // [54] <SimpleCond'> -> ε
        else if (
                lookahead() == LexicalUnit.THEN ||
                lookahead() == LexicalUnit.DO)
        {
            print(54, "<SimpleCond'> -> ε");
            return null;
        }

        return null;
    }

    /**
     * Function representing the variable While.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
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
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<While>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable For.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
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
            while (children.remove(null));
            return new ParseTree(new Symbol(null, "<For>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable Print.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
    private ParseTree Print() {
        List<ParseTree> children = new ArrayList<>();

        // [45] <Print> -> print([VarName])
        if (lookahead() == LexicalUnit.PRINT) {
            print(45, "<Print> -> print([VarName])");

            children.add(match(LexicalUnit.PRINT));
            children.add(match(LexicalUnit.LEFT_PARENTHESIS));
            children.add(match(LexicalUnit.VARNAME));
            children.add(match(LexicalUnit.RIGHT_PARENTHESIS));
            return new ParseTree(new Symbol(null, "<Print>"), children);
        }

        return null;
    }

    /**
     * Function representing the variable Read.
     *
     * According to the action table, the function will execute a
     * certain rule if the lookahead is corresponding.
     *
     * If the rule contains terminals, there will be a match and
     * the lookahead will be incremented.
     *
     * Otherwise, the function corresponding to the variable is called.
     *
     * Each match return a leaf of the tree. Each function return
     * a recursive tree. Those are stored as children of the tree returned
     * in this function.
     *
     * @return a recursive ParseTree
     */
    private ParseTree Read() {
        List<ParseTree> children = new ArrayList<>();

        // [46] <Read> -> read([VarName])
        if (lookahead() == LexicalUnit.READ) {
            print(46, "<Read> -> read([VarName])");

            children.add(match(LexicalUnit.READ));
            children.add(match(LexicalUnit.LEFT_PARENTHESIS));
            children.add(match(LexicalUnit.VARNAME));
            children.add(match(LexicalUnit.RIGHT_PARENTHESIS));
            return new ParseTree(new Symbol(null, "<Read>"), children);
        }

        return null;
    }


    /**
     * Match the input with the lexical unit type. If the match is succesfull,
     * the lookahead is incremented and becomes the next terminal of the input
     *
     * @param type lexical unit to be matched
     * @return a leaf of a ParseTree with the matched terminal
     */
    private ParseTree match(LexicalUnit type) {
        Symbol terminal = symbols.get(l);
        if (terminal.getType() == type) {
            if (v) {
                System.out.println("Match : " + terminal);
            }
            l++;

            return new ParseTree(terminal);
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


    /**
     * @return return the symbol pointed by the lookahead 'l'
     */
    private LexicalUnit lookahead() {
        return symbols.get(l).getType();
    }

    /**
     * Display a rule. (only the rule number if not verbose)
     *
     * @param number rule number
     * @param rule rule description
     */
    private void print(int number, String rule) {
        if (v)
            System.out.println("[" + number + "] " + rule);
//        else
//            System.out.print(number + " ");
    }
}
