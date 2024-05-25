import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

class XMLReader
{
    private static String[][] createCPTTable(Definition definition, Map<String, Variable> variableMap)
    {
        Variable variable = variableMap.get(definition.forVar);
        if (variable == null)
        {
            return null;
        }

        int numOutcomes = variable.outcomes.size();
        int numGivenCombinations = definition.probabilities.size() / numOutcomes;

        // Create the table with appropriate size, including header row
        String[][] table = new String[numGivenCombinations * numOutcomes + 1][definition.givens.size() + 2];

        // Fill in the header row
        for (int j = 0; j < definition.givens.size(); j++)
        {
            table[0][j] = definition.givens.get(j);
        }

        table[0][definition.givens.size()] = definition.forVar;
        table[0][definition.givens.size() + 1] = "Probability";

        // Fill the table rows with the given combinations, outcomes, and probabilities
        for (int i = 0; i < numGivenCombinations; i++)
        {
            for (int k = 0; k < numOutcomes; k++)
            {
                int row = i * numOutcomes + k + 1; // Adjust row index to account for header

                // Fill in the given variable values (Needs change)
                for (int j = 0; j < definition.givens.size(); j++)
                {
                    int givenIndex = ((i / (int) Math.pow(definition.givens.size(), j)) % definition.givens.size());
                    table[row][j] = variableMap.get(definition.givens.get(j)).outcomes.get(givenIndex);
                }

                // Fill in the outcome and probability
                table[row][definition.givens.size()] = variable.outcomes.get(k);
                double prob = definition.probabilities.get(i * numOutcomes + k);
                table[row][definition.givens.size() + 1] = String.format("%.5f", prob);
            }
        }

        return table;
    }
    private static void printCPTTable(String[][] table)
    {
        // Print the header
        for (String[] row : table)
        {
            for (String cell : row)
            {
                System.out.print(cell + "\t");
            }
            System.out.println();
        }
    }
    public static void main(String[] args)
    {
        try
        {
            // Print the current working directory
            System.out.println("Current working directory: " + new File(".").getAbsolutePath());

            // Use a relative path
            File inputFile = new File("C:\\Users\\noaml\\IdeaProjects\\AIProject\\src\\alarm_net.xml");
            if (!inputFile.exists())
            {
                throw new FileNotFoundException("File not found: " + inputFile.getAbsolutePath());
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList variableList = doc.getElementsByTagName("VARIABLE");
            List<Variable> variables = new ArrayList<>();
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
            for (Map.Entry<String, String[][]> entry : cptMap.entrySet())
            {
                System.out.println("CPT for: " + entry.getKey());
                printCPTTable(entry.getValue());
                System.out.println();
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}