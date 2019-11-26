import java.util.List;
import java.util.ArrayList;

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

public class ParseTree extends AbstractSyntaxTree {
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
     * @param label  The label of the root
     * @param children Its children
     */
    public ParseTree(Symbol label, List<ParseTree> children) {
        super(label);
        this.children = children;
    }

    static public AbstractSyntaxTree toAST(ParseTree parseTree) {
        List<AbstractSyntaxTree> childrenAST = new ArrayList<>();

        for (ParseTree child : parseTree.children) {
            if (child.label.isTerminal()) {
                if (isToBeKept(child)) {
                    childrenAST.add(new AbstractSyntaxTree(child.label));
                }
            }
            else if (child.label.isNonTerminal()) {

                if (isToBeKept(child)) {
                    childrenAST.add(toAST(child));
                }
                else {
                    for (ParseTree greatChild : child.children) {
                        childrenAST.add(toAST(greatChild));
                    }
                }
            }
        }

        return new AbstractSyntaxTree(parseTree.label, childrenAST);
    }

    static private boolean isToBeKept(ParseTree parseTree) {
        return true;
    }
}
