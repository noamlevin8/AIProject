import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class VariableElimination {
    public static ArrayList<Factor> factors = new ArrayList<>();
    public static Map<String, Variable> variablesMap;
    public VariableElimination(Variable start, Map<String, Variable> variables, ArrayList<Variable> order, ArrayList<Variable> evidence, ArrayList<String> outcome, FileWriter myWriter, ArrayList<String> queryOutcome, Map<String, String[][]> cpt) throws IOException {
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
            //factors.add(new Factor(v.cpt));
        }

        if(checkForBuiltIn(factorMap, evidence, start, outcome, queryOutcome, myWriter))
            return;

        //deleteEvidence(variablesMap.get("A"), "F", factorMap);



//        System.out.println("\nbefore delete");
//        for (Map.Entry<String, Factor> entry : factorMap.entrySet()) {
//            //System.out.println("Factor for: " + entry.getKey());
//            entry.getValue().printFactor();
//        }

//        for (int i = 0; i < evidence.size(); i++) {
//            Variable evi = evidence.get(i);
//            String outcomeValue = outcome.get(i);
//            for (Factor factor : factors) {
//                if (factor.getVariables().contains(evi)) {
//                    factor.evidenceDelete(evi, outcomeValue);
//                }
//            }
//        }

//        System.out.println("evidence size - " + evidence.size());
//        for(Variable variable : evidence){
//            System.out.println("evidence: " + variable.name);
//        }

        for(int i = 0; i < evidence.size(); i++){
            System.out.println("Evidence - " + evidence.get(i).name + " with value - " + outcome.get(i));
            deleteEvidence(evidence.get(i), outcome.get(i), factorMap);
        }
        System.out.println();

//        System.out.println("Before factor eli: ");
//        for(Factor f : factors){
//            f.printFactor();
//        }
        ArrayList<Factor> remove = new ArrayList<>();

        for(int i = 0; i < factors.size(); i++){
//            System.out.println("Test factor elimination:");
//            factors.get(i).printFactor();
            for(Variable evi : evidence) {
//                System.out.println("Evi - " + evi.name);
                if (factors.get(i).vars.contains(evi.name)) {
//                    System.out.println("Yes");
                    remove.add(factors.get(i));
                    //factors.remove(factors.get(i));
                }
//                else
//                    System.out.println("No");
            }
        }

        factors.removeAll(remove);

        for (Factor factor : factors) {
            if(factor.vars.size()==1 && evidence.contains(VariableElimination.variablesMap.get(factor.vars.get(0)))){
                factors.remove(factor);
            }
        }

        //deleteEvidence(variablesMap.get("A"), "F", factorMap);

//        System.out.println("after delete");
//        for (Factor factor : factors) {
//            factor.printFactor();
//            System.out.print("Vars - ");
//            for(String str : factor.vars){
//                System.out.print(str + " ");
//            }
//            System.out.println();
//        }

//        for (Variable v : order) {
//            factors.add(new Factor(v));
//        }

//        System.out.print("Order - ");
//        for (Variable ord : order) {
//            System.out.print(ord.name + " ");
//        }
//        System.out.println();

        //System.out.println("size factors: " + factors.size());

        for (Variable ord : order) {
            if (!toAdd.contains(ord)) {
                //System.out.println("Not adding");
                continue;
            }
            //System.out.println("test for");
            ArrayList<Factor> newFactors = new ArrayList<>();
//            int count = 0;
//            System.out.println("size factors: " + factors.size());
            for (Factor factor : factors) {
//                System.out.print("Vars - ");
//                for(String str : factor.vars){
//                    System.out.print(str + " ");
//                }
//                System.out.println();
//                System.out.println("count: " + ++count);
//                factor.printFactor();
                if (!factor.vars.isEmpty() && factor.vars.contains(ord.name)) {
                    //System.out.println("added factor");
                    newFactors.add(factor);
                }
            }
            sortFactors(newFactors);
            factors.removeAll(newFactors);
//            System.out.println("Num of fac: " + factors.size());

            Factor newFactor = newFactors.get(0);
//            System.out.println("print new factor: ");
//            newFactor.printFactor();
            newFactors.remove(newFactor);
//            System.out.println("Size new: " + newFactors.size());
            for (Factor factor : newFactors) {
//                System.out.println("I multipy:");
//                newFactor.printFactor();
//                System.out.println("with:");
//                factor.printFactor();
                numMultiply += newFactor.multiply(factor);
//                index++;
//                System.out.println(index+":");
//                System.out.println("and i get this:");
//                newFactor.printFactor();
            }
            numAdds += newFactor.sumUp(ord);
//            System.out.println("print new factor: ");
//            newFactor.printFactor();
            factors.add(newFactor);
        }

        Factor newFactor=factors.get(0);
        factors.remove(newFactor);
//        System.out.println("Size at the end - " + factors.size());
        for(Factor fr: factors)
            numMultiply += newFactor.multiply(fr);
//        Factor newFactor = factors.get(0);
//        numAdds+= newFactor.marginalize();
//        System.out.println("Before norm: ");
//        newFactor.printFactor();
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
        //boolean added = false;

        //ArrayList<String> remove = new ArrayList<>();

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
//                    System.out.println("Test new factor:");
//                    f.printFactor();
//                    if(VariableElimination.factors.contains(entry.getValue()))
//                        VariableElimination.factors.remove(entry.getValue());
                    VariableElimination.factors.add(f);
//                    System.out.println("Key to remove: " + entry.getKey());
//                    remove.add(entry.getKey());
                    //cpt.remove(entry.getKey());
                    added = true;
                    break;
                }
                //VariableElimination.factors.add(entry.getValue());
            }
            if(!added && !VariableElimination.factors.contains(entry.getValue())) {
                VariableElimination.factors.add(entry.getValue());
//                System.out.println("Test added factor:");
//                entry.getValue().printFactor();
            }

            //added = false;
        }

//        for(String r : remove)
//            cpt.remove(r);
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
                // Check if all evidence variables are contained in this factor
                for (Variable v : evidence) {
                    if (!entry.getValue().vars.contains(v.name)) {
                        flag = false;
                        break;
                    }
                }

                if (flag) {
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
                        }
                    }

                    // Write the probability to the file
                    if (probability > 0) {
                        myWriter.write(probability+",0,0");
                        myWriter.write("\n");
                        return true; // If found, return true
                    }
                }
            }
        }
        return false;
    }

}