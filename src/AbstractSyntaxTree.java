import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbstractSyntaxTree {
    private Symbol label;
    private List<AbstractSyntaxTree> children;
    private Compiler compiler;

    /**
     * Creates a singleton tree with only a root labeled by label.
     *
     * @param label The label of the root
     */
    public AbstractSyntaxTree(Symbol label) {
        this(label, new ArrayList<>());
    }

    /**
     * Creates a tree with root labeled by label and with children.
     *
     * @param label    The label of the root
     * @param children Its children
     */
    public AbstractSyntaxTree(Symbol label, List<AbstractSyntaxTree> children) {
        this.label = label;
        this.children = children;
    }

    public AbstractSyntaxTree(ParseTree parseTree) {
        this(parseTree.getLabel(), new ArrayList<>());
        createTree(parseTree.getChildren(), children);

        simplify();
    }

    /**
     * Writes the tree as LaTeX code
     */
    public String toLaTexTree() {
        StringBuilder treeTeX = new StringBuilder();
        treeTeX.append("[");
        treeTeX.append("{").append(getValue()).append("}");
        treeTeX.append(" ");

        for (AbstractSyntaxTree child : children) {
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
        treeTikZ.append(getValue());
        treeTikZ.append("}\n");
        for (AbstractSyntaxTree child : children) {
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

    public void simplify() {
        if (getValue() == "<ExprArith>" && numberOfChildren() == 1) {
            AbstractSyntaxTree child = childAt(0);

            if (child.getValue() == "<ExprArith>") {
                children = child.getChildren();
            }
        }

        if (getValue() == "<ExprArith>" && numberOfChildren() >= 3) {
            children = Collections.singletonList(rearrangeTree());
        }

        if (getValue() == "<Cond>" && numberOfChildren() == 1) {
            AbstractSyntaxTree child = childAt(0);

            if (child.getValue() == "<Cond>") {
                children = child.getChildren();
            }
        }

        if (getValue() == "<Cond>" && numberOfChildren() >= 3) {
            children = Collections.singletonList(rearrangeTree());
        }

        for (AbstractSyntaxTree child : children) {
            child.simplify();
        }
    }

    private int getOperatorIndex() {
        for (int i = numberOfChildren() - 1; i >= 0; i--) {
            AbstractSyntaxTree child = childAt(i);
            if (child.getLabel().getType() == LexicalUnit.AND) {
                return i;
            }
        }

        for (int i = numberOfChildren() - 1; i >= 0; i--) {
            AbstractSyntaxTree child = childAt(i);
            if (child.getLabel().getType() == LexicalUnit.OR) {
                return i;
            }
        }

        for (int i = numberOfChildren() - 1; i >= 0; i--) {
            AbstractSyntaxTree child = childAt(i);
            if (child.getLabel().getType() != null) {
                switch (child.getLabel().getType()) {
                    case EQUAL:
                    case DIFFERENT:
                    case GREATER:
                    case GREATER_EQUAL:
                    case SMALLER:
                    case SMALLER_EQUAL:
                        return i;
                }
            }
        }

        for (int i = numberOfChildren() - 1; i >= 0; i--) {
            AbstractSyntaxTree child = childAt(i);
            if (child.getLabel().getType() != null) {
                switch (child.getLabel().getType()) {
                    case PLUS:
                    case MINUS:
                        return i;
                }
            }
        }

        for (int i = numberOfChildren() - 1; i >= 0; i--) {
            AbstractSyntaxTree child = childAt(i);
            if (child.getLabel().getType() != null) {
                switch (child.getLabel().getType()) {
                    case DIVIDE:
                    case TIMES:
                        return i;
                }
            }
        }
        return -1;
    }

    private AbstractSyntaxTree rearrangeTree() {
        int operatorIndex = getOperatorIndex();
        AbstractSyntaxTree operator = childAt(operatorIndex);
        List<AbstractSyntaxTree> operatorChildren = new ArrayList<>();

        if (operatorIndex > 1) {
            AbstractSyntaxTree temp = new AbstractSyntaxTree(new Symbol(null, "temp"));
            int startPos = 0;
            for (int i = startPos; i < operatorIndex; i++) {
                temp.getChildren().add(childAt(startPos));
                children.remove(startPos);
            }
            operatorChildren.add(temp.rearrangeTree());
        } else {
            AbstractSyntaxTree child = childAt(0);
            if (child.getValue() == "<ExprArith>" || child.getValue() == "<Cond>") {
                if (child.numberOfChildren() >= 3) {
                    operatorChildren.add(child.rearrangeTree());
                } else {
                    operatorChildren.add(child.childAt(0));
                }
            } else
                operatorChildren.add(child);
            children.remove(0);
        }

        operatorIndex = getOperatorIndex();

        if (operatorIndex < numberOfChildren() - 2) {

            AbstractSyntaxTree temp = new AbstractSyntaxTree(new Symbol(null, "temp"));
            int startPos = 1;
            int childrenSize = numberOfChildren();
            for (int i = startPos; i < childrenSize; i++) {
                temp.getChildren().add(childAt(startPos));
                children.remove(startPos);
            }
            operatorChildren.add(temp.rearrangeTree());
        } else {
            AbstractSyntaxTree child = childAt(numberOfChildren() - 1);
            if (child.getValue() == "<ExprArith>" || child.getValue() == "<Cond>") {
                if (child.numberOfChildren() >= 3) {
                    operatorChildren.add(child.rearrangeTree());
                } else {
                    operatorChildren.add(child.childAt(0));
                }
            } else
                operatorChildren.add(child);
            children.remove(numberOfChildren() - 1);
        }

        operator.children = operatorChildren;

        return operator;
    }

    public Symbol getLabel() {
        return label;
    }

    public List<AbstractSyntaxTree> getChildren() {
        return children;
    }

    public AbstractSyntaxTree childAt(int i) {
        return children.get(i);
    }

    public int numberOfChildren() {
        return children.size();
    }

    private Object getValue() {
        return label.getValue();
    }

    /**
     * Create an AbstractSyntaxTree from a ParseTree by keeping certain nodes and removing the others.
     *
     * @param childrenPT children of a ParseTree
     * @param childrenAST children of an AbstractSyntaxTree created during the process
     */
    static private void createTree(List<ParseTree> childrenPT, List<AbstractSyntaxTree> childrenAST) {
        for (ParseTree childPT : childrenPT) {
            if (isToBeKept(childPT)) {
                childrenAST.add(new AbstractSyntaxTree(childPT.getLabel()));
            }

            if (childPT.getLabel().isNonTerminal()) {
                if (isToBeKept(childPT)) {
                    createTree(childPT.getChildren(), childrenAST.get(childrenAST.size() - 1).children);
                } else {
                    createTree(childPT.getChildren(), childrenAST);
                }
            }
        }
    }

    static private boolean isToBeKept(ParseTree nodeParseTree) {
        if (nodeParseTree.getLabel().isNonTerminal()) {
            String nonTerminal = nodeParseTree.getLabel().getValue().toString();
            switch (nonTerminal) {
                case "<Program>":
                case "<Code>":
                case "<Assign>":
                case "<If>":
                case "<While>":
                case "<For>":
                case "<Print>":
                case "<Read>":
                case "<ExprArith>":
                case "<Cond>":
                    return true;
            }
        } else {
            LexicalUnit terminal = nodeParseTree.getLabel().getType();
            switch (terminal) {
                case VARNAME:
                case MINUS:
                case NUMBER:
                case OR:
                case EQUAL:
                case GREATER:
                case SMALLER:
                case DIFFERENT:
                case GREATER_EQUAL:
                case SMALLER_EQUAL:
                case AND:
                case END:
                case PLUS:
                case NOT:
                case TIMES:
                case DIVIDE:
                case BEG:
                    return true;
            }
        }
        return false;
    }
}
