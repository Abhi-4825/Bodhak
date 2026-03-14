package Analysis;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import org.treesitter.TSNode;

public interface ClassInfoBuilder {

    ClassInfo buildClassInfo(TSNode classNode) throws Exception;
}
