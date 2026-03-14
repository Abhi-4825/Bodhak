package Analysis.language.python;

import Analysis.LanguageAnalyzer;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import org.treesitter.TSNode;
import org.treesitter.TSParser;
import org.treesitter.TSTree;
import org.treesitter.TreeSitterPython;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PyhtonAnalyzer implements LanguageAnalyzer {
    @Override
    public List<ClassInfo> analyze(List<Path> files) throws Exception {

        TSParser parser = new TSParser();
        parser.setLanguage(new TreeSitterPython());
        List<ClassInfo> classes = new ArrayList<>();
        for (Path path : files) {
            File file = path.toFile();
            String source= Files.readString(path);

            TSTree tree= parser.parseString(null,source);
            TSNode root=tree.getRootNode();




        }


        return List.of();
    }
}
