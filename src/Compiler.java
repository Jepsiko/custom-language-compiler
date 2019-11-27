import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class Compiler {
    private Path outputFile;
    private final boolean toFile;

    private int unnamedVar;

    public Compiler() {
        toFile = false;
        unnamedVar = 0;
    }

    public Compiler(String outputFilename) {
        if (!outputFilename.equals("")) {
            toFile = true;
            outputFile = Paths.get(outputFilename);
        }
        else {
            toFile = false;
        }
        unnamedVar = 0;
    }

    public void compile(AbstractSyntaxTree AST) throws IOException {
        Program(AST);
    }

    private void Program(AbstractSyntaxTree AST) throws IOException {
        begin();
        Code(AST.get(1));
        end();
    }

    private void begin() throws IOException {
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

    private void Code(AbstractSyntaxTree AST) throws IOException {
        for (AbstractSyntaxTree child : AST.getChildren()) {
            switch (child.getLabel().getValue().toString()) {
                case "<Assign>":
                    Assign(child);
                    break;
                case "<Read>":
                    Read(child);
                    break;
                case "<Print>":
                    Print(child);
                    break;
                case "<While>":
                    While(child);
                    break;
            }
            write(""); // Empty line between each group of instruction
        }
    }

    private void end() throws IOException {
        write("}");
    }

    private void Read(AbstractSyntaxTree AST) throws IOException {
        String varName = AST.get(0).getLabel().getValue().toString();
        write("%" + varName + " = call i32 @readInt()");
    }

    private void Print(AbstractSyntaxTree AST) throws IOException {
        String varName = AST.get(0).getLabel().getValue().toString();
        write("call void @println(i32 %" + varName + ")");
    }

    private void While(AbstractSyntaxTree AST) throws IOException {
        Cond(AST.get(0));
        Code(AST.get(1));
    }

    private void Cond(AbstractSyntaxTree AST) {

    }

    private void Assign(AbstractSyntaxTree AST) throws IOException {
        String varName = AST.get(0).getLabel().getValue().toString();

        write("%" + varName + " = alloca i32");

        ExprArith(AST.get(1).get(0));

        write("store i32 %" + unnamedVar + ", i32* %" + varName);
        unnamedVar++;
    }

    private void ExprArith(AbstractSyntaxTree AST) throws IOException {
        switch (AST.getLabel().getType()) {
            case NUMBER:
                write("%" + unnamedVar + " = i32 " + AST.getLabel().getValue());
                break;
            case VARNAME:
                write("%" + unnamedVar + " = load i32, i32* " + AST.getLabel().getValue());
                break;
            case PLUS:
                Plus(AST);
                break;
            case MINUS:
                Minus(AST);
                break;
            case TIMES:
                Times(AST);
                break;
            case DIVIDE:
                Divide(AST);
                break;
        }
    }

    private void operation(AbstractSyntaxTree AST, String operator) throws IOException {
        ExprArith(AST.get(0));
        int n = unnamedVar;

        unnamedVar++;
        ExprArith(AST.get(1));
        int m = unnamedVar;

        unnamedVar++;
        int p = unnamedVar;
        write("%" + p + " = " + operator + " i32 %" + n + ", %" + m);
    }

    private void Plus(AbstractSyntaxTree AST) throws IOException {
        operation(AST, "add");
    }

    private void Minus(AbstractSyntaxTree AST) throws IOException {
        operation(AST, "sub");
    }

    private void Divide(AbstractSyntaxTree AST) throws IOException {
        operation(AST, "sdiv");
    }

    private void Times(AbstractSyntaxTree AST) throws IOException {
        operation(AST, "mul");
    }

    private void write(String llCode) throws IOException {
        if(toFile) {
            Files.write(outputFile, Collections.singleton(llCode + "\n"), StandardCharsets.UTF_8);
        }
        else {
            System.out.println(llCode);
        }
    }
}
