public class Parser {
    private final java.util.List<Symbol> symbols;
    private final boolean v;

    private int l;

    public Parser(java.util.List<Symbol> symbols, boolean verbose) {
        this.symbols = symbols;
        this.v = verbose;
        this.l = 0;
    }

    public Parser(java.util.List<Symbol> symbols) { this(symbols, true); }

    public void parse() {
        Program();
        System.out.println();

        if (l == symbols.size()) {
            System.out.println("Parsing Successful");
        }
    }

    private void Program() {
        // [1] <Program> -> begin <Code> end
        if (lookahead() == LexicalUnit.BEG) {
            print(1, "<Program> -> begin <Code> end");
            match(LexicalUnit.BEG);
            Code();
            match(LexicalUnit.END);
        }
    }

    private void Code() {
        // [2] <Code> -> ε
        if (
                lookahead() == LexicalUnit.END ||
                lookahead() == LexicalUnit.ENDIF ||
                lookahead() == LexicalUnit.ELSE ||
                lookahead() == LexicalUnit.ENDWHILE)
        {
            print(2, "<Code> -> ε");
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

            InstList();
        }
        else {
            System.out.println("Error Code");
        }
    }

    private void InstList() {
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

            Instruction();
            NextInst();
        }
        else {
            System.out.println("Error InstList");
        }
    }

    private void NextInst() {
        // [5] <NextInst> -> ε
        if (
                lookahead() == LexicalUnit.END ||
                lookahead() == LexicalUnit.ENDIF ||
                lookahead() == LexicalUnit.ELSE ||
                lookahead() == LexicalUnit.ENDWHILE)
        {
            print(5, "<NextInst> -> ε");
        }
        // [6] <NextInst> -> ; <InstList>
        else if (lookahead() == LexicalUnit.SEMICOLON) {
            print(6, "<NextInst> -> ; <InstList>");

            match(LexicalUnit.SEMICOLON);
            InstList();
        }
        else {
            System.out.println("Error NextInst");
        }
    }

    private void Instruction() {
        // [7] <Instruction> -> <Assign>
        if (lookahead() == LexicalUnit.VARNAME) {
            print(7, "<Instruction> -> <Assign>");

            Assign();
        }
        // [8] <Instruction> -> <If>
        else if (lookahead() == LexicalUnit.IF) {
            print(8, "<Instruction> -> <If>");

            If();
        }
        // [9] <Instruction> -> <While>
        else if (lookahead() == LexicalUnit.WHILE) {
            print(9, "<Instruction> -> <While>");

            While();
        }
        // [10] <Instruction> -> <For>
        else if (lookahead() == LexicalUnit.FOR) {
            print(10, "<Instruction> -> <For>");

            For();
        }
        // [11] <Instruction> -> <Print>
        else if (lookahead() == LexicalUnit.PRINT) {
            print(11, "<Instruction> -> <Print>");

            Print();
        }
        // [12] <Instruction> -> <Read>
        else if (lookahead() == LexicalUnit.READ) {
            print(12, "<Instruction> -> <Read>");

            Read();
        }
        else {
            System.out.println("Error Instruction");
        }
    }

    private void Assign() {
        // [13] <Assign> -> [VarName] := <ExprArith>
        if (lookahead() == LexicalUnit.VARNAME) {
            print(13, "<Assign> -> [VarName] := <ExprArith>");

            match(LexicalUnit.VARNAME);
            match(LexicalUnit.ASSIGN);
            ExprArith();
        }
        else {
            System.out.println("Error Assign");
        }
    }

    private void ExprArith() {
        // [14] <ExprArith> -> <Prod> <ExprArith'>
        if (
                lookahead() == LexicalUnit.VARNAME ||
                lookahead() == LexicalUnit.NUMBER ||
                lookahead() == LexicalUnit.MINUS ||
                lookahead() == LexicalUnit.LEFT_PARENTHESIS)
        {
            print(14, "<ExprArith> -> <Prod> <ExprArith'>");

            Prod();
            ExprArith_prime();
        }
        else {
            System.out.println("Error ExprArith");
        }
    }

    private void ExprArith_prime() {
        // [15] <ExprArith'> -> + <Prod> <ExprArith'>
        if (lookahead() == LexicalUnit.PLUS) {
            print(15, "<ExprArith'> -> + <Prod> <ExprArith'>");

            match(LexicalUnit.PLUS);
            Prod();
            ExprArith_prime();
        }
        // [16] <ExprArith'> -> - <Prod> <ExprArith'>
        else if (lookahead() == LexicalUnit.MINUS) {
            print(16, "<ExprArith'> -> - <Prod> <ExprArith'>");

            match(LexicalUnit.MINUS);
            Prod();
            ExprArith_prime();
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
        }
        else {
            System.out.println("Error ExprArith'");
        }
    }

    private void Prod() {
        // [18] <Prod> -> <Atom> <Prod'>
        if (
                lookahead() == LexicalUnit.VARNAME ||
                lookahead() == LexicalUnit.NUMBER ||
                lookahead() == LexicalUnit.MINUS ||
                lookahead() == LexicalUnit.LEFT_PARENTHESIS)
        {
            print(18, "<Prod> -> <Atom> <Prod'>");

            Atom();
            Prod_prime();
        }
        else {
            System.out.println("Error ExprArith");
        }
    }

    private void Prod_prime() {
        // [19] <Prod'> -> * <Atom> <Prod'>
        if (lookahead() == LexicalUnit.TIMES) {
            print(19, "<Prod'> -> * <Atom> <Prod'>");

            match(LexicalUnit.TIMES);
            Atom();
            Prod_prime();
        }
        // [20] <Prod'> -> / <Atom> <Prod'>
        else if (lookahead() == LexicalUnit.DIVIDE) {
            print(20, "<Prod'> -> / <Atom> <Prod'>");

            match(LexicalUnit.TIMES);
            Atom();
            Prod_prime();
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
        }
        else {
            System.out.println("Error Prod'");
        }
    }

    private void Atom() {
        // [22] <Atom> -> - <Atom>
        if (lookahead() == LexicalUnit.MINUS) {
            print(22, "<Atom> -> - <Atom>");

            match(LexicalUnit.MINUS);
            Atom();
        }
        // [23] <Atom> -> [Number]
        else if (lookahead() == LexicalUnit.NUMBER) {
            print(23, "<Atom> -> [Number]");

            match(LexicalUnit.NUMBER);
        }
        // [24] <Atom> -> [VarName]
        else if (lookahead() == LexicalUnit.VARNAME) {
            print(24, "<Atom> -> [VarName]");

            match(LexicalUnit.VARNAME);
        }
        // [25] <Atom> -> ( <ExprArith> )
        else if (lookahead() == LexicalUnit.LEFT_PARENTHESIS) {
            print(25, "<Atom> -> ( <ExprArith> )");

            match(LexicalUnit.LEFT_PARENTHESIS);
            ExprArith();
            match(LexicalUnit.RIGHT_PARENTHESIS);
        }
        else {
            System.out.println("Error Atom");
        }
    }

    private void If() {
        // [26] <If> -> if <Cond> then <Code> <IfSeq>
        if (lookahead() == LexicalUnit.IF) {
            print(26, "<If> -> if <Cond> then <Code> <IfSeq>");

            match(LexicalUnit.IF);
            Cond();
            match(LexicalUnit.THEN);
            Code();
            IfSeq();
        }
        else {
            System.out.println("Error If");
        }
    }

    private void IfSeq() {
        // [27] <IfSeq> -> endif
        if (lookahead() == LexicalUnit.ENDIF) {
            print(27, "<IfSeq> -> endif");

            match(LexicalUnit.ENDIF);
        }
        // [28] <IfSeq> -> else <Code> endif
        else if (lookahead() == LexicalUnit.ELSE) {
            print(28, "<IfSeq> -> else <Code> endif");

            match(LexicalUnit.ELSE);
            Code();
            match(LexicalUnit.ENDIF);
        }
        else {
            System.out.println("Error IfSeq");
        }
    }

    private void Cond() {
        // [29] <Cond> -> <CondAnd> <Cond'>
        if (
                lookahead() == LexicalUnit.VARNAME ||
                lookahead() == LexicalUnit.NUMBER ||
                lookahead() == LexicalUnit.MINUS ||
                lookahead() == LexicalUnit.LEFT_PARENTHESIS ||
                lookahead() == LexicalUnit.NOT)
        {
            print(29, "<Cond> -> <CondAnd> <Cond'>");

            CondAnd();
            Cond_prime();
        }
        else {
            System.out.println("Error Cond");
        }
    }

    private void Cond_prime() {
        // [30] <Cond'> -> or <CondAnd> <Cond'>
        if (lookahead() == LexicalUnit.OR) {
            print(30, "<Cond'> -> or <CondAnd> <Cond'>");

            match(LexicalUnit.OR);
            CondAnd();
            Cond_prime();
        }
        // [31] <Cond'> -> ε
        else if (
                lookahead() == LexicalUnit.THEN ||
                lookahead() == LexicalUnit.DO)
        {
            print(31, "<Cond'> -> ε");
        }
        else {
            System.out.println("Error Cond'");
        }
    }

    private void CondAnd() {
        // [32] <CondAnd> -> <SimpleCond> <CondAnd'>
        if (
                lookahead() == LexicalUnit.VARNAME ||
                lookahead() == LexicalUnit.NUMBER ||
                lookahead() == LexicalUnit.MINUS ||
                lookahead() == LexicalUnit.LEFT_PARENTHESIS ||
                lookahead() == LexicalUnit.NOT)
        {
            print(32, "<CondAnd> -> <SimpleCond> <CondAnd'>");

            SimpleCond();
            CondAnd_prime();
        }
        else {
            System.out.println("Error CondAnd");
        }
    }

    private void CondAnd_prime() {
        // [33] <CondAnd'> -> and <SimpleCond> <CondAnd'>
        if (lookahead() == LexicalUnit.AND) {
            print(33, "<CondAnd'> -> and <SimpleCond> <CondAnd'>");

            match(LexicalUnit.AND);
            SimpleCond();
            CondAnd_prime();
        }
        // [34] <CondAnd'> -> ε
        else if (
                lookahead() == LexicalUnit.THEN ||
                lookahead() == LexicalUnit.DO ||
                lookahead() == LexicalUnit.OR)
        {
            print(34, "<CondAnd'> -> ε");
        }
        else {
            System.out.println("Error CondAnd'");
        }
    }

    private void SimpleCond() {
        // [35] <SimpleCond> -> <ExprArith> <Comp> <ExprArith>
        if (
                lookahead() == LexicalUnit.VARNAME ||
                lookahead() == LexicalUnit.NUMBER ||
                lookahead() == LexicalUnit.MINUS ||
                lookahead() == LexicalUnit.LEFT_PARENTHESIS)
        {
            print(35, "<SimpleCond> -> <ExprArith> <Comp> <ExprArith>");

            ExprArith();
            Comp();
            ExprArith();
        }
        // [36] <SimpleCond> -> not <SimpleCond>
        else if (lookahead() == LexicalUnit.NOT) {
            print(36, "<SimpleCond> -> not <SimpleCond>");
        }
        else {
            System.out.println("Error SimpleCond");
        }
    }

    private void Comp() {
        // [37] <Comp> -> =
        if (lookahead() == LexicalUnit.EQUAL) {
            print(37, "<Comp> -> =");

            match(LexicalUnit.EQUAL);
        }
        // [38] <Comp> -> >=
        else if (lookahead() == LexicalUnit.GREATER_EQUAL) {
            print(38, "<Comp> -> >=");

            match(LexicalUnit.GREATER_EQUAL);
        }
        // [39] <Comp> -> >
        else if (lookahead() == LexicalUnit.GREATER) {
            print(39, "<Comp> -> >");

            match(LexicalUnit.GREATER);
        }
        // [40] <Comp> -> <=
        else if (lookahead() == LexicalUnit.SMALLER_EQUAL) {
            print(40, "<Comp> -> <=");

            match(LexicalUnit.SMALLER_EQUAL);
        }
        // [41] <Comp> -> <
        else if (lookahead() == LexicalUnit.SMALLER) {
            print(41, "<Comp> -> <");

            match(LexicalUnit.SMALLER);
        }
        // [42] <Comp> -> /=
        else if (lookahead() == LexicalUnit.DIFFERENT) {
            print(42, "<Comp> -> /=");

            match(LexicalUnit.DIFFERENT);
        }
        else {
            System.out.println("Error Comp");
        }
    }

    private void While() {
        // [43] <While> -> while <Cond> do <Code> endwhile
        if (lookahead() == LexicalUnit.WHILE) {
            print(43, "<While> -> while <Cond> do <Code> endwhile");

            match(LexicalUnit.WHILE);
            Cond();
            match(LexicalUnit.DO);
            Code();
            match(LexicalUnit.ENDWHILE);
        }
        else {
            System.out.println("Error While");
        }
    }

    private void For() {
        // [44] <For> -> for [VarName] from <ExprArith> by <ExprArith> to <ExprArith> do <Code> endwhile
        if (lookahead() == LexicalUnit.FOR) {
            print(44, "<For> -> for [VarName] from <ExprArith> by <ExprArith> to " +
                    "<ExprArith> do <Code> endwhile");

            match(LexicalUnit.FOR);
            match(LexicalUnit.VARNAME);
            match(LexicalUnit.FROM);
            ExprArith();
            match(LexicalUnit.BY);
            ExprArith();
            match(LexicalUnit.TO);
            ExprArith();
            match(LexicalUnit.DO);
            Code();
            match(LexicalUnit.ENDWHILE);
        }
        else {
            System.out.println("Error While");
        }
    }

    private void Print() {
        // [45] <Print> -> print([VarName])
        if (lookahead() == LexicalUnit.PRINT) {
            print(45, "<Print> -> print([VarName])");

            match(LexicalUnit.PRINT);
            match(LexicalUnit.LEFT_PARENTHESIS);
            match(LexicalUnit.VARNAME);
            match(LexicalUnit.RIGHT_PARENTHESIS);
        }
        else {
            System.out.println("Error Print");
        }
    }

    private void Read() {
        // [46] <Read> -> read([VarName])
        if (lookahead() == LexicalUnit.READ) {
            print(46, "<Read> -> read([VarName])");

            match(LexicalUnit.READ);
            match(LexicalUnit.LEFT_PARENTHESIS);
            match(LexicalUnit.VARNAME);
            match(LexicalUnit.RIGHT_PARENTHESIS);
        }
        else {
            System.out.println("Error Read");
        }
    }

    private void match(LexicalUnit type)
    {
        if (lookahead() == type) {
            if (v) {
                System.out.print("Match : ");
                System.out.println(type);
            }
            l++;
        }
        else
            System.out.println("Error");
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
