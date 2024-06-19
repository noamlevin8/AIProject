import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class VariableElimination {
    public static ArrayList<Factor> factors = new ArrayList<>();
    public static Map<String, Variable> variablesMap;
    public VariableElimination(Variable start, Map<String, Variable> variables, ArrayList<Variable> order, ArrayList<Variable> evidence, ArrayList<String> outcome, FileWriter myWriter, ArrayList<String> queryOutcome) throws IOException {
        variablesMap = variables;
        Map<String, Factor> factorMap = new HashMap<>();

        int numAdds = 0, numMultiply = 0;
        double probability = 0;
        ArrayList<Variable> toAdd = new ArrayList<>();

        for (Map.Entry<String, Variable> entry : variables.entrySet()) {
            toAdd.add(entry.getValue());
        }

        toAddStart(toAdd, start);
        BayesBall ball = new BayesBall();

//        for (Variable v : evidence) {
//            toAddStart(toAdd, v);
//        }
//        for (Variable v : order) {
//            toAddStart(toAdd, v);
//        }

        removeIndependentVariables(toAdd,start,ball,evidence);

        for (Variable v : toAdd) {
            System.out.println("name: " + v.name);
        }
        System.out.println();

        for (Variable v : toAdd) {
            Factor f = new Factor(v.cpt);
            String s = "";
            for(String n : f.vars)
                s += n + " ";
            factorMap.put(s, f);
        }

        if(checkForBuiltIn(factorMap, evidence, start, outcome, queryOutcome, myWriter))
            return;


        if(evidence.isEmpty()){
            for (Map.Entry<String, Factor> entry : factorMap.entrySet())
            {
                factors.add(entry.getValue());
            }
        }

        else {
            for (int i = 0; i < evidence.size(); i++) {
                System.out.println("Evidence - " + evidence.get(i).name + " with value - " + outcome.get(i));
                deleteEvidence(evidence.get(i), outcome.get(i), factorMap);
            }
            System.out.println();
        }

        ArrayList<Factor> remove = new ArrayList<>();

        for (Factor value : factors) {
            for (Variable evi : evidence) {
                if (value.vars.contains(evi.name)) {
                    remove.add(value);
                }
            }
        }

        factors.removeAll(remove);

        for (Variable ord : order) {
            if (!toAdd.contains(ord)) {
                continue;
            }

            ArrayList<Factor> newFactors = new ArrayList<>();
            for (Factor factor : factors) {
                if (!factor.vars.isEmpty() && factor.vars.contains(ord.name)) {
                    newFactors.add(factor);
                }
            }
            sortFactors(newFactors);
            factors.removeAll(newFactors);

            Factor newFactor = newFactors.get(0);
            newFactors.remove(newFactor);
            for (Factor factor : newFactors) {
                numMultiply += newFactor.multiply(factor);
            }
            numAdds += newFactor.sumUp(ord);
            factors.add(newFactor);
        }

        Factor newFactor=factors.get(0);
        factors.remove(newFactor);

        for(Factor fr: factors)
            numMultiply += newFactor.multiply(fr);

        numAdds+=newFactor.normalize();
        System.out.println("Final factor:");
        newFactor.printFactor();

        int index1=start.outcomes.indexOf(queryOutcome.get(0));
        probability = Double.parseDouble(newFactor.table[index1+1][newFactor.table[0].length - 1]);
        String roundedNumber = String.format("%.5f", probability);
        myWriter.write(roundedNumber + "," + numAdds + "," + numMultiply + "\n");
        System.out.print("Finish\n");
        factors.clear();
    }

    public static void deleteEvidence(Variable evidence, String value, Map<String, Factor> cpt) {

        for (Map.Entry<String, Factor> entry : cpt.entrySet())
        {
            boolean added = false;
            for(int j = 0; j < entry.getValue().table[0].length-1; j++)
            {
                if(entry.getValue().table[0][j].equals(evidence.name))
                {
                    String[][] newFactor = new String[entry.getValue().table.length / evidence.outcomes.size() + 1][entry.getValue().table[0].length - 1];

                    int index1 = 1;
                    int index2 = 0;
                    int index3 = 0;

                    // First row
                    for(int k = 0; k < entry.getValue().table[0].length; k++)
                    {
                        if(!entry.getValue().table[0][k].equals(evidence.name)) {
                            newFactor[0][index3] = entry.getValue().table[0][k];
                            index3++;
                        }
                    }

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

    private static void toAddStart(ArrayList<Variable> toAdd, Variable start) {
        if (toAdd.contains(start)) {
            return;
        }
        toAdd.add(start);
        for (Variable parent : start.parents) {
            toAddStart(toAdd, parent);
        }
    }

    private static void removeIndependentVariables(ArrayList<Variable> toRemove, Variable start, BayesBall ball, ArrayList<Variable> evidence) {
        toRemove.removeIf(v -> ball.bayesBall(v, start, evidence));
    }

    private static void sortFactors(ArrayList<Factor> factors) {
        factors.sort(new Comparator<Factor>() {
            @Override
            public int compare(Factor f1, Factor f2) {
                return Integer.compare(f1.vars.size(), f2.vars.size());
            }
        });
    }

    private boolean checkForBuiltIn(Map<String, Factor> factorMap, ArrayList<Variable> evidence, Variable start, ArrayList<String> outcome, ArrayList<String> queryOutcome, FileWriter myWriter) throws IOException {

        double probability = 0;
        boolean flag = true;


        for (Map.Entry<String, Factor> entry : factorMap.entrySet()) {
            if (entry.getValue().vars.contains(start.name)) {
                entry.getValue().printFactor();
                // Check if all evidence variables are contained in this factor
                for (Variable v : evidence) {
                    if (!entry.getValue().vars.contains(v.name)) {
                        flag = false;
                        break;
                    }
                }

                if (flag) {
                    boolean fl=true;
                    for (String v : entry.getValue().vars) {
                        if(!v.equals(start.name) && !evidence.contains(VariableElimination.variablesMap.get(v))){
                            fl=false;
                            break;
                        }
                    }
                    if (!fl) {
                        break;
                    }

                    String[][] table = entry.getValue().table;

                    // Iterate through each row in the table
                    for (int i = 1; i < table.length; i++) {
                        String[] row = table[i];
                        boolean match = true;

                        // Check if the row matches the evidence and query outcomes
                        for (int j = 0; j < entry.getValue().vars.size(); j++) {
                            Variable var = VariableElimination.variablesMap.get(entry.getValue().vars.get(j));

                            // Check against evidence
                            if (evidence.contains(var)) {
                                int evidenceIndex = evidence.indexOf(var);
                                if (!row[j].equals(outcome.get(evidenceIndex))) {
                                    match = false;
                                    break;
                                }
                            }

                            // Check against query outcome
                            if (var.equals(start)) {
                                int queryIndex = entry.getValue().vars.indexOf(start.name);
                                if (!row[queryIndex].equals(queryOutcome.get(0))) {
                                    match = false;
                                    break;
                                }
                            }
                        }

                        // If row matches both evidence and query outcomes, add its probability
                        if (match) {
                            probability += Double.parseDouble(row[row.length - 1]);
                            break;
                        }
                    }

                    // Write the probability to the file
                    if (probability >= 0) {
                        String roundedNumber = String.format("%.5f", probability);
                        myWriter.write(roundedNumber+",0,0");
                        myWriter.write("\n");
                        return true; // If found, return true
                    }
                }
            }
        }
        return false;
    }

}