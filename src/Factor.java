import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Factor {
    ArrayList<String> vars = new ArrayList<>();
    String[][] table;

    public Factor(String[][] t){
        this.table = t;

        for(int j = 0; j < t[0].length-1; j++)
        {
            this.vars.add(t[0][j]);
        }
    }

    public int multiply(Factor other) {
        if(other.vars.size()>this.vars.size()){
            return this.multiply2(other);
        }

        int numMultiplies = 0;

        ArrayList<Variable> newVariables = new ArrayList<>();

        for(int i=0;i<this.table[0].length-1;i++){
            String str=this.table[0][i];
            for(String var: this.vars){
                if(str.equals(var))
                    newVariables.add(VariableElimination.variablesMap.get(var));
            }
        }
        for (String var : other.vars) {
            if (!newVariables.contains(VariableElimination.variablesMap.get(var))) {
                newVariables.add(VariableElimination.variablesMap.get(var));
            }
        }

        ArrayList<Variable> lolOther=new ArrayList<>();
        for(int i=0;i<other.table[0].length-1;i++){
            String str=other.table[0][i];
            for(String var: other.vars){
                if(str.equals(var))
                    lolOther.add(VariableElimination.variablesMap.get(var));
            }
        }

        ArrayList<Variable> lolThis=new ArrayList<>();
        for(int i=0;i<this.table[0].length-1;i++){
            String str=this.table[0][i];
            for(String var: this.vars){
                if(str.equals(var))
                    lolThis.add(VariableElimination.variablesMap.get(var));
            }
        }

        // Calculate the size of the new table based on combined variables
        int newTableSize = 1;
        for (Variable var : newVariables) {
            newTableSize *= var.outcomes.size();
        }

        // Initialize a new table to hold multiplied values
        String[][] newTable = new String[newTableSize + 1][newVariables.size() + 1];

        // Populate the header row of the new table with variable names and "pro" for probability
        for (int j = 0; j < newVariables.size(); j++) {
            newTable[0][j] = newVariables.get(j).name;
        }
        newTable[0][newTable[0].length - 1] = "Probability";

        // Identify common variables between this factor and the other factor
        ArrayList<Variable> commonVariables = new ArrayList<>();
        for (Variable var : newVariables) {
            if (this.vars.contains(var.name) && other.vars.contains(var.name)) {
                commonVariables.add(var);
            }
        }

        // Iterate through each row in this factor's table
        for (int i = 1; i < this.table.length; i++) {
            String[] thisLine = this.table[i];
            double prob1 = Double.parseDouble(thisLine[thisLine.length - 1]);

            // Iterate through each row in the other factor's table
            for (int j = 1; j < other.table.length; j++) {
                String[] otherLine = other.table[j];
                double prob2 = Double.parseDouble(otherLine[otherLine.length - 1]);

                // Check if the current rows can be multiplied (consistent assignments for common variables)
                boolean consistent = true;
                for (Variable var : commonVariables) {
                    int thisIndex = lolThis.indexOf(var);
                    int otherIndex = lolOther.indexOf(var);
                    if (!thisLine[thisIndex].equals(otherLine[otherIndex])) {
                        consistent = false;
                        break;
                    }
                }

                if (consistent) {
                    // Create a new row in the new table for the multiplied result
                    String[] newRow = new String[newTable[0].length];

                    // Fill the new row according to the new variable order
                    for (int k = 0; k < newVariables.size(); k++) {
                        Variable newVar = newVariables.get(k);
                        if (this.vars.contains(newVar.name)) {
                            newRow[k] = thisLine[lolThis.indexOf(newVar)];
                        } else if (other.vars.contains(newVar.name)) {
                            newRow[k] = otherLine[lolOther.indexOf(newVar)];
                        }
                    }

                    // Multiply the probabilities
                    newRow[newTable[0].length - 1] = String.valueOf(prob1 * prob2);

                    // Add the new row to the new table
                    newTable[numMultiplies + 1] = newRow;
                    numMultiplies++;
                }
            }
        }

        // Update this factor's variables and table with the new variables and table
        this.vars = new ArrayList<>();

        this.table = newTable;

        for(int j = 0; j < this.table[0].length-1; j++)
        {
            this.vars.add(this.table[0][j]);
        }

        return numMultiplies;
    }

    public int multiply2(Factor other) {
        int numMultiplies = 0;

        ArrayList<Variable> newVariables = new ArrayList<>();

        for(int i=0;i<other.table[0].length-1;i++) {
            String str = other.table[0][i];
            for (String var : other.vars) {
                if (str.equals(var)) {
                    newVariables.add(VariableElimination.variablesMap.get(var));
                    break;
                }
            }
        }
        for (String var : this.vars) {
            if (!newVariables.contains(VariableElimination.variablesMap.get(var))) {
                newVariables.add(VariableElimination.variablesMap.get(var));
                break;
            }
        }

        ArrayList<Variable> lolOther=new ArrayList<>();
        for(int i=0;i<other.table[0].length-1;i++){
            String str=other.table[0][i];
            for(String var: other.vars){
                if(str.equals(var))
                    lolOther.add(VariableElimination.variablesMap.get(var));
            }
        }

        ArrayList<Variable> lolThis=new ArrayList<>();
        for(int i=0;i<this.table[0].length-1;i++){
            String str=this.table[0][i];
            for(String var: this.vars){
                if(str.equals(var))
                    lolThis.add(VariableElimination.variablesMap.get(var));
            }
        }

        // Calculate the size of the new table based on combined variables
        int newTableSize = 1;
        for (Variable var : newVariables) {
            newTableSize *= var.outcomes.size();
        }

        // Initialize a new table to hold multiplied values
        String[][] newTable = new String[newTableSize + 1][newVariables.size() + 1];

        // Populate the header row of the new table with variable names and "pro" for probability
        for (int j = 0; j < newVariables.size(); j++) {
            newTable[0][j] = newVariables.get(j).name;
        }
        newTable[0][newTable[0].length - 1] = "Probability";

        // Identify common variables between this factor and the other factor
        ArrayList<Variable> commonVariables = new ArrayList<>();
        for (Variable var : newVariables) {
            if (this.vars.contains(var.name) && other.vars.contains(var.name)) {
                commonVariables.add(var);
            }
        }

        // Iterate through each row in this factor's table
        for (int i = 1; i < other.table.length; i++) {
            String[] thisLine = other.table[i];
            double prob1 = Double.parseDouble(thisLine[thisLine.length - 1]);

            // Iterate through each row in the other factor's table
            for (int j = 1; j < this.table.length; j++) {
                String[] otherLine = this.table[j];
                double prob2 = Double.parseDouble(otherLine[otherLine.length - 1]);

                // Check if the current rows can be multiplied (consistent assignments for common variables)
                boolean consistent = true;
                for (Variable var : commonVariables) {
                    int thisIndex = lolOther.indexOf(var);
                    int otherIndex = lolThis.indexOf(var);
                    if (!thisLine[thisIndex].equals(otherLine[otherIndex])) {
                        consistent = false;
                        break;
                    }
                }

                if (consistent) {
                    // Create a new row in the new table for the multiplied result
                    String[] newRow = new String[newTable[0].length];

                    // Fill the new row according to the new variable order
                    for (int k = 0; k < newVariables.size(); k++) {
                        Variable newVar = newVariables.get(k);
                        if (other.vars.contains(newVar.name)) {
                            newRow[k] = thisLine[lolOther.indexOf(newVar)];
                        } else if (this.vars.contains(newVar.name)) {
                            newRow[k] = otherLine[lolThis.indexOf(newVar)];
                        }
                    }

                    // Multiply the probabilities
                    newRow[newTable[0].length - 1] = String.valueOf(prob1 * prob2);

                    // Add the new row to the new table
                    newTable[numMultiplies + 1] = newRow;
                    numMultiplies++;
                }
            }
        }

        // Update this factor's variables and table with the new variables and table
        this.vars = new ArrayList<>();

        this.table = newTable;

        for(int j = 0; j < this.table[0].length-1; j++)
        {
            this.vars.add(this.table[0][j]);
        }

        return numMultiplies;
    }

    public int sumUp(Variable var) {
        // Create a list of variables excluding the given variable
        ArrayList<Variable> newVariables = new ArrayList<>();

        for(String variable : this.vars) {
            newVariables.add(VariableElimination.variablesMap.get(variable));
        }

        newVariables.remove(var);

        // Calculate the size of the new table after summing up
        int newTableSize = 1;
        for (Variable v : newVariables) {
            newTableSize *= v.outcomes.size();
        }

        // Initialize a new table for the summed-up factor
        String[][] newTable = new String[newTableSize + 1][newVariables.size() + 1];

        // Populate the header row of the new table with variable names and "Probability"
        for (int j = 0; j < newVariables.size(); j++) {
            newTable[0][j] = newVariables.get(j).name;
        }
        newTable[0][newTable[0].length - 1] = "Probability";

        // Map to store sums of probabilities for each assignment of newVariables
        Map<String, Double> sumMap = new LinkedHashMap<>();

        // Iterate through each row in the current factor's table
        for (int i = 1; i < this.table.length; i++) {
            String[] row = this.table[i];

            // Build key for the assignment of variables excluding the given variable
            StringBuilder sb = new StringBuilder();
            for (Variable newVar : newVariables) {
                int originalIndex = this.vars.indexOf(newVar.name);
                sb.append(row[originalIndex]);
                sb.append(",");  // Use comma as a delimiter for better separation
            }
            String assignmentKey = sb.toString();

            // Update the sum for this assignment key
            double currentProbability;
            try {
                currentProbability = Double.parseDouble(row[row.length - 1]);
            } catch (NumberFormatException e) {
                // Handle parsing errors gracefully
                continue;  // Skip this row if parsing fails
            }

            double sum = sumMap.getOrDefault(assignmentKey, 0.0);
            sum += currentProbability;
            sumMap.put(assignmentKey, sum);
        }

        // Populate the new table with summed probabilities
        int newIndex = 1;
        for (Map.Entry<String, Double> entry : sumMap.entrySet()) {
            String assignmentKey = entry.getKey();
            double summedProbability = entry.getValue();

            String[] newRow = new String[newTable[0].length];
            String[] assignmentValues = assignmentKey.split(",");

            for (int j = 0; j < newVariables.size(); j++) {
                newRow[j] = assignmentValues[j];
            }
            newRow[newTable[0].length - 1] = String.format(String.valueOf(summedProbability));
            newTable[newIndex++] = newRow;
        }

        // Update this factor's variables and table with the new variables and table
        this.vars = new ArrayList<>();

        this.table = newTable;

        for(int j = 0; j < this.table[0].length-1; j++) {
            this.vars.add(this.table[0][j]);
        }

        // Return the number of additions performed
        return this.table.length - 1;
    }

    public int normalize() {
        double sum = 0;
        int numOfAdds = 0;
        int index = 0;
        for (String[] row : table) {
            if(index == 0){
                index++;
                continue;
            }
            try {
                sum += Double.parseDouble(row[this.table[0].length - 1]);
                numOfAdds++;
            } catch (NumberFormatException e) {
                // Skip invalid values
            }
        }
        for (int i = 1; i < table.length; i++) {
            try {
                table[i][this.table[0].length - 1] = String.valueOf(Double.parseDouble(table[i][this.table[0].length - 1]) / sum);
            } catch (NumberFormatException e) {
                // Skip invalid values
            }
        }
        return numOfAdds-1;
    }

    public void printFactor()
    {
        System.out.print("Factor for: ");
        for(String variable : this.vars)
            System.out.print(variable + " ");
        System.out.print("\n");

        // Print the header
        for (String[] row : this.table)
        {
            for (String cell : row)
            {
                System.out.print(cell + "\t");
            }
            System.out.println();
        }
    }
}
