import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main of the Code, read the code and call the parser
 */
public class Main{
    public static void main(String[] args) throws IOException, SecurityException, InterruptedException {

        if (args.length == 0)
            System.exit(-1) ;

        boolean verbose = false;
        boolean execute = false;
        String texFile = "";
        String outputFile = "";

        int i = 0;
        String arg = args[i];
        if (arg.equals("-exec")) {
            execute = true;
            outputFile = "temp.ll";
            i++;
            arg = args[i];
        }
        FileReader Source = new FileReader(arg);

        i++;
        while (i < args.length) {
            arg = args[i];

            switch (arg) {
                case "-v":
                    verbose = true;
                    break;
                case "-wt":
                    texFile = args[++i];
                    break;
                case "-o":
                    outputFile = args[++i];
                    break;
            }
            i++;
        }

        final LexicalAnalyzer analyzer = new LexicalAnalyzer(Source);
        List<Symbol> symbols = new ArrayList<>();

        Symbol symbol;
        while(!(symbol = analyzer.nextToken()).getType().equals(LexicalUnit.END_OF_STREAM)){
            symbols.add(symbol);
        }

        Parser parser = new Parser(symbols, verbose);
        ParseTree parseTree = parser.parse();

        if (parseTree == null)
            return;

        if (!texFile.equals("")) {
            Path file = Paths.get(texFile);
            Files.write(file, Collections.singleton(parseTree.toLaTeX()), StandardCharsets.UTF_8);
        }

        AbstractSyntaxTree AST = new AbstractSyntaxTree(parseTree);

        if (!texFile.equals("")) {
            texFile = texFile.replace(".", "_AST.");
            Path file = Paths.get(texFile);
            Files.write(file, Collections.singleton(AST.toLaTeX()), StandardCharsets.UTF_8);
        }

        Compiler compiler = new Compiler(outputFile);
        compiler.compile(AST);

        if (execute) {
            String byteCodeFile = outputFile.substring(0, outputFile.length()-2) + "bc";
            Process process = new ProcessBuilder("llvm-as", outputFile, "-o=" + byteCodeFile).inheritIO().start();
            process.waitFor();

            process = new ProcessBuilder("lli", byteCodeFile).inheritIO().start();
            process.waitFor();

            File tempFile = new File("temp.ll");
            tempFile.delete();
            tempFile = new File("temp.bc");
            tempFile.delete();
        }
    }
}