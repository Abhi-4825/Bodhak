package Analysis;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;

import java.nio.file.Path;
import java.util.List;

public interface LanguageAnalyzer {
    List<ClassInfo> analyze(List<Path> files) throws Exception;
}
