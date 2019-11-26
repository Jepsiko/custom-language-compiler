import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbstractSyntaxTree {
    private Symbol label;
    private List<AbstractSyntaxTree> children;
    private Compiler compiler;

    /**
     * Creates a singleton tree with only a root labeled by lbl.
     *
     * @param label The label of the root
     */
    public AbstractSyntaxTree(Symbol label) {
        this(label, new ArrayList<>());
    }

    /**
     * Creates a tree with root labeled by lbl and children chdn.
     *
     * @param label    The label of the root
     * @param children Its children
     */
    public AbstractSyntaxTree(Symbol label, List<AbstractSyntaxTree> children) {
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
        treeTikZ.append(label.getValue());
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
        if (label.getValue() == "<Cond>"  && children.size() == 1) {
            AbstractSyntaxTree child = children.get(0);
            children = child.children;
        }

        if (label.getValue() == "<ExprArith>" && children.size() == 1) {
            AbstractSyntaxTree child = children.get(0);

            if (child.label.getValue() == "<ExprArith>") {
                children = child.children;
            }
        }

        if (label.getValue() == "<SimpleCond>" && children.size() == 3) {
            children = Collections.singletonList(rearrangeTree());
        }

        if (label.getValue() == "<ExprArith>" && children.size() >= 3) {
            children = Collections.singletonList(rearrangeTree());
        }

        for (AbstractSyntaxTree child : children) {
            child.simplify();
        }
    }

    private int getOperatorIndex() {
        for (int i = children.size()-1; i >= 0; i--) {
            AbstractSyntaxTree child = children.get(i);
            if (child.label.getType() != null) {
                switch (child.label.getType()) {
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

        for (int i = children.size()-1; i >= 0; i--) {
            AbstractSyntaxTree child = children.get(i);
            if (child.label.getType() != null) {
                switch (child.label.getType()) {
                    case PLUS:
                    case MINUS:
                        return i;
                }
            }
        }

        for (int i = children.size()-1; i >= 0; i--) {
            AbstractSyntaxTree child = children.get(i);
            if (child.label.getType() != null) {
                switch (child.label.getType()) {
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
        System.out.println(operatorIndex);
        System.out.println(children.size());
        AbstractSyntaxTree operator = children.get(operatorIndex);
        System.out.println(operator.label.getValue());
        List<AbstractSyntaxTree> operatorChildren = new ArrayList<>();

        if (operatorIndex > 1) {
            AbstractSyntaxTree temp = new AbstractSyntaxTree(new Symbol(null, "temp"));
            int startPos = 0;
            for (int i = startPos; i < operatorIndex; i++) {
                temp.children.add(children.get(startPos));
                System.out.println("Left : " + children.get(startPos).label.getValue());
                children.remove(startPos);
            }
            operatorChildren.add(temp.rearrangeTree());
        }
        else {
            operatorChildren.add(children.get(0));
        }

        if (operatorIndex < children.size()-2) {

            AbstractSyntaxTree temp = new AbstractSyntaxTree(new Symbol(null, "temp"));
            int startPos = 2;
            int childrenSize = children.size();
            for (int i = startPos; i < childrenSize; i++) {
                temp.children.add(children.get(startPos));
                System.out.println("Right : " + children.get(startPos).label.getValue());
                children.remove(startPos);
            }
            operatorChildren.add(temp.rearrangeTree());
        }
        else {
            operatorChildren.add(children.get(children.size()-1));
        }

        operator.children = operatorChildren;

        return operator;
    }

    public List<AbstractSyntaxTree> traversal() {
        List<AbstractSyntaxTree> path = new ArrayList<>();
        path.add(this);
        for (AbstractSyntaxTree child : children) {
            path.addAll(child.traversal());
        }
        return path;
    }

    public Symbol getLabel() {
        return label;
    }
}
