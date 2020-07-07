/*******************************************************************************
 * Copyright (c) 2017, SEMERU
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 *******************************************************************************/
package correlation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/**
 * {Insert class description here}
 *
 * @author Carlos Bernal
 */
public class Similarities {

    public static void main(String[] args) {
        // 15,25,50
        int topics = 15;
        // file,function
        String type = "file";

        // IdDocument Topic1 Topic2 ... TopicN
        String requirements = "/Users/semeru/Documents/SEMERU/Cisco/resultsFilter/" + type + "/" + topics
                + "/originalRequirementsFL_composition.txt";
        String files = "/Users/semeru/Documents/SEMERU/Cisco/resultsFilter/" + type + "/" + topics + "/orSRFL.txt";
        // String methods = "cisco/methods.txt";
        // String requirements = "cisco/requirements.txt";
        // String files = "cisco/files.txt";
        // String methods = "cisco/methods.txt";
        String output = "/Users/semeru/Documents/SEMERU/Cisco/resultsFilter/" + type + "/" + topics + "/orResult.txt";

        // Compute similarities between files and requirements
        computeSimilarities(requirements, files, output, topics);

    }

    /**
     * @param requirements
     * @param documents
     * @param output
     * @param topics
     */
    private static void computeSimilarities(String requirements, String documents, String output, int topics) {
        try (BufferedReader brReq = new BufferedReader(new FileReader(requirements));
                BufferedReader brDoc = new BufferedReader(new FileReader(documents))) {

            String sCurrentReq;
            String sCurrentDoc;
            HashMap<String, double[]> mapReq = new HashMap<String, double[]>();
            HashMap<String, double[]> mapDoc = new HashMap<String, double[]>();
            while ((sCurrentReq = brReq.readLine()) != null) {
                processLine(sCurrentReq, mapReq, topics);
            }
            while ((sCurrentDoc = brDoc.readLine()) != null) {
                processLine(sCurrentDoc, mapDoc, topics);
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(output));

            // Compute similarity Requirement VS. Document
            for (Entry<String, double[]> reqItem : mapReq.entrySet()) {
                for (Entry<String, double[]> docItem : mapDoc.entrySet()) {
                    String line = reqItem.getKey() + " " + docItem.getKey() + " ";
                    double total = 0;
                    // double totalA = 0;
                    // double totalB = 0;
                    for (int i = 0; i < reqItem.getValue().length; i++) {
                        double a = reqItem.getValue()[i];
                        double b = docItem.getValue()[i];
                        double val = Math.sqrt(a) - Math.sqrt(b);
                        total += val * val;
                        // totalA += a;
                        // totalB += b;
                    }
                    total = Math.sqrt(total) / Math.sqrt(2); //danaderp>>Normalization Step
                    // System.out.println("a:" + totalA + " - b:" + totalB);
                    line += total;
                    writer.write(line + "\n");
                    System.out.println(line);
                }
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param map
     * @param topics
     * @param
     * @return
     */
    private static void processLine(String currentLine, HashMap<String, double[]> map, int topics) {
        String name = "";
        double[] probabilities = new double[topics];
        StringTokenizer tokenizer = new StringTokenizer(currentLine, "\t");
        for (int i = 0; tokenizer.hasMoreTokens(); i++) {
            String token = tokenizer.nextToken();
            if (i != 0) {
                int index = Integer.parseInt(token);
                double probability = Double.parseDouble(tokenizer.nextToken());
                probabilities[index] = probability;
            } else {
                name = token;
            }
        }
        map.put(name, probabilities);
    }

}
