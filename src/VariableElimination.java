import java.util.ArrayList;
import java.util.Map;

public class VariableElimination {
    public static ArrayList<Factor> factors;
    public void variableElimination(Variable start, ArrayList<Variable> variables, ArrayList<Variable> order,ArrayList<Variable> evidence) {

    }

    public void deleteEvidence(String evidenceName, String value, Map<String, String[][]> cpt) {
        for (Map.Entry<String, String[][]> entry : cpt.entrySet())
        {
            for(int j = 0; j < entry.getValue()[0].length-2; j++)
            {
                if(entry.getValue()[0][j].equals(evidenceName))
                {
//                    if(j == 0)
//                    {
//                        cpt.remove(entry.getKey());
//                    }
//                    else
//                    {
//
//                    }

                }
            }
        }
    }
}