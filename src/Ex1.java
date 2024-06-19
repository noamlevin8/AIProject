// CausalChain - yes in line 9 (should be no)
// CommonCausal - yes in line 9 (should be no)
// Example1 - 1 in line 6 (should be 0), 19 and 36 in line 8 (should be 11, 20)
// Example2 - yes in line 1 (should be no), line 3 should be (0.32609,2,3), yes in line 7 (should be no), 15 and 26 in line 8 (should be 11, 16)

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class Ex1
{
    private static String[][] createCPTTable(Definition definition, Map<String, Variable> variableMap) {
        Variable variable = variableMap.get(definition.forVar);
        if (variable == null) {
            return null;
        }

        int numOutcomes = variable.outcomes.size();
        int numGivenCombinations = definition.probabilities.size() / numOutcomes;

        // Create the table with appropriate size, including header row
        String[][] table = new String[numGivenCombinations * numOutcomes + 1][definition.givens.size() + 2];

        // Fill in the header row
        for (int j = 0; j < definition.givens.size(); j++) {
            table[0][j] = definition.givens.get(j);
        }

        table[0][definition.givens.size()] = definition.forVar;
        table[0][definition.givens.size() + 1] = "Probability";

        // Fill the table rows with the given combinations, outcomes, and probabilities
        int counter = variable.outcomes.size();

        // Set the variable values on the last column
        for(int i = 1; i < numGivenCombinations * numOutcomes + 1; i++) {
           table[i][definition.givens.size()] = variable.outcomes.get((i-1) % counter);
        }

        // Set the parents values
        for(int j = definition.givens.size()-1; j >= 0; j--){
            for(int i = 1; i < numGivenCombinations * numOutcomes + 1; i++) {
                table[i][j] = variable.parents.get(j).outcomes.get(((i-1) / counter) % variable.parents.get(j).outcomes.size());
            }

            counter *= variable.parents.get(j).outcomes.size();
        }

        // Set the probability
        for(int i = 1; i < numGivenCombinations * numOutcomes + 1; i++) {
            double prob = definition.probabilities.get(i-1);
            table[i][definition.givens.size() + 1] = String.format(String.valueOf(prob));
        }

        return table;
    }

    private static void extract_for_elimination(ArrayList<Variable> isIn, ArrayList<Variable> evidence, ArrayList<Variable> order, ArrayList<Variable> variables, String line,ArrayList<String> evidenceOutcome,ArrayList<String> queryOutcome) {
        String[] parts = line.split(" ");
        String probabilityPart = parts[0];
        String orderPart = parts.length > 1 ? parts[1] : "";

        // Extract the variables inside the P()
        int startIndex = probabilityPart.indexOf('(') + 1;
        int endIndex = probabilityPart.indexOf(')');
        String insideP = probabilityPart.substring(startIndex, endIndex);

        // Split the insideP part around the "|"
        String[] conditionalParts = insideP.split("\\|");
        String leftOfPipe = conditionalParts[0];
        String rightOfPipe = conditionalParts.length > 1 ? conditionalParts[1] : "";

        // Add variables to isIn
        String[] leftVariables = leftOfPipe.split(",");
        for (String var : leftVariables) {
            String[] nameValue = var.split("=");
            for(Variable variable : variables){
                if(variable.name.equals(nameValue[0])){
                    isIn.add(variable);
                    queryOutcome.add(nameValue[1]);
                    break;
                }
            }
        }

        // Add variables to evidence
        if (!rightOfPipe.isEmpty()) {
            String[] rightVariables = rightOfPipe.split(",");
            for (String var : rightVariables) {
                String[] nameValue = var.split("=");
                for (Variable variable : variables) {
                    if (variable.name.equals(nameValue[0])) {
                        evidence.add(variable);
                        evidenceOutcome.add(nameValue[1]);
                        break;
                    }
                }
            }
        }

        // Add variables to order
        if (!orderPart.isEmpty()) {
            String[] orderVariables = orderPart.split("-");
            for (String var : orderVariables) {
                for (Variable variable : variables) {
                    if (variable.name.equals(var)) {
                        order.add(variable);
                        break;
                    }
                }
            }
        }
    }

    public static boolean isBayesBall(String line){
        int i=0;
        while(line.charAt(i)==' '){
            i++;
        }
        return !line.contains("(") && !line.contains(")");
    }

    public static void extract_for_bayesBall(ArrayList<Variable> isIn, ArrayList<Variable> evidence, ArrayList<Variable> variables, String line) {
        // Clear previous evidence
        evidence.clear();
        isIn.clear();
        // Split the line into the left and right parts
        String[] parts = line.split("\\|");
        if (parts.length < 1) {
            System.err.println("Invalid line format: " + line);
            return;
        }

        String leftPart = parts[0]; // B-E
        String rightPart = parts.length > 1 ? parts[1].trim() : ""; // J=T

        // Extract start and end variables from the left part
        String[] leftVariables = leftPart.split("-");
        if (leftVariables.length < 2) {
            System.err.println("Invalid left part format: " + leftPart);
            return;
        }

        String startStr = leftVariables[0];
        String endStr = leftVariables[1];

        for (Variable variable : variables) {
            if (variable.name.equals(startStr)) {
                isIn.add(variable);
            }
            if (variable.name.equals(endStr)) {
                isIn.add(variable);
            }
        }

        // Extract evidence from the right part if it exists
        if (!rightPart.isEmpty()) {
            String[] evidencePairs = rightPart.split(","); // Split by comma to handle multiple evidence pairs
            for (String evidencePair : evidencePairs) {
                String[] nameValuePair = evidencePair.split("=");
                if (nameValuePair.length == 2) {
                    String evidenceVar = nameValuePair[0].trim(); // Evidence variable name
                    for (Variable variable : variables) {
                        if (variable.name.equals(evidenceVar)) {
                            evidence.add(variable);
                        }
                    }
                }
            }
        }
    }
    public static void main(String[] args)
    {
        try
        {
            BufferedReader file = new BufferedReader(new FileReader("input.txt"));
            String xmlName=file.readLine();

            FileWriter myWriter = new FileWriter("output.txt");


            // Print the current working directory
            System.out.println("Current working directory: " + new File(".").getAbsolutePath());

            // Use a relative path
            File inputFile = new File(xmlName);
            if (!inputFile.exists())
            {
                throw new FileNotFoundException("File not found: " + inputFile.getAbsolutePath());
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList variableList = doc.getElementsByTagName("VARIABLE");
            ArrayList<Variable> variables = new ArrayList<>();
            Map<String, Variable> variableMap = new HashMap<>();

            for (int i = 0; i < variableList.getLength(); i++)
            {
                Node variableNode = variableList.item(i);
                if (variableNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element variableElement = (Element) variableNode;
                    String name = variableElement.getElementsByTagName("NAME").item(0).getTextContent();
                    Variable variable = new Variable(name);

                    NodeList outcomeList = variableElement.getElementsByTagName("OUTCOME");
                    for (int j = 0; j < outcomeList.getLength(); j++)
                    {
                        variable.addOutcome(outcomeList.item(j).getTextContent());
                    }

                    variables.add(variable);
                    variableMap.put(name, variable);
                }
            }

            NodeList definitionList = doc.getElementsByTagName("DEFINITION");
            List<Definition> definitions = new ArrayList<>();
            Map<String, Definition> definitionMap = new HashMap<>();

            for (int i = 0; i < definitionList.getLength(); i++)
            {
                Node definitionNode = definitionList.item(i);
                if (definitionNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element definitionElement = (Element) definitionNode;
                    String forVar = definitionElement.getElementsByTagName("FOR").item(0).getTextContent();
                    Definition definition = new Definition(forVar);

                    NodeList givenList = definitionElement.getElementsByTagName("GIVEN");
                    for (int j = 0; j < givenList.getLength(); j++)
                    {
                        Variable v1 = null;
                        Variable v2 = null;
                        for (Variable v : variables)
                        {
                            if (v.name.equals(forVar))
                            {
                                v1 = v;
                                break;
                            }
                        }
                        for (Variable v : variables)
                        {
                            if (v.name.equals(givenList.item(j).getTextContent()))
                            {
                                v2 = v;
                                break;
                            }
                        }

                        if (v1 != null && v2 != null)
                        {
                            definition.addGiven(givenList.item(j).getTextContent(), v1, v2);
                        }
                    }

                    String table = definitionElement.getElementsByTagName("TABLE").item(0).getTextContent();
                    definition.setTable(table);

                    definitions.add(definition);
                    definitionMap.put(forVar, definition);
                }
            }

            // Create a map to store the CPTs for each variable
            Map<String, String[][]> cptMap = new HashMap<>();

            // Process each definition and store the CPT in the map
            for (Definition definition : definitions)
            {
                String[][] cptTable = createCPTTable(definition, variableMap);
                cptMap.put(definition.forVar, cptTable);
            }

            // Example: print out the CPT table for each variable (for debugging purposes)
            for (Variable variable : variables)
            {
                variable.addCPT(cptMap.get(variable.name));
            }

            String line;
            while ((line = file.readLine()) != null){
                if(isBayesBall(line)){
                    System.out.println("Bayes Ball:");

                    ArrayList<Variable> evidence = new ArrayList<>();
                    ArrayList<Variable> queryVariables = new ArrayList<>();
                    extract_for_bayesBall(queryVariables,evidence,variables,line);

                    System.out.println("Start: " + queryVariables.get(0));
                    System.out.println("End: " + queryVariables.get(1));
                    System.out.println("Evidence: " + evidence);

                    BayesBall bayesBallInstance = new BayesBall();
                    if(bayesBallInstance.bayesBall(queryVariables.get(1),queryVariables.get(0),evidence) && bayesBallInstance.bayesBall(queryVariables.get(0),queryVariables.get(1),evidence)) {
                        System.out.println(queryVariables.get(1).name + " and " + queryVariables.get(0).name + " are independent\n");
                        myWriter.write("yes\n");
                    }
                    else {
                        System.out.println(queryVariables.get(1).name + " and " + queryVariables.get(0).name + " are dependent\n");
                        myWriter.write("no\n");
                    }
                }
                else{
                    System.out.println("\nVariable Elimination:");
                    ArrayList<Variable> evidence = new ArrayList<>();
                    ArrayList<String> evidenceOutcome = new ArrayList<>();
                    ArrayList<Variable> queryVariables = new ArrayList<>();
                    ArrayList<Variable> order = new ArrayList<>();

                    ArrayList<String> queryOutcome = new ArrayList<>();
                    extract_for_elimination(queryVariables,evidence,order,variables,line,evidenceOutcome,queryOutcome);
                    VariableElimination variableEliminationInstance = new VariableElimination(queryVariables.get(0),variableMap,order,evidence,evidenceOutcome,myWriter,queryOutcome);
                }
            }

            String line2;
            myWriter.close();
            BufferedReader file2 = new BufferedReader(new FileReader("output.txt"));
            System.out.println();
            while ((line2 = file2.readLine()) != null){
                System.out.println(line2);
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}