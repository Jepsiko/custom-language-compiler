import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

/**
 * A skeleton class to represent parse trees. The arity is not fixed: a node can
 * have 0, 1 or more children. Trees are represented in the following way: Tree
 * :== Symbol * List(Tree) In other words, trees are defined recursively: A tree
 * is a root (with a label of type Symbol) and a list of trees children. Thus, a
 * leave is simply a tree with no children (its list of children is empty). This
 * class can also be seen as representing the Node of a tree, in which case a
 * tree is simply represented as its root.
 *
 * @author Léo Exibard, Sarah Winter
 */

public class ParseTree {
    private Symbol label;
    private List<ParseTree> children;

    /**
     * Creates a singleton tree with only a root labeled by lbl.
     *
     * @param label The label of the root
     */
    public ParseTree(Symbol label) {
        this(label, new ArrayList<>()); // This tree has no children
    }

    /**
     * Creates a tree with root labeled by lbl and children chdn.
     *
     * @param label    The label of the root
     * @param children Its children
     */
    public ParseTree(Symbol label, List<ParseTree> children) {
        this.label = label;
        this.children = children;
    }

    /**
     * Writes the tree as LaTeX code
     */
    public String toLaTexTree() {
        StringBuilder treeTeX = new StringBuilder();
        treeTeX.append("[");
        treeTeX.append("{").append(label.getValue()).append("}");
        treeTeX.append(" ");

        for (ParseTree child : children) {
            treeTeX.append(child.toLaTexTree());
        }
        treeTeX.append("]");
        return treeTeX.toString();
    }

    /**
     * Writes the tree as TikZ code. TikZ is a language to specify drawings in LaTeX
     * files.
     */
    public String toTikZ() {
        StringBuilder treeTikZ = new StringBuilder();
        treeTikZ.append("node {");
        treeTikZ.append(label.getValue());
        treeTikZ.append("}\n");
        for (ParseTree child : children) {
            treeTikZ.append("child { ");
            treeTikZ.append(child.toTikZ());
            treeTikZ.append(" }\n");
        }
        return treeTikZ.toString();
    }

    /**
     * Writes the tree as a TikZ picture. A TikZ picture embeds TikZ code so that
     * LaTeX undertands it.
     */
    public String toTikZPicture() {
        return "\\begin{tikzpicture}[tree layout]\n\\" + toTikZ() + ";\n\\end{tikzpicture}";
    }

    /**
     * Writes the tree as a LaTeX document which can be compiled (using the LuaLaTeX
     * engine). Be careful that such code will not compile with PDFLaTeX, since the
     * tree drawing algorithm is written in Lua. The code is not very readable as
     * such, but you can have a look at the outputted file if you want to understand
     * better. <br>
     * <br>
     * The result can be used with the command:
     *
     * <pre>
     * lualatex some-file.tex
     * </pre>
     */
    public String toLaTeXLua() {
        return "\\RequirePackage{luatex85}\n\\documentclass{standalone}\n\n\\usepackage[T1]{fontenc}\n\\usepackage{tikz}\n\n\\usetikzlibrary{graphdrawing, graphdrawing.trees}\n\n\\begin{document}\n\n"
                + toTikZPicture() + "\n\n\\end{document}\n%% Local Variables:\n%% TeX-engine: luatex\n%% End:";
    }

    /**
     * Writes the tree as a forest picture. Returns the tree in forest enviroment
     * using the latex code of the tree
     */
    public String toForestPicture() {
        return "\\begin{forest}for tree={rectangle,draw, l sep=20pt}" + toLaTexTree() + ";\n\\end{forest}";
    }

    /**
     * Writes the tree as a LaTeX document which can be compiled using PDFLaTeX.
     * <br>
     * <br>
     * The result can be used with the command:
     *
     * <pre>
     * pdflatex some-file.tex
     * </pre>
     */
    public String toLaTeX() {
        return "\\documentclass[border=5pt]{standalone}\n\n\\usepackage[T1]{fontenc}\n\\usepackage{tikz}\n\\usepackage{forest}\n\n\\begin{document}\n\n"
                + toForestPicture() + "\n\n\\end{document}\n%% Local Variables:\n%% TeX-engine: pdflatex\n%% End:";
    }

    static public AbstractSyntaxTree toAST(ParseTree parseTree) {
        AbstractSyntaxTree AST = createTree(parseTree);

        AST.simplify();

        return AST;
    }

    static private AbstractSyntaxTree createTree(ParseTree parseTree) {

        List<AbstractSyntaxTree> childrenAST = new ArrayList<>();

        for (ParseTree child : parseTree.children) {
            if (child.label.isTerminal()) { // If terminal to keep
                if (isToBeKept(child)) {
                    childrenAST.add(new AbstractSyntaxTree(child.label));
                }
            } else if (child.label.isNonTerminal()) {
                if (isToBeKept(child)) {
                    childrenAST.add(createTree(child));
                } else {
                    Stack<ParseTree> stack = new Stack<>();
                    for (int i = child.children.size()-1; i >= 0; i--) { // Initialization of the stack
                        ParseTree greatChild = child.children.get(i); // Little child
                        if (isToBeKept(greatChild)) {
                            if (greatChild.label.isTerminal()) {
                                childrenAST.add(new AbstractSyntaxTree(greatChild.label));
                            }
                            else {
                                childrenAST.add(createTree(greatChild));
                            }
                        }
                        else {
                            stack.push(greatChild);
                        }
                    }

                    ParseTree current;
                    while (!stack.isEmpty()) {

                        current = stack.pop();
                        for (int i = current.children.size()-1; i >= 0; i--) {
                            ParseTree greatChild = current.children.get(i);
                            if (isToBeKept(greatChild)) {
                                if (greatChild.label.isTerminal()) {
                                    childrenAST.add(new AbstractSyntaxTree(greatChild.label));
                                }
                                else {
                                    childrenAST.add(createTree(greatChild));
                                }
                            }
                            else {
                                stack.push(greatChild);
                            }
                        }
                    }
                }
            }
        }

        return new AbstractSyntaxTree(parseTree.label, childrenAST);
    }

    static private boolean isToBeKept(ParseTree parseTree) {
        if (parseTree.label.isNonTerminal()) {
            String nonTerminal = parseTree.label.getValue().toString();
            switch (nonTerminal) {
                case "<InstList>":
                case "<Instruction>":
                case "<NextInst>":
                case "<Prod>":
                case "<Atom>":
                case "<IfSeq>":
                case "<Cond'>":
                case "<CondAnd'>":
                case "<ExprArith'>":
                case "<Comp>":
                case "<Prod'>":
                    return false;
            }
        } else {
            LexicalUnit terminal = parseTree.label.getType();
            switch (terminal) {
                case LEFT_PARENTHESIS:
                case RIGHT_PARENTHESIS:
                case SEMICOLON:
                case READ:
                case DO:
                case THEN:
                case BY:
                case TO:
                case FROM:
                case FOR:
                case IF:
                case PRINT:
                case ENDIF:
                case ELSE:
                case ENDWHILE:
                case ASSIGN:
                case WHILE:
                case AND:
                case OR:
                    return false;
            }
        }
        return true;
    }
}
