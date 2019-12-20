import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class reprensenting an AbstractSyntaxTree.
 * It as only necessary informations for the compiler and all the operations
 * are represented as tree with the operator at the top and the operants as children.
 */
public class AbstractSyntaxTree {
    private Symbol label;
    private List<AbstractSyntaxTree> children;

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


        try {
            Path file = Paths.get("tree-building.tex");
            Files.write(file, Collections.singleton(toLaTeX()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    /**
     * Recursive function which simplify and rearrange depending the type of the evaluation
     */
    public void simplify() {

        // The case where an <ExprArith> as at least 3 children.
        // That means that the children contains an operator.
        // So we replace the <ExprArith> with result of the rearranged tree.
        // Giving the node of the less priority operator.
        if (getValue() == "<ExprArith>" && numberOfChildren() >= 3) {
            children = Collections.singletonList(rearrangeTree());
        }

        // The case where an <Cond> as at least 3 children.
        // That means that the children contains an operator.
        // So we replace the <Cond> with result of the rearranged tree.
        // Giving the node of the less priority operator.
        if (getValue() == "<Cond>" && numberOfChildren() >= 3) {
            children = Collections.singletonList(rearrangeTree());
        }

        // We call this function recursively on all the children of the this node.
        for (AbstractSyntaxTree child : children) {
            child.simplify();
        }

        // If we have still some <ExprArith> and <Cond>, we delete them knowing
        // that they all have only one child.
        int size = numberOfChildren();
        for (int i = 0; i < size; i++) {
            if (childAt(i).getValue() == "<ExprArith>" || childAt(i).getValue() == "<Cond>") {
                children.set(i, childAt(i).childAt(0));
            }
        }
    }

    /**
     * Get the index of the less priority operator among all the children while rearranging the tree.
     * @return index
     */
    private int getOperatorIndex() {
        // OR - left associativity (reverse-loop)
        for (int i = numberOfChildren() - 1; i >= 0; i--) {
            AbstractSyntaxTree child = childAt(i);
            if (child.getLabel().getType() == LexicalUnit.OR) {
                return i;
            }
        }

        // AND - left associativity (reverse-loop)
        for (int i = numberOfChildren() - 1; i >= 0; i--) {
            AbstractSyntaxTree child = childAt(i);
            if (child.getLabel().getType() == LexicalUnit.AND) {
                return i;
            }
        }

        // NOT - right associativity (normal loop)
        for (int i = 0; i < numberOfChildren(); i++) {
            AbstractSyntaxTree child = childAt(i);
            if (child.getLabel().getType() == LexicalUnit.NOT) {
                return i;
            }
        }

        // COMPARATOR - left associativity (reverse-loop)
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

        // ADDITION SOUSTRACTION - left associativity (reverse-loop)
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

        // MULTIPLICATION DIVISION - left associativity (reverse-loop)
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

    /**
     * Rearrange the tree for each side of the less priority operator.
     *
     * All the children at the left side of the operator will be made children
     * of a temporary node which will be the left child of the operator.
     * Then the function repeats itself recursively on the temporary child.
     *
     * The same is done for all the children at the right side of the operator.
     *
     * This makes the operator to have only two children transforming the tree
     * in a binary tree below him.
     *
     * @return the operator node
     */
    private AbstractSyntaxTree rearrangeTree() {
        int operatorIndex = getOperatorIndex();
        AbstractSyntaxTree operator = childAt(operatorIndex);
        // operatorChildren will be with a maximum size of 2 at the end of this code.
        List<AbstractSyntaxTree> operatorChildren = new ArrayList<>();

        // If the operator has more than one node at his left we create a temporary node
        // to store all the nodes as his children an removing them from this node's children.
        if (operatorIndex > 1) {
            AbstractSyntaxTree temp = new AbstractSyntaxTree(new Symbol(null, "temp"));
            int startPos = 0;
            for (int i = startPos; i < operatorIndex; i++) {
                temp.getChildren().add(childAt(startPos));
                children.remove(startPos);
            }
            operatorChildren.add(temp.rearrangeTree());
        }
        // If we have only one node at the left of the operator we look if this child
        // is an <ExprArith> or a <Cond>. In this case it will have to rearrange the tree
        // on the child to remove them. In the other case we can add the child meaning
        // it's a [Number] or a [VarName].
        else if (operatorIndex == 1) {
            AbstractSyntaxTree child = childAt(0);
            if (child.getValue() == "<ExprArith>" || child.getValue() == "<Cond>") {
                if (child.numberOfChildren() >= 3) {
                    operatorChildren.add(child.rearrangeTree());
                }
                else {
                    operatorChildren.add(child.childAt(0));
                }
            }
            else
                operatorChildren.add(child);
            children.remove(0);
        }

        // We do the same for the other side.
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
        }
        else {
            AbstractSyntaxTree child = childAt(numberOfChildren() - 1);
            if (child.getValue() == "<ExprArith>" || child.getValue() == "<Cond>") {
                if (child.numberOfChildren() >= 3) {
                    operatorChildren.add(child.rearrangeTree());
                }
                else {
                    operatorChildren.add(child.childAt(0));
                }
            }
            else
                operatorChildren.add(child);
            children.remove(numberOfChildren() - 1);
        }

        operator.children = operatorChildren;

        return operator;
    }

    /**
     * Get the label
     * @return a label
     */
    public Symbol getLabel() {
        return label;
    }

    /**
     * Get the children of the tree
     * @return children of the tree
     */
    public List<AbstractSyntaxTree> getChildren() {
        return children;
    }

    /**
     * Get the child at the index i in the tree
     * @param i index
     * @return the child at index i
     */
    public AbstractSyntaxTree childAt(int i) {
        return children.get(i);
    }

    /**
     * Get the number of children
     * @return size of the children
     */
    public int numberOfChildren() {
        return children.size();
    }

    /**
     * Get the value  of the label
     */
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
            // We only keep the childPT that we want as a new child of the childrenAST
            if (isToBeKept(childPT)) {
                childrenAST.add(new AbstractSyntaxTree(childPT.getLabel()));
            }

            // If the node has children
            if (childPT.getLabel().isNonTerminal()) {
                // For the childPT that we keep, we repeate the function recursively on the children of
                // the childPT and on the last child added in the childrenAST.
                // Since the childPT is to be kept, we are sure that it made a new child just before.
                if (isToBeKept(childPT)) {
                    createTree(childPT.getChildren(), childrenAST.get(childrenAST.size() - 1).children);
                }
                // Otherwise we repeate recursively on the children of the childPT but on the same childrenAST.
                // This way ignoring the node but not his children.
                else {
                    createTree(childPT.getChildren(), childrenAST);
                }
            }
        }
    }

    /**
     * Check if a certain node of the ParseTree is to be kept
     *
     * @param nodeParseTree the node for the check
     * @return true if the node is to be kept, false otherwise
     */
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
