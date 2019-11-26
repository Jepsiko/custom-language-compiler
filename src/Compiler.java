import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class Compiler {
    private Path outputFile;
    private final boolean toFile;

    public Compiler() {
        toFile = false;
    }

    public Compiler(String outputFilename) throws IOException {
        if (!outputFilename.equals("")) {
            toFile = true;
            outputFile = Paths.get(outputFilename);
            Files.write(outputFile, Collections.singleton(""), StandardCharsets.UTF_8);
        }
        else {
            toFile = false;
        }
    }

    public void compile(AbstractSyntaxTree AST) {
        List<AbstractSyntaxTree> path = AST.traversal();

        for (AbstractSyntaxTree elem : path) {
            System.out.println(elem.getLabel().getValue());
        }
    }
}
