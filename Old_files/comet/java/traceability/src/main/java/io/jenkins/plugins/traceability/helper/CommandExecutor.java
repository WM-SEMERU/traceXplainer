package io.jenkins.plugins.traceability.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CommandExecutor {

    private File path = null;
    private Map<String, String> environment = null;
    private List<String> command;
    private ThreadedStreamHandler inputStreamHandler;
    private ThreadedStreamHandler errorStreamHandler;

    public CommandExecutor(List<String> command) {
        this(null, command);
    }

    public CommandExecutor(File path, List<String> command) {
        this(path, null, command);
    }

    public CommandExecutor(File path, Map<String, String> environment, List<String> command) {
        this.path = path;
        this.environment = environment;
        this.command = command;
    }

    public void executeCommand()
            throws IOException, InterruptedException {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            if (path != null)
                pb.directory(path);
            if (environment != null)
                pb.environment().putAll(environment);
            Process process = pb.start();

            OutputStream stdOutput = process.getOutputStream();
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();
            inputStreamHandler = new ThreadedStreamHandler(inputStream, stdOutput);
            errorStreamHandler = new ThreadedStreamHandler(errorStream);

            inputStreamHandler.start();
            errorStreamHandler.start();
            process.waitFor();
            inputStreamHandler.interrupt();
            errorStreamHandler.interrupt();
            inputStreamHandler.join();
            errorStreamHandler.join();
        } catch (IOException e) {
            throw e;
        } catch (InterruptedException e) {
            throw e;
        }
    }

    public StringBuilder getStandardOutput() {
        return inputStreamHandler.getOutputBuffer();
    }

    public StringBuilder getErrorOutput() {
        return errorStreamHandler.getOutputBuffer();
    }

    public static void main(String [] args){

        CommandExecutor executor = new CommandExecutor(Arrays.asList("python3","/Users/cbernalc/Documents/CISCO/Traceability/Projects/Security-Traceability/python/Unified-traceability/unified_traceability/CausalityFacade.py"));
        try {
            executor.executeCommand();
            StringBuilder standardOutput = executor.getStandardOutput();
//            StringBuilder errorOutput = executor.getErrorOutput();
            System.out.print(standardOutput);
//            System.out.print(errorOutput);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
