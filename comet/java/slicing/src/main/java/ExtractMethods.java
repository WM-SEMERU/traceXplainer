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


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * {Insert class description here}
 *
 * @author Carlos Bernal
 */
public class ExtractMethods {

    public static void main(String[] args) {


        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            String userPath = System.getProperty("user.dir");
            String cFileName = userPath + "/srcXML/est_server_http.xml";
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(cFileName);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/unit");

            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node item = nodes.item(i);
                insertInstrument(item, doc);
                // break;
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(userPath + "/est_server_httpM.xml"));
            transformer.transform(source, result);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }



    public static Node insertNodeFromTemplate(String FunctionName){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Node expr_stmt =  null;
        try {
            String userPath = System.getProperty("user.dir");
            String cFileName = userPath + "/nodito.xml";
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(cFileName);

            expr_stmt = doc.getFirstChild();
            Node literal = doc.getElementsByTagName("literal").item(0);
            literal.setTextContent("NADER TAG: " + FunctionName);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return expr_stmt;
    }

    public static void removeChilds(Node node) {
        while (node.hasChildNodes())
            node.removeChild(node.getFirstChild());
    }


    private static void insertInstrument(Node node, Document doc){


        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            //Extract Child
            if ( node.getChildNodes().item(i).getNodeName().equalsIgnoreCase("function") ) {

                // write method
                NodeList childNodesTypes = node.getChildNodes().item(i).getChildNodes();
                Node childNodesTypesIntrument;
                String nameTemp = ""; //Function Name

                for (int j = 0; j < childNodesTypes.getLength(); j++) {

                    if ((childNodesTypes.item(j)).getNodeName().equals("name")) {
                        nameTemp = ((Element) childNodesTypes.item(j)).getTextContent().replace("\n", "");
                    }


                    if ((childNodesTypes.item(j)).getNodeName().equals("block")) {

                        Element expr_stmt = (Element) doc.importNode(insertNodeFromTemplate(nameTemp),true);
                        childNodesTypesIntrument = childNodesTypes.item(j).getChildNodes().item(1);
                        childNodesTypes.item(j).insertBefore(expr_stmt, childNodesTypesIntrument);

                    }
                }

            }
        }
    }

    /**
     * @param node
     * @param output
     */
    private static void processNodeUnit(Node node, String output) {
        Element element = (Element) node;
        String file = element.getAttribute("filename");
        List<Node> nodes = new ArrayList<>();
        file = file.substring(file.lastIndexOf("/") + 1, file.length());

        String comment = "";
        String function = "";


        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            //Extract Child
            Node codeNode = node.getChildNodes().item(i);
            //The child should be a function_decl (?) or function
            //if (codeNode.getNodeName().equalsIgnoreCase("function_decl")
            //        || codeNode.getNodeName().equalsIgnoreCase("function")) {
            if ( codeNode.getNodeName().equalsIgnoreCase("function") ) {

                if (i != 0) {
                    if (node.getChildNodes().item(i - 2).getNodeName().equalsIgnoreCase("comment")) {
                        nodes.add(node.getChildNodes().item(i - 2));
                        comment = node.getChildNodes().item(i - 2).getTextContent() + "\n";
                    }
                }
                nodes.add(codeNode);
                function = codeNode.getTextContent() + "\n";

                // write method
                NodeList childNodesTypes = codeNode.getChildNodes();
                String nameTemp = ""; //Node name
                String parametersTemp = ""; //Node parameters
                String blockTemp = ""; //Node block

                for (int j = 0; j < childNodesTypes.getLength(); j++) {
                    if ((childNodesTypes.item(j)).getNodeName().equals("name")) {
                        nameTemp = ((Element) childNodesTypes.item(j)).getTextContent().replace("\n", "");
                        // System.out.println(nameTemp);
                    }
                    if ((childNodesTypes.item(j)).getNodeName().equals("parameter_list")) {
                        parametersTemp = ((Element) childNodesTypes.item(j)).getTextContent().replace("*", "")
                                .replace("\n", "").replaceAll("\\s+", " ");
                        // System.out.println(parametersTemp);
                    }
                    if ((childNodesTypes.item(j)).getNodeName().equals("block")) {
                        blockTemp = ((Element) childNodesTypes.item(j)).getTextContent().replace("*", "")
                                .replace("\n", "").replaceAll("\\s+", " ");
                        // System.out.println(parametersTemp);
                    }
                }

                System.out.println("---------------");
                // System.out.println(comment);
                System.out.println(nameTemp + parametersTemp + blockTemp);
                System.out.println("---------------");
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(output + "method/" + nameTemp + ".txt"))) {
                    bw.write(comment);
                    bw.write(function);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // write file

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(output + "file/" + file + ".txt", true))) {
                    bw.write(comment);
                    bw.write(function);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println();

    }
}
