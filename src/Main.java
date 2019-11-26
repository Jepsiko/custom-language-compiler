import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    public static void main(String[] args) throws FileNotFoundException, IOException, SecurityException{

        if (args.length == 0)
            System.exit(-1) ;

        boolean verbose = false;
        String texFile = "";
        String outputFile = "";
        int i = 0;
        while (i < args.length-1) {
            String arg = args[i];

            if (arg.equals("-v")) {
                verbose = true;
            }
            else if (arg.equals("-wt")) {
                texFile = args[++i];
            }
            else if (arg.equals("-o")) {
                outputFile = args[++i];
            }
            i++;
        }

        FileReader Source = new FileReader(args[i]);


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

        AbstractSyntaxTree AST = ParseTree.toAST(parseTree);

        if (!texFile.equals("")) {
            texFile = texFile.replace(".", "_AST.");
            Path file = Paths.get(texFile);
            Files.write(file, Collections.singleton(AST.toLaTeX()), StandardCharsets.UTF_8);
        }

        Compiler compiler = new Compiler(outputFile);
        compiler.compile(AST);
    }
}