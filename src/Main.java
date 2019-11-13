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

public class Main{
    public static void main(String[] args) throws FileNotFoundException, IOException, SecurityException{

        if (args.length == 0)
            System.exit(-1) ;

        boolean verbose = false;
        String texFile = "";
        int i = 0;
        while (i < args.length-1) {
            String arg = args[i];

            if (arg.equals("-v")) {
                verbose = true;
            }
            else if (arg.equals("-wt")) {
                texFile = args[++i];
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

        if (!texFile.equals("") && parseTree != null) {
            Path file = Paths.get(texFile);
            Files.write(file, Collections.singleton(parseTree.toLaTeX()), StandardCharsets.UTF_8);
        }
    }
}