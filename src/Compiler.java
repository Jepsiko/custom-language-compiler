import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for the compiler.
 * The compiler can compile from an AbstractSyntaxTree.
 */
public class Compiler {
    /**
     * Path to the .ll file for the compiled code
     */
    private Path outputFile;
    /**
     * Boolean equal to true if we write to file, false if we write on the stdin
     */
    private final boolean toFile;
    /**
     * Number representing an unnamed variable, incremented each time the variable isn't used anymore
     */
    private int unnamedVar;
    /**
     * Different index for each while loop to make each label unique
     */
    private int whileIndex;
    /**
     * Different index for each for loop to make each label unique
     */
    private int forIndex;
    /**
     * Different index for each if/else condition to make each label unique
     */
    private int ifIndex;
    /**
     * List of all the variables used in the program
     */
    private List<String> variables;

    public Compiler() {
        toFile = false;
        unnamedVar = 0;
        whileIndex = 0;
        forIndex = 0;
        ifIndex = 0;
        variables = new ArrayList<>();
    }

    public Compiler(String outputFilename) {
        if (!outputFilename.equals("")) {
            toFile = true;
            outputFile = Paths.get(outputFilename);
            try {
                Files.write(outputFile, Collections.singleton(""), StandardCharsets.UTF_8); // Empty the file
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            toFile = false;
        }
        unnamedVar = 0;
        whileIndex = 0;
        forIndex = 0;
        ifIndex = 0;
        variables = new ArrayList<>();
    }

    /**
     * Compile function which takes the AbstractSyntaxTree reprensenting the code and creates
     * a LLVM IR code corresponding to the algorithm of the AST.
     *
     * @param AST AbstractSyntaxTree representing the ALGOL0 code to be compiled
     */
    public void compile(AbstractSyntaxTree AST) {
        Program(AST);
    }

    private void Program(AbstractSyntaxTree AST) {
        begin();
        Code(AST.childAt(1));
        end();
    }

    /**
     * Write some LLVM functions to print and read from input.
     * Then it writes the beginning of the main function.
     */
    private void begin() {
        write("@.strP = private unnamed_addr constant [4 x i8] c\"%d\\0A\\00\", align 1\n" +
                "\n" +
                "; Function Attrs: nounwind uwtable\n" +
                "define void @println(i32 %x) #0 {\n" +
                "\t%1 = alloca i32, align 4\n" +
                "\tstore i32 %x, i32* %1, align 4\n" +
                "\t%2 = load i32, i32* %1, align 4\n" +
                "\t%3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strP, i32 0, i32 0), i32 %2)\n" +
                "\tret void\n" +
                "}\n" +
                "\n" +
                "declare i32 @printf(i8*, ...) #1\n" +
                "\n" +
                "declare i32 @getchar()\n" +
                "\n" +
                "define i32 @readInt() {\n" +
                "entry:\n" +
                "\t%res = alloca i32\n" +
                "\tstore i32 0, i32* %res\n" +
                "\n" +
                "\t%isNegative = alloca i1\n" +
                "\t%number = alloca i32\n" +
                "\t%0 = call i32 @getchar()\n" +
                "\t\n" +
                "\t%1 = icmp eq i32 %0, 45\n" +
                "\tstore i1 %1, i1* %isNegative\n" +
                "\tbr i1 %1, label %loop, label %firstIteration\n" +
                "firstIteration:\n" +
                "\t%2 = sub i32 %0, 48\n" +
                "\tstore i32 %2, i32* %number\n" +
                "\n" +
                "\t%3 = icmp sge i32 %2, 0\n" +
                "\t%4 = icmp sle i32 %2, 9\n" +
                "\t%5 = and i1 %3, %4\n" +
                "\tbr i1 %5, label %continue, label %exit\n" +
                "loop:\n" +
                "\t%6 = call i32 @getchar()\n" +
                "\t%7 = sub i32 %6, 48\n" +
                "\tstore i32 %7, i32* %number\n" +
                "\n" +
                "\t%8 = icmp sge i32 %7, 0\n" +
                "\t%9 = icmp sle i32 %7, 9\n" +
                "\t%10 = and i1 %8, %9\n" +
                "\tbr i1 %10, label %continue, label %exit\n" +
                "continue:\n" +
                "\t%11 = load i32, i32* %res\n" +
                "\t%12 = mul i32 %11, 10\n" +
                "\t%13 = load i32, i32* %number\n" +
                "\t%14 = add i32 %12, %13 \n" +
                "\tstore i32 %14, i32* %res\n" +
                "\n" +
                "\tbr label %loop\n" +
                "exit:\n" +
                "\n" +
                "\t%15 = load i1, i1* %isNegative\n" +
                "\tbr i1 %15, label %ifNegative, label %endifNegative\n" +
                "\n" +
                "ifNegative:\n" +
                "\t%16 = load i32, i32* %res\n" +
                "\t%17 = mul i32 %16, -1\n" +
                "\tstore i32 %17, i32* %res\n" +
                "\n" +
                "\tbr label %endifNegative\n" +
                "endifNegative:\n" +
                "\t%18 = load i32, i32* %res\n" +
                "\tret i32 %18\n" +
                "}\n" +
                "\n" +
                "\n" +
                "define i32 @main() {\n" +
                "entry:\n");
    }

    /**
     * For each instruction of the code the compilator calls the right function
     * with the AST of the instruction as parameter.
     *
     * @param AST the AbstractSyntaxTree whose label is Code
     */
    private void Code(AbstractSyntaxTree AST) {
        for (AbstractSyntaxTree instruction : AST.getChildren()) {
            switch (instruction.getLabel().getValue().toString()) {
                case "<Assign>":
                    Assign(instruction);
                    break;
                case "<Read>":
                    Read(instruction);
                    break;
                case "<Print>":
                    Print(instruction);
                    break;
                case "<While>":
                    While(instruction, whileIndex);
                    break;
                case "<For>":
                    For(instruction, forIndex);
                    break;
                case "<If>":
                    If(instruction, ifIndex);
                    break;
            }
            write(); // Empty line between each group of instruction
        }
    }

    /**
     * Write the end of the main function.
     */
    private void end() {
        write("ret i32 0;\n}");
    }

    private void Read(AbstractSyntaxTree AST) {
        String varName = AST.childAt(0).getLabel().getValue().toString();

        if (!variables.contains(varName)) {
            write("%" + varName + " = alloca i32");
            variables.add(varName);
        }

        write("%" + unnamedVar + " = call i32 @readInt()");
        write("store i32 %" + unnamedVar + ", i32* %" + varName);
        unnamedVar++;
    }

    private void Print(AbstractSyntaxTree AST) {
        String varName = AST.childAt(0).getLabel().getValue().toString();
        write("%" + unnamedVar + " = load i32, i32* %" + varName);
        write("call void @println(i32 %" + unnamedVar + ")");
        unnamedVar++;
    }

    private void If(AbstractSyntaxTree AST, int index) {
        if (AST.numberOfChildren() < 2) {
            return; // If the code of the if is empty we don't write it because it's useless
        }
        ifIndex++;

        boolean withElse = AST.getChildren().size() == 3; // True if there is an else

        operate(AST.childAt(0));

        int cond = unnamedVar;
        unnamedVar++;
        if (withElse) {
            write("\nbr i1 %" + cond + ", label %ifCode" + index + ", label %elseCode" + index);
        } else {
            write("\nbr i1 %" + cond + ", label %ifCode" + index + ", label %endif" + index);
        }
        write("ifCode" + index + ":");

        Code(AST.childAt(1));

        write("br label %endif" + index);

        if (withElse) {
            write("elseCode" + index + ":");

            Code(AST.childAt(2));

            write("br label %endif" + index);
        }

        write("endif" + index + ":");
    }

    private void For(AbstractSyntaxTree AST, int index) {
        if (AST.numberOfChildren() < 5) {
            return; // If the code of the for loop is empty we don't write it because it's useless
        }
        forIndex++;

        /*
        Creation of the variable of the for loop
         */
        String varName = Assign(AST);

        write("br label %forCond" + index);
        write("forCond" + index + ":");

        operate(AST.childAt(0)); // Load i into an unnamed variable
        int i = unnamedVar;
        unnamedVar++;

        /*
        Comparison of the variable with the maximal value
         */
        AbstractSyntaxTree maxValue = AST.childAt(3);
        boolean maxValueIsNumber = maxValue.getLabel().getType() == LexicalUnit.NUMBER;
        int m;
        if (maxValueIsNumber) {
            m = (int) maxValue.getLabel().getValue();
        }
        else {
            operate(maxValue);
            m = unnamedVar;
            unnamedVar++;
        }

        int cond = unnamedVar;
        unnamedVar++;
        StringBuilder llCode = new StringBuilder("%" + cond + " = icmp slt i32 %" + i + ", ");
        if (!maxValueIsNumber) {
            llCode.append("%");
        }
        llCode.append(m);

        write(llCode.toString());

        /*
        Beginning of the inner code of the for loop
         */
        write("\nbr i1 %" + cond + ", label %forCode" + index + ", label %endfor" + index);
        write("forCode" + index + ":");

        Code(AST.childAt(4));

        /*
        Increment the value of the variable
         */
        AbstractSyntaxTree increment = AST.childAt(2);
        boolean incrementIsNumber = increment.getLabel().getType() == LexicalUnit.NUMBER;
        if (incrementIsNumber) {
            m = (int) increment.getLabel().getValue();
        }
        else {
            operate(increment);
            m = unnamedVar;
            unnamedVar++;
        }

        int p = unnamedVar;
        llCode = new StringBuilder("%" + p + " = add i32 %" + i + ", ");
        if (!incrementIsNumber) {
            llCode.append("%");
        }
        llCode.append(m);

        write(llCode.toString());

        write("store i32 %" + p + ", i32* %" + varName);
        unnamedVar++;

        /*
        End of the for loop
         */
        write("br label %forCond" + index);
        write("endfor" + index + ":");
    }

    private void While(AbstractSyntaxTree AST, int index) {
        if (AST.numberOfChildren() < 2) {
            return; // If the code of the while loop is empty we don't write it because it's useless
        }
        whileIndex++;

        write("br label %whileCond" + index);
        write("whileCond" + index + ":");

        operate(AST.childAt(0));

        int cond = unnamedVar;
        unnamedVar++;
        write("\nbr i1 %" + cond + ", label %whileCode" + index + ", label %endwhile" + index);
        write("whileCode" + index + ":");

        Code(AST.childAt(1));

        write("br label %whileCond" + index);
        write("endwhile" + index + ":");
    }

    /**
     * Call the corresponding operation with the good operator.
     * If the AST is a VarName, we load it.
     *
     * @param AST the AST corresponding to the operator
     */
    private void operate(AbstractSyntaxTree AST) {
        switch (AST.getLabel().getType()) {
            case VARNAME:
                write("%" + unnamedVar + " = load i32, i32* %" + AST.getLabel().getValue());
                break;
            case PLUS:
                operation(AST, "add");
                break;
            case MINUS:
                operation(AST, "sub");
                break;
            case TIMES:
                operation(AST, "mul");
                break;
            case DIVIDE:
                operation(AST, "sdiv");
                break;
            case AND:
                operation(AST, "and", true);
                break;
            case OR:
                operation(AST, "or", true);
                break;
            case NOT:
                operation(AST, "not", true);
                break;
            case EQUAL:
                operation(AST, "icmp eq");
                break;
            case DIFFERENT:
                operation(AST, "icmp ne");
                break;
            case GREATER:
                operation(AST, "icmp sgt");
                break;
            case GREATER_EQUAL:
                operation(AST, "icmp sge");
                break;
            case SMALLER:
                operation(AST, "icmp slt");
                break;
            case SMALLER_EQUAL:
                operation(AST, "icmp sle");
                break;
        }
    }

    private String Assign(AbstractSyntaxTree AST) {
        String varName = AST.childAt(0).getLabel().getValue().toString();

        if (!variables.contains(varName)) {
            write("%" + varName + " = alloca i32");
            variables.add(varName);
        }

        AbstractSyntaxTree rightTerm = AST.childAt(1);
        boolean rightIsNumber = rightTerm.getLabel().getType() == LexicalUnit.NUMBER;
        int m;
        if (rightIsNumber) {
            m = (int) rightTerm.getLabel().getValue();
        }
        else {
            operate(rightTerm);
            m = unnamedVar;
            unnamedVar++;
        }

        StringBuilder llCode = new StringBuilder("store i32 ");

        if (!rightIsNumber) {
            llCode.append("%");
        }
        llCode.append(m);
        llCode.append(", i32* %");
        llCode.append(varName);

        write(llCode.toString());

        return varName;
    }

    /**
     * Function handling operations between integers given the AST corresponding to the operator
     * and the string for the operator in the LLVM IR language.
     *
     * @param AST the AST corresponding to the operator
     * @param operator the operator in the LLVM IR language
     */
    private void operation(AbstractSyntaxTree AST, String operator) {
        operation(AST, operator, false);
    }

    /**
     * Function handling operations given the AST corresponding to the operator
     * and the string for the operator in the LLVM IR language.
     *
     * @param AST the AST corresponding to the operator
     * @param operator the operator in the LLVM IR language
     * @param isBoolean true if the type of the operand is a boolean, false if it's a integer
     */
    private void operation(AbstractSyntaxTree AST, String operator, boolean isBoolean) {
        int size = 2;
        if (operator.equals("not")) {
            isBoolean = true;
            size = 1;
        }

        boolean[] isNumber = new boolean[size];
        int[] variables = new int[size];
        for (int i = 0; i < size; i++) {
            AbstractSyntaxTree term = AST.childAt(i);

            isNumber[i] = term.getLabel().getType() == LexicalUnit.NUMBER;
            if (isNumber[i]) {
                variables[i] = (int) term.getLabel().getValue();
            }
            else {
                operate(term);
                variables[i] = unnamedVar;
                unnamedVar++;
            }
        }

        StringBuilder llCode;
        int p = unnamedVar;
        int n = variables[0];
        int type = isBoolean ? 1 : 32;

        if (operator.equals("not")) {
            llCode = new StringBuilder("%" + p + " = sub i" + type + " 1, ");

            if (!isNumber[0]) {
                llCode.append("%");
            }
            llCode.append(n);
        }
        else {
            llCode = new StringBuilder("%" + p + " = " + operator + " i" + type + " ");

            if (!isNumber[0]) {
                llCode.append("%");
            }
            llCode.append(n);

            int m = variables[1];

            llCode.append(", ");
            if (!isNumber[1]) {
                llCode.append("%");
            }
            llCode.append(m);
        }

        write(llCode.toString());
    }

    /**
     * Write the string given in the code in a file or on the stdout
     */
    private void write(String llCode) {
        if(toFile) {
            try {
                Files.write(outputFile, Collections.singleton(llCode), StandardCharsets.UTF_8,
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println(llCode);
        }
    }

    /**
     * Write an empty line in the code
     */
    private void write() {
        write("");
    }
}
