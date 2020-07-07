package io.jenkins.plugins.traceability.helper;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread is needed to avoid getting stuck with a long output on the terminal
 *
 * http://juxed.blogspot.com/2015/09/java-processbuilder-has-32kb-buffer.html
 *
 */
public class ThreadedStreamHandler extends Thread {

    private static final Logger LOGGER = Logger.getLogger(ThreadedStreamHandler.class.getCanonicalName());

    InputStream inputStream;
    OutputStream outputStream;
    PrintWriter printWriter;
    StringBuilder outputBuffer = new StringBuilder();

    ThreadedStreamHandler(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    ThreadedStreamHandler(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.printWriter = new PrintWriter(outputStream);
    }

    public void run() {

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (outputBuffer.length() > 0) {
                    outputBuffer.append('\n');
                }
                outputBuffer.append(line);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, t.getMessage(), t);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
            }
        }
    }

    public StringBuilder getOutputBuffer() {
        return outputBuffer;
    }
}
