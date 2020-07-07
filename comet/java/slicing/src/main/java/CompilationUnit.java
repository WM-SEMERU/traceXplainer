import java.io.File;

/**
 * Created by david on 8/28/17.
 */
public class CompilationUnit {
    private File srcFile;

    public CompilationUnit(File srcFile) {
        this.srcFile = srcFile;
    }

    public File getSrcFile() {
        return srcFile;
    }

    public void setSrcFile(File srcFile) {
        this.srcFile = srcFile;
    }
}
