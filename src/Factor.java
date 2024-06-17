import java.util.ArrayList;
import java.util.HashMap;
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
        int numMultiplies = 0;

        // Combine variables from both factors
        ArrayList<Variable> newVariables = new ArrayList<>();

        for(String var : this.vars) {
            newVariables.add(VariableElimination.variablesMap.get(var));
        }

        for (String var : other.vars) {
            if (!newVariables.contains(VariableElimination.variablesMap.get(var))) {
                newVariables.add(VariableElimination.variablesMap.get(var));
            }
        }

        // Calculate the size of the new table based on combined variables
        int newTableSize = 1;
        for (Variable var : newVariables) {
            newTableSize *= var.outcomes.size();
        }

        // Initialize a new table to hold multiplied values
        String[][] newTable = new String[newTableSize+1][newVariables.size() + 1];
       // System.out.println("Table size: rows - " + newTable.length + " columns - " + newTable[0].length);

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
            // Iterate through each row in the other factor's table
            for (int j = 1; j < other.table.length; j++) {
                String[] thisLine = this.table[i];
                String[] otherLine = other.table[j];

                // Check if the current rows can be multiplied (consistent assignments for common variables)
                if (areConsistent(other,thisLine, otherLine, commonVariables)) {
                    // Create a new row in the new table for the multiplied result
                    String[] newRow = new String[newTable[0].length];

                    // Copy variable assignments from this factor's table
                    for (int k = 0; k < this.vars.size(); k++) {
                        newRow[k] = thisLine[k];
                    }

                    // Copy variable assignments from the other factor's table
                    int newRowIdx = this.vars.size();
                    for (int k = 0; k < other.vars.size(); k++) {
                        newRow[newRowIdx++] = otherLine[k];
                    }

                    // Multiply the probabilities
                    double prob1 = Double.parseDouble(thisLine[thisLine.length - 1]);
                    double prob2 = Double.parseDouble(otherLine[otherLine.length - 1]);
                    newRow[newTable[0].length - 1] = String.format("%.5f", prob1 * prob2);

                    // Add the new row to the new table
                    newTable[numMultiplies + 1] = newRow;
                    numMultiplies++;
                }
            }
        }

        // Update this factor's variables and table with the new variables and table
        this.vars = new ArrayList<>();

//        for(Variable variable : newVariables){
//            this.vars.add(variable.name);
//        }

        this.table = newTable;

        for(int j = 0; j < this.table[0].length-1; j++)
        {
            //System.out.println("Var name: " + this.table[0][j]);
            this.vars.add(this.table[0][j]);
        }
//        System.out.println("Vars size: " + this.vars.size());
//        this.printFactor();

        return numMultiplies;
    }

    private boolean areConsistent(Factor other,String[] thisLine, String[] otherLine, ArrayList<Variable> commonVariables) {
        // Check if the variable assignments are consistent for common variables
        for (Variable var : commonVariables) {
            int idx1 = this.vars.indexOf(var.name);
            int idx2 = other.vars.indexOf(var.name);
            if (!thisLine[idx1].equals(otherLine[idx2])) {
                return false;
            }
        }
        return true;
    }

    public int marginalize(Variable var) {
        int numAdds = 0;

        // Create a list of variables excluding the marginalized variable
        ArrayList<Variable> newVariables = new ArrayList<>();
        for(String variable : this.vars) {
            newVariables.add(VariableElimination.variablesMap.get(variable));
        }
        newVariables.remove(var);

        // Calculate the size of the new table after marginalization
        int newTableSize = 1;
        for (Variable v : newVariables) {
            newTableSize *= v.outcomes.size();
        }

        // Initialize a new table for the marginalized factor
        String[][] newTable = new String[newTableSize+1][newVariables.size() + 1];

        // Populate the header row of the new table with variable names and "pro" for probability
        for (int j = 0; j < newVariables.size(); j++) {
            newTable[0][j] = newVariables.get(j).name;
        }
        newTable[0][newTable[0].length - 1] = "Probability";

        // Index mapping for the marginalized variable
        int varIndex = this.vars.indexOf(var);

        // Map to store sums of probabilities for each assignment of newVariables
        Map<String, Double> sumMap = new HashMap<>();

        // Iterate through each row in the current factor's table
        for (int i = 1; i < this.table.length; i++) {
            String[] row = this.table[i];

            // Check if the row is valid and has enough columns
            if (row.length <= varIndex || varIndex < 0 || row[varIndex] == null) {
                continue;  // Skip invalid or incomplete rows
            }

            // Build key for the assignment of variables excluding the marginalized variable
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < newVariables.size(); j++) {
                sb.append(row[j]);
            }
            String assignmentKey = sb.toString();

            // Update the sum for this assignment key
            double currentProbability = 0.0;
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
            for (int j = 0; j < newVariables.size(); j++) {
                newRow[j] = String.valueOf(assignmentKey.charAt(j));
            }
            newRow[newTable[0].length - 1] = String.format("%.5f", summedProbability);
            newTable[newIndex++] = newRow;
        }

        // Update this factor's variables and table with the new variables and table
        this.vars = new ArrayList<>();

        this.table = newTable;

        for(int j = 0; j < this.table[0].length-1; j++)
        {
            this.vars.add(this.table[0][j]);
        }

        numAdds=(this.table.length-1)/var.outcomes.size();
        return numAdds;
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

        // Populate the header row of the new table with variable names and "pro" for probability
        for (int j = 0; j < newVariables.size(); j++) {
            newTable[0][j] = newVariables.get(j).name;
        }
        newTable[0][newTable[0].length - 1] = "Probability";

        // Index mapping for the given variable
        //int varIndex = this.vars.indexOf(var);

        // Map to store sums of probabilities for each assignment of newVariables
        Map<String, Double> sumMap = new HashMap<>();

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
            newRow[newTable[0].length - 1] = String.format("%.5f", summedProbability);
            newTable[newIndex++] = newRow;
        }

        // Update this factor's variables and table with the new variables and table
        this.vars = new ArrayList<>();

        this.table = newTable;

        for(int j = 0; j < this.table[0].length-1; j++)
        {
            this.vars.add(this.table[0][j]);
        }
        this.printFactor();

        // Return the number of additions performed
        return this.table.length - 1;
    }

    public int normalize() {
        double sum = 0;
        int numOfAdds = 0;
        for (String[] row : table) {
            try {
                sum += Double.parseDouble(row[this.table[0].length - 1]);
                numOfAdds++;
            } catch (NumberFormatException e) {
                // Skip invalid values
            }
        }
        for (int i = 1; i < table.length; i++) {
            try {
                table[i][this.table[0].length - 1] = String.format("%.5f", Double.parseDouble(table[i][this.table[0].length - 1]) / sum);
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
        System.out.println();
    }
}
