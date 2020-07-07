import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by david on 8/24/17.
 */
public class SrcMLInstrumentation {

    public static void main(String[] args) {
        try{
            String userPath = System.getProperty("user.dir");
            String srcMLPath = userPath + "/srcML/";
            String cFileName = userPath + "/sua/srcLibEst/est.c";
            ProcessBuilder processBuilder = new ProcessBuilder("srcml", cFileName);
            processBuilder.directory(new File(srcMLPath));

            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            int i;
            StringBuilder xmlData = new StringBuilder();
            while ((i = inputStream.read()) != -1)
            {
                xmlData.append((char) i);
            }
            System.out.println(xmlData.toString());
        }
        catch (IOException e){
            System.out.print("Error:" + e.getMessage());
        }
    }
}
