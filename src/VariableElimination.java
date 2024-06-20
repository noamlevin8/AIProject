import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class VariableElimination {
    public static ArrayList<Factor> factors = new ArrayList<>();
    public static Map<String, Variable> variablesMap; // A map of all the variables by their name

    // The main function for the Variable Elimination
    public VariableElimination(Variable start, Map<String, Variable> variables, ArrayList<Variable> order, ArrayList<Variable> evidence, ArrayList<String> outcome, FileWriter myWriter, ArrayList<String> queryOutcome) throws IOException {
        variablesMap = variables;
        Map<String, Factor> factorMap = new HashMap<>();

        int numOfAdditions = 0, numOfMultiplictions = 0;
        double probability = 0;
        ArrayList<Variable> relevant = new ArrayList<>();

        // Getting the relevant variables
        relevantFromStart(relevant, start);

        for (Variable v : evidence)
            relevantFromStart(relevant,v);

        BayesBall ball = new BayesBall();

        removeIndependentVariables(relevant,start,ball,evidence);

        // Printing all the relevant variables for the query
        for (Variable v : relevant) {
            System.out.println("name: " + v.name);
        }
        System.out.println();

        // Inserting all the relevant CPT'S to the factor map
        for (Variable v : relevant) {
            Factor f = new Factor(v.cpt);
            String s = "";
            for(String n : f.vars)
                s += n + " ";
            factorMap.put(s, f);
        }

        // Checking if we can get the answer from the initial CPT'S
        if(checkInCPTS(factorMap, evidence, start, outcome, queryOutcome, myWriter))
            return;

        // If we don't have any evidence we add all the CPT'S to the factor list
        if(evidence.isEmpty()){
            for (Map.Entry<String, Factor> entry : factorMap.entrySet())
            {
                factors.add(entry.getValue());
            }
        }

        // Evidence deletion
        else {
            for (int i = 0; i < evidence.size(); i++) {
                System.out.println("Evidence - " + evidence.get(i).name + " with value - " + outcome.get(i));
                deleteEvidence(evidence.get(i), outcome.get(i), factorMap);
            }
            System.out.println();
        }

        ArrayList<Factor> remove = new ArrayList<>();

        // Checking if we still have a factor with evidence and then removing it
        for (Factor value : factors) {
            for (Variable evi : evidence) {
                if (value.vars.contains(evi.name)) {
                    remove.add(value);
                }
            }
        }
        factors.removeAll(remove);

        // Eliminating by the given order
        for (Variable ord : order) {
            if (!relevant.contains(ord)) {
                continue;
            }

            ArrayList<Factor> newFactors = new ArrayList<>();

            for (Factor factor : factors) {
                if (!factor.vars.isEmpty() && factor.vars.contains(ord.name)) {
                    newFactors.add(factor);
                }
            }

            // Sorting the factors in order to do the elimination in the right order by means of size
            sortFactors(newFactors);
            factors.removeAll(newFactors);

            Factor newFactor = newFactors.get(0);
            newFactors.remove(newFactor);

            for (Factor factor : newFactors) {
                numOfMultiplictions += newFactor.multiply(factor);
            }

            numOfAdditions += newFactor.Merging(ord);
            factors.add(newFactor);
        }

        Factor newFactor = factors.get(0);
        factors.remove(newFactor);

        for(Factor fr: factors)
            numOfMultiplictions += newFactor.multiply(fr);

        numOfAdditions += newFactor.normalize();
        System.out.println("Final factor:");
        newFactor.printFactor();

        int index1 = start.outcomes.indexOf(queryOutcome.get(0));
        probability = Double.parseDouble(newFactor.table[index1+1][newFactor.table[0].length - 1]);
        String roundedNumber = String.format("%.5f", probability);
        myWriter.write(roundedNumber + "," + numOfAdditions + "," + numOfMultiplictions + "\n");
        System.out.print("Finish\n");
        factors.clear();
    }

    // Delete the evidence from all the CPT'S
    public static void deleteEvidence(Variable evidence, String value, Map<String, Factor> cpt) {

        for (Map.Entry<String, Factor> entry : cpt.entrySet())
        {
            boolean added = false; // In order to know if in the current iteration we added a factor

            for(int j = 0; j < entry.getValue().table[0].length-1; j++)
            {
                if(entry.getValue().table[0][j].equals(evidence.name))
                {
                    String[][] newFactor = new String[entry.getValue().table.length / evidence.outcomes.size() + 1][entry.getValue().table[0].length - 1];

                    int index1 = 1; // New factor's rows
                    int index2 = 0; // New factor's columns
                    int index3 = 0; // New factor's columns for the header row

                    // First row
                    for(int k = 0; k < entry.getValue().table[0].length; k++)
                    {
                        if(!entry.getValue().table[0][k].equals(evidence.name)) {
                            newFactor[0][index3] = entry.getValue().table[0][k];
                            index3++;
                        }
                    }

                    // Rest of the table
                    for(int n = 1; n < entry.getValue().table.length; n++)
                    {
                        if(entry.getValue().table[n][j].equals(value))
                        {
                            for(int m = 0; m < entry.getValue().table[0].length; m++)
                            {
                                if(m != j)
                                {
                                    newFactor[index1][index2] = entry.getValue().table[n][m];
                                    index2++;
                                }
                            }
                            index1++;
                            index2 = 0;
                        }
                    }

                    Factor f = new Factor(newFactor);
                    VariableElimination.factors.add(f);
                    added = true;
                    break;
                }
            }
            if(!added && !VariableElimination.factors.contains(entry.getValue())) {
                VariableElimination.factors.add(entry.getValue());
            }
        }
    }

    // Adding to a list all the relevant variables
    private static void relevantFromStart(ArrayList<Variable> relevant, Variable start) {
        if (relevant.contains(start)) {
            return;
        }

        relevant.add(start);
        for (Variable parent : start.parents) {
            relevantFromStart(relevant, parent);
        }
    }

    // Removing independent variables
    private static void removeIndependentVariables(ArrayList<Variable> toRemove, Variable start, BayesBall ball, ArrayList<Variable> evidence) {
        toRemove.removeIf(v -> ball.bayesBall(v, start, evidence));
    }

    // Sort factors by size
    private static void sortFactors(ArrayList<Factor> factors) {
        factors.sort(new Comparator<Factor>() {
            @Override
            public int compare(Factor f1, Factor f2) {
                return Integer.compare(f1.vars.size(), f2.vars.size());
            }
        });
    }

    // Checking if we can return the answer from the initial CPT'S
    private boolean checkInCPTS(Map<String, Factor> factorMap, ArrayList<Variable> evidence, Variable start, ArrayList<String> outcome, ArrayList<String> queryOutcome, FileWriter myWriter) throws IOException {

        double probability = -1;
        boolean eviFlag = true; // For checking evidence
        int count = 0;


        for (Map.Entry<String, Factor> entry : factorMap.entrySet()) {
            if (entry.getValue().vars.contains(start.name)) {

                // Check that we look at a table that represent our query in the right way
                for(int j = 0; j < entry.getValue().table[0].length - 1; j++){
                    if(evidence.contains(VariableElimination.variablesMap.get(entry.getValue().table[0][j]))){
                        count++;
                    }
                    else
                        break;
                }

                // Not the correct table
                if (count != evidence.size())
                    break;

                for (Variable v : evidence) {
                    if (!entry.getValue().vars.contains(v.name)) {
                        eviFlag = false;
                        break;
                    }
                }

                if (eviFlag) {
                    boolean startFlag = true; // For checking the start
                    for (String v : entry.getValue().vars) {
                        if(!v.equals(start.name) && !evidence.contains(VariableElimination.variablesMap.get(v))){
                            startFlag = false;
                            break;
                        }
                    }
                    if (!startFlag) {
                        break;
                    }

                    String[][] table = entry.getValue().table;

                    // Iterating through each row in the table
                    for (int i = 1; i < table.length; i++) {
                        String[] row = table[i];
                        boolean foundMatch = true; // For checking if we found the value

                        // Check if the row matches the evidence and query outcomes
                        for (int j = 0; j < entry.getValue().vars.size(); j++) {
                            Variable var = VariableElimination.variablesMap.get(entry.getValue().vars.get(j));

                            // Check evidence
                            if (evidence.contains(var)) {
                                int evidenceIndex = evidence.indexOf(var);
                                if (!row[j].equals(outcome.get(evidenceIndex))) {
                                    foundMatch = false;
                                    break;
                                }
                            }

                            // Check query outcome
                            if (var.equals(start)) {
                                int queryIndex = entry.getValue().vars.indexOf(start.name);
                                if (!row[queryIndex].equals(queryOutcome.get(0))) {
                                    foundMatch = false;
                                    break;
                                }
                            }
                        }

                        // If row matches both evidence and query outcomes, add its probability
                        if (foundMatch) {
                            if(probability == -1)
                                probability = Double.parseDouble(row[row.length - 1]);

                            else
                                probability += Double.parseDouble(row[row.length - 1]);

                            break;
                        }
                    }

                    // Write the probability to the file
                    if (probability >= 0) {
                        String roundedNumber = String.format("%.5f", probability);
                        myWriter.write(roundedNumber + ",0,0");
                        myWriter.write("\n");
                        return true; // If found, return true
                    }
                }
            }
        }
        return false;
    }

}