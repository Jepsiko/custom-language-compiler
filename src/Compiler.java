import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Compiler {
    private Path outputFile;
    private final boolean toFile;

    private int unnamedVar;
    private int whileIndex;
    private int forIndex;
    private int ifIndex;

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

    public void compile(AbstractSyntaxTree AST) {
        Program(AST);
    }

    private void Program(AbstractSyntaxTree AST) {
        begin();
        Code(AST.get(1));
        end();
    }

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
                "\tbr label %loop\n" +
                "loop:\n" +
                "\t%0 = call i32 @getchar()\n" +
                "\t%1 = sub i32 %0, 48\n" +
                "\n" +
                "\t%2 = icmp sge i32 %1, 0\n" +
                "\t%3 = icmp sle i32 %1, 9\n" +
                "\t%4 = and i1 %2, %3\n" +
                "\tbr i1 %4, label %continue, label %exit\n" +
                "continue:\n" +
                "\t%5 = load i32, i32* %res\n" +
                "\t%6 = mul i32 %5, 10\n" +
                "\t%7 = add i32 %6, %1\n" +
                "\tstore i32 %7, i32* %res\n" +
                "\n" +
                "\tbr label %loop\n" +
                "exit:\n" +
                "\t%8 = load i32, i32* %res\n" +
                "\tret i32 %8\n" +
                "}\n" +
                "\n" +
                "\n" +
                "define i32 @main() {\n" +
                "entry:\n");
    }

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
            write(""); // Empty line between each group of instruction
        }
    }

    private void end() {
        write("ret i32 0;\n}");
    }

    private void Read(AbstractSyntaxTree AST) {
        String varName = AST.get(0).getLabel().getValue().toString();

        if (!variables.contains(varName)) {
            write("%" + varName + " = alloca i32");
            variables.add(varName);
        }

        write("%" + unnamedVar + " = call i32 @readInt()");
        write("store i32 %" + unnamedVar + ", i32* %" + varName);
        unnamedVar++;
    }

    private void Print(AbstractSyntaxTree AST) {
        String varName = AST.get(0).getLabel().getValue().toString();
        write("%" + unnamedVar + " = load i32, i32* %" + varName);
        write("call void @println(i32 %" + unnamedVar + ")");
        unnamedVar++;
    }

    private void If(AbstractSyntaxTree AST, int index) {
        ifIndex++;

        boolean withElse = AST.getChildren().size() == 3; // True if there is an else

        Cond(AST.get(0));

        int cond = unnamedVar-1;
        if (withElse) {
            write("\nbr i1 %" + cond + ", label %ifCode" + index + ", label %elseCode" + index);
        } else {
            write("\nbr i1 %" + cond + ", label %ifCode" + index + ", label %endif" + index);
        }
        write("ifCode" + index + ":");

        Code(AST.get(1));

        write("br label %endif" + index);

        if (withElse) {
            write("elseCode" + index + ":");

            Code(AST.get(2));

            write("br label %endif" + index);
        }

        write("endif" + index + ":");
    }

    private void For(AbstractSyntaxTree AST, int index) {
        forIndex++;

        /*
        Creation of the variable of the for loop
         */
        String varName = Assign(AST);

        write("br label %forCond" + index);
        write("forCond" + index + ":");

        ExprArith(AST.get(0)); // Load i into an unnamed variable
        int i = unnamedVar;
        unnamedVar++;

        /*
        Comparison of the variable with the maximal value
         */
        AbstractSyntaxTree maxValue = AST.get(3).get(0);
        boolean maxValueIsNumber = maxValue.getLabel().getType() == LexicalUnit.NUMBER;
        int m;
        if (maxValueIsNumber) {
            m = (int) maxValue.getLabel().getValue();
        }
        else {
            ExprArith(maxValue);
            m = unnamedVar;
            unnamedVar++;
        }

        int cond = unnamedVar;
        StringBuilder llCode = new StringBuilder("%" + cond + " = icmp slt i32 %" + i + ", ");
        if (!maxValueIsNumber) {
            llCode.append("%");
        }
        llCode.append(m);

        write(llCode.toString());
        unnamedVar++;

        /*
        Beginning of the inner code of the for loop
         */
        write("\nbr i1 %" + cond + ", label %forCode" + index + ", label %endfor" + index);
        write("forCode" + index + ":");

        Code(AST.get(4));

        /*
        Increment the value of the variable
         */
        AbstractSyntaxTree increment = AST.get(2).get(0);
        boolean incrementIsNumber = increment.getLabel().getType() == LexicalUnit.NUMBER;
        if (incrementIsNumber) {
            m = (int) increment.getLabel().getValue();
        }
        else {
            ExprArith(increment);
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

        write("store i32 %" + unnamedVar + ", i32* %" + varName);
        unnamedVar++;

        /*
        End of the for loop
         */
        write("br label %forCond" + index);
        write("endfor" + index + ":");
    }

    private void While(AbstractSyntaxTree AST, int index) {
        whileIndex++;

        write("br label %whileCond" + index);
        write("whileCond" + index + ":");

        Cond(AST.get(0));

        int cond = unnamedVar-1;
        write("\nbr i1 %" + cond + ", label %whileCode" + index + ", label %endwhile" + index);
        write("whileCode" + index + ":");

        Code(AST.get(1));

        write("br label %whileCond" + index);
        write("endwhile" + index + ":");
    }

    private void Cond(AbstractSyntaxTree AST) {
        CondAnd(AST.get(0));
        int n = unnamedVar-1;

        for (int i = 1; i < AST.getChildren().size(); i++) {
            CondAnd(AST.get(i));
            int m = unnamedVar-1;

            int p = unnamedVar;
            write("%" + p + " = or i1 %" + n + ", %" + m);

            unnamedVar++;
            n = p;
        }
    }

    private void CondAnd(AbstractSyntaxTree AST) {
        SimpleCond(AST.get(0).get(0));
        int n = unnamedVar;

        unnamedVar++;
        for (int i = 1; i < AST.getChildren().size(); i++) {
            SimpleCond(AST.get(i).get(0));
            int m = unnamedVar;

            unnamedVar++;
            int p = unnamedVar;
            write("%" + p + " = and i1 %" + n + ", %" + m);

            unnamedVar++;
            n = p;
        }
    }

    private void SimpleCond(AbstractSyntaxTree AST) {
        switch (AST.getLabel().getType()) {
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
        String varName = AST.get(0).getLabel().getValue().toString();

        if (!variables.contains(varName)) {
            write("%" + varName + " = alloca i32");
            variables.add(varName);
        }

        AbstractSyntaxTree rightTerm = AST.get(1).get(0);
        boolean rightIsNumber = rightTerm.getLabel().getType() == LexicalUnit.NUMBER;
        int m;
        if (rightIsNumber) {
            m = (int) rightTerm.getLabel().getValue();
        }
        else {
            ExprArith(rightTerm);
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

    private void ExprArith(AbstractSyntaxTree AST) {
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
        }
    }

    private void operation(AbstractSyntaxTree AST, String operator) {
        AbstractSyntaxTree leftTerm = AST.get(0);
        boolean leftIsNumber = leftTerm.getLabel().getType() == LexicalUnit.NUMBER;
        int n;
        if (leftIsNumber) {
            n = (int) leftTerm.getLabel().getValue();
        }
        else {
            ExprArith(leftTerm);
            n = unnamedVar;
            unnamedVar++;
        }

        AbstractSyntaxTree rightTerm = AST.get(1);
        boolean rightIsNumber = rightTerm.getLabel().getType() == LexicalUnit.NUMBER;
        int m;
        if (rightIsNumber) {
            m = (int) rightTerm.getLabel().getValue();
        }
        else {
            ExprArith(rightTerm);
            m = unnamedVar;
            unnamedVar++;
        }

        int p = unnamedVar;

        StringBuilder llCode = new StringBuilder("%" + p + " = " + operator + " i32 ");
        if (!leftIsNumber) {
            llCode.append("%");
        }
        llCode.append(n);

        llCode.append(", ");
        if (!rightIsNumber) {
            llCode.append("%");
        }
        llCode.append(m);

        write(llCode.toString());
    }

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
}
