package Analysis.language.python;



import org.treesitter.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PythonParseCache {

    private final TSParser parser;
    private final Map<Path, TSTree> cache = new ConcurrentHashMap<>();

    public PythonParseCache() {
        parser = new TSParser();
        parser.setLanguage(new TreeSitterPython());
    }

    public TSTree get(Path filePath) {

        Path normalized = filePath.toAbsolutePath().normalize();

        return cache.computeIfAbsent(normalized, path -> {
            try {
                String source = Files.readString(path);
                return parser.parseString(null, source);
            } catch (Exception e) {
                return null;
            }
        });
    }

    public void invalidate(Path filePath) {
        cache.remove(filePath.toAbsolutePath().normalize());
    }

    public void clear() {
        cache.clear();
    }
}
