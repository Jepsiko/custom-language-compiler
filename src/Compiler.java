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
        write("define i32 @main() {\n" +
                "entry:\n");
    }

    private void Code(AbstractSyntaxTree AST) throws IOException {
        for (AbstractSyntaxTree child : AST.getChildren()) {
            if (child.getLabel().getValue() == "<Assign>") {
                Assign(child);
            }
            else if (child.getLabel().getValue() == "") {

            }
        }
    }

    private void end() throws IOException {
        write("}");
    }

    private void Assign(AbstractSyntaxTree AST) throws IOException {
        String varName = AST.get(0).getLabel().getValue().toString();

        write("%" + varName + "= alloca i32");

        ExprArith(AST.get(1).get(0));

        write("store i32 %" + unnamedVar + ", i32* %" + varName + "\n");
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
