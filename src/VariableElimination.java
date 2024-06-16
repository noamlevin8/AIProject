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

        for (Variable v : evidence) {
            toAddStart(toAdd, v);
        }
        for (Variable v : order) {
            toAddStart(toAdd, v);
        }

        removeIndependentVariables(toAdd,start,ball,evidence);

        for (Variable v : toAdd) {
            System.out.println("name: " + v.name);
        }

        for (Variable v : toAdd) {
            factorMap.put(v.name,new Factor(v.cpt));
            //factors.add(new Factor(v.cpt));
        }

        //deleteEvidence(variablesMap.get("A"), "F", factorMap);



        System.out.println("\nbefore delete");
        for (Map.Entry<String, Factor> entry : factorMap.entrySet()) {
            //System.out.println("Factor for: " + entry.getKey());
            entry.getValue().printFactor();
        }

//        for (int i = 0; i < evidence.size(); i++) {
//            Variable evi = evidence.get(i);
//            String outcomeValue = outcome.get(i);
//            for (Factor factor : factors) {
//                if (factor.getVariables().contains(evi)) {
//                    factor.evidenceDelete(evi, outcomeValue);
//                }
//            }
//        }

        System.out.println("evidence size - " + evidence.size());
        for(Variable variable : evidence){
            System.out.println("evidence: " + variable.name);
        }

        for(int i = 0; i < evidence.size(); i++){
            deleteEvidence(evidence.get(i), outcome.get(i), factorMap);
        }
        //deleteEvidence(variablesMap.get("A"), "F", factorMap);

        System.out.println("after delete");
        for (Factor factor : factors) {
            factor.printFactor();
//            System.out.print("Vars - ");
//            for(String str : factor.vars){
//                System.out.print(str + " ");
//            }
//            System.out.println();
        }

//        for (Variable v : order) {
//            factors.add(new Factor(v));
//        }

        System.out.print("Order - ");
        for (Variable ord : order) {
            System.out.print(ord.name + " ");
        }
        System.out.println();

        //System.out.println("size factors: " + factors.size());

        for (Variable ord : order) {
            if (!toAdd.contains(ord)) {
                //System.out.println("Not adding");
                continue;
            }
            //System.out.println("test for");
            ArrayList<Factor> newFactors = new ArrayList<>();
            int count = 0;
            System.out.println("size factors: " + factors.size());
            for (Factor factor : factors) {
//                System.out.print("Vars - ");
//                for(String str : factor.vars){
//                    System.out.print(str + " ");
//                }
//                System.out.println();
                System.out.println("count: " + ++count);
                factor.printFactor();
                if (!factor.vars.isEmpty() && factor.vars.contains(ord.name)) {
                    //System.out.println("added factor");
                    newFactors.add(factor);
                }
            }
            sortFactors(newFactors);
            factors.removeAll(newFactors);
            System.out.println("Num of fac: " + factors.size());

            Factor newFactor = newFactors.get(0);
//            System.out.println("print new factor: ");
//            newFactor.printFactor();
            newFactors.remove(newFactor);
            System.out.println("Size new: " + newFactors.size());
            for (Factor factor : newFactors) {
                numMultiply += newFactor.multiply(factor);
            }
            numAdds += newFactor.sumUp(ord);
            System.out.println("print new factor: ");
            newFactor.printFactor();
            factors.add(newFactor);
        }

        Factor newFactor=factors.get(0);
        factors.remove(newFactor);
        for(Factor fr: factors)
            numMultiply += newFactor.multiply(fr);
//        Factor newFactor = factors.get(0);
//        numAdds+= newFactor.marginalize();
        numAdds+=newFactor.normalize();
        newFactor.printFactor();

        int index=start.outcomes.indexOf(queryOutcome.get(0));
        probability = Double.parseDouble(newFactor.table[index+1][newFactor.table[0].length - 1]);
        myWriter.write(probability + "," + numAdds + "," + numMultiply + "\n");
        System.out.println("finish");
    }

    public static void deleteEvidence(Variable evidence, String value, Map<String, Factor> cpt) {
        for (Map.Entry<String, Factor> entry : cpt.entrySet())
        {
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
//                    System.out.println("Test factor:");
//                    f.printFactor();
                    VariableElimination.factors.add(f);
                    break;
                }
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
}