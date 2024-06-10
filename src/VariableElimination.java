import java.util.ArrayList;
import java.util.Map;

public class VariableElimination {
    public static ArrayList<Factor> factors;
    public void variableElimination(Variable start, ArrayList<Variable> variables, ArrayList<Variable> order,ArrayList<Variable> evidence) {

    }

    public void deleteEvidence(Variable evidence, String value, Map<String, String[][]> cpt) {
        for (Map.Entry<String, String[][]> entry : cpt.entrySet())
        {
            for(int j = 0; j < entry.getValue()[0].length-2; j++)
            {
                if(entry.getValue()[0][j].equals(evidence.name))
                {
                    String[][] newFactor = new String[entry.getValue().length / evidence.outcomes.size()][entry.getValue()[0].length - 1];

                    int index1 = 1;
                    int index2 = 0;

                    // First row
                    for(int k = 0; k < newFactor[0].length; k++)
                    {
                        if(!newFactor[0][k].equals(evidence.name))
                            newFactor[0][k] = entry.getValue()[0][k];
                        else
                            k--;
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
                }
            }
        }
    }
}