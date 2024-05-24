import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

class XMLReader {
    public static void main(String[] args) {
        try {
            // Print the current working directory
            System.out.println("Current working directory: " + new File(".").getAbsolutePath());

            // Use a relative path
            File inputFile = new File("C:\\Users\\noaml\\IdeaProjects\\AIProject\\src\\alarm_net.xml");
            if (!inputFile.exists()) {
                throw new FileNotFoundException("File not found: " + inputFile.getAbsolutePath());
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList variableList = doc.getElementsByTagName("VARIABLE");
            List<Variable> variables = new ArrayList<>();
            Map<String, Variable> variableMap = new HashMap<>();

            for (int i = 0; i < variableList.getLength(); i++) {
                Node variableNode = variableList.item(i);
                if (variableNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element variableElement = (Element) variableNode;
                    String name = variableElement.getElementsByTagName("NAME").item(0).getTextContent();
                    Variable variable = new Variable(name);

                    NodeList outcomeList = variableElement.getElementsByTagName("OUTCOME");
                    for (int j = 0; j < outcomeList.getLength(); j++) {
                        variable.addOutcome(outcomeList.item(j).getTextContent());
                    }

                    variables.add(variable);
                    variableMap.put(name, variable);
                }
            }

            NodeList definitionList = doc.getElementsByTagName("DEFINITION");
            List<Definition> definitions = new ArrayList<>();
            Map<String, Definition> definitionMap = new HashMap<>();

            for (int i = 0; i < definitionList.getLength(); i++) {
                Node definitionNode = definitionList.item(i);
                if (definitionNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element definitionElement = (Element) definitionNode;
                    String forVar = definitionElement.getElementsByTagName("FOR").item(0).getTextContent();
                    Definition definition = new Definition(forVar);

                    NodeList givenList = definitionElement.getElementsByTagName("GIVEN");
                    for (int j = 0; j < givenList.getLength(); j++) {
                        Variable v1 = null;
                        Variable v2 = null;
                        for (Variable v : variables) {
                            if (v.name.equals(forVar)) {
                                v1 = v;
                                break;
                            }
                        }
                        for (Variable v : variables) {
                            if (v.name.equals(givenList.item(j).getTextContent())) {
                                v2 = v;
                                break;
                            }
                        }

                        if (v1 != null && v2 != null) {
                            definition.addGiven(givenList.item(j).getTextContent(), v1, v2);
                        }
                    }

                    String table = definitionElement.getElementsByTagName("TABLE").item(0).getTextContent();
                    definition.setTable(table);

                    definitions.add(definition);
                    definitionMap.put(forVar, definition);
                }
            }

            // Combine the variables and definitions into a joint probability table
            Map<String, Double> jointProbabilityTable = calculateJointProbabilityTable(variables, definitions);

            // Print out the joint probability table for verification
            for (Map.Entry<String, Double> entry : jointProbabilityTable.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
                // + String.format("%.5f", entry.getValue())
            }

//            // Print out variables and definitions for verification
//            for (Variable variable : variables) {
//                System.out.println(variable);
//            }
//
//            for (Definition definition : definitions) {
//                System.out.println(definition);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Double> calculateJointProbabilityTable(List<Variable> variables, List<Definition> definitions) {
        Map<String, Double> jointProbabilityTable = new LinkedHashMap<>();

        // Base case for recursion - fill in the probabilities for leaf nodes (variables with no parents)
        for (Definition def : definitions) {
            if (def.givens.isEmpty()) {
                Variable var = getVariableByName(variables, def.forVar);
                for (int i = 0; i < var.outcomes.size(); i++) {
                    String key = def.forVar + "=" + var.outcomes.get(i);
                    jointProbabilityTable.put(key, def.probabilities.get(i));
                }
            }
        }

        // Recursive case - calculate joint probabilities for variables with parents
        calculateJointProbabilities(variables, definitions, jointProbabilityTable, new LinkedHashMap<>(), 0);

        return jointProbabilityTable;
    }

    private static void calculateJointProbabilities(List<Variable> variables, List<Definition> definitions,
                                                    Map<String, Double> jointProbabilityTable, Map<String, String> currentAssignment, int index) {
        if (index == variables.size()) {
            double jointProb = 1.0;
            for (Definition def : definitions) {
                List<String> parentAssignments = new ArrayList<>();
                for (String parent : def.givens) {
                    parentAssignments.add(currentAssignment.get(parent));
                }
                String childAssignment = currentAssignment.get(def.forVar);
                double prob = getProbability(def, parentAssignments, childAssignment);
                jointProb *= prob;
            }
            String key = currentAssignment.toString();
            jointProbabilityTable.put(key, jointProb);
            return;
        }

        Variable var = variables.get(index);
        for (String outcome : var.outcomes) {
            currentAssignment.put(var.name, outcome);
            calculateJointProbabilities(variables, definitions, jointProbabilityTable, currentAssignment, index + 1);
        }
    }

    private static double getProbability(Definition def, List<String> parentAssignments, String childAssignment) {
        int index = 0;
        int multiplier = 1;
        for (int i = parentAssignments.size() - 1; i >= 0; i--) {
            String parentOutcome = parentAssignments.get(i);
            if (parentOutcome.equals("T")) {
                index += multiplier;
            }
            multiplier *= 2;
        }
        if (childAssignment.equals("T")) {
            return def.probabilities.get(index);
        } else {
            return def.probabilities.get(index + 1);
        }
    }

    private static Variable getVariableByName(List<Variable> variables, String name) {
        for (Variable var : variables) {
            if (var.name.equals(name)) {
                return var;
            }
        }
        return null;
    }
}