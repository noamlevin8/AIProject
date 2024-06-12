import java.util.ArrayList;
import java.util.Map;

public class VariableElimination {
    public static ArrayList<Factor> factors = new ArrayList<>();
    public VariableElimination(Variable start, ArrayList<Variable> variables, ArrayList<Variable> order,ArrayList<Variable> evidence) {

    }

    public static void deleteEvidence(Variable evidence, String value, Map<String, String[][]> cpt) {
        for (Map.Entry<String, String[][]> entry : cpt.entrySet())
        {
            for(int j = 0; j < entry.getValue()[0].length-1; j++)
            {
                if(entry.getValue()[0][j].equals(evidence.name))
                {
                    String[][] newFactor = new String[entry.getValue().length / evidence.outcomes.size() + 1][entry.getValue()[0].length - 1];

                    int index1 = 1;
                    int index2 = 0;
                    int index3 = 0;

                    // First row
                    for(int k = 0; k < entry.getValue()[0].length; k++)
                    {
                        if(!entry.getValue()[0][k].equals(evidence.name)) {
                            newFactor[0][index3] = entry.getValue()[0][k];
                            index3++;
                        }
                    }

                    for(int n = 1; n < entry.getValue().length; n++)
                    {
                        if(entry.getValue()[n][j].equals(value))
                        {
                            for(int m = 0; m < entry.getValue()[0].length; m++)
                            {
                                if(m != j)
                                {
                                    newFactor[index1][index2] = entry.getValue()[n][m];
                                    index2++;
                                }
                            }
                            index1++;
                            index2 = 0;
                        }
                    }
                    Factor f = new Factor(newFactor);
                    VariableElimination.factors.add(f);
                    break;
                }
            }
        }
    }
}