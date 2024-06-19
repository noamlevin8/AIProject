import java.util.ArrayList;

public class BayesBall {

    // Main function for running Bayes Ball
    public boolean bayesBall(Variable start,Variable end, ArrayList<Variable> evidence) {
        ArrayList<Variable> visited=new ArrayList<>();

        // Dealing with common cause
        for(Variable v1 : start.parents)
            for(Variable v2 : end.parents)
                if(v1 == v2 && !evidence.contains(v1))
                    return false;

        // Dealing with common effect
        for(Variable v1 : start.children)
            for(Variable v2 : end.children)
                if(v1 == v2 && evidence.contains(v1))
                    return false;

        return areIndependent(start,end,visited,false,evidence);
    }

    // Checking if the start and end variables are independent
    private static boolean areIndependent(Variable start, Variable end, ArrayList<Variable> visited, boolean comingFromChild, ArrayList<Variable> evidence) {
        visited.add(start);

        if(start == end)
            return false;

        if(evidence.contains(start) && comingFromChild)
            return true;

        // Option 1
        else if(evidence.contains(start)){
            for(int i = 0; i < start.parents.size(); i++) {
                    if (!areIndependent(start.parents.get(i), end, visited, true, evidence))
                        return false;
            }
        }

        // Option 2
        else if(!evidence.contains(start) && comingFromChild){
            for(int i=0; i < start.children.size(); i++)
                if(!visited.contains(start.children.get(i)))
                    if(!areIndependent(start.children.get(i),end,visited,false,evidence))
                        return false;

            for(int i=0;i<start.parents.size();i++)
                    if(!areIndependent(start.parents.get(i),end,visited,true,evidence))
                        return false;
        }

        // Option 3
        else if(!evidence.contains(start) && !comingFromChild){
            for(int i=0;i<start.children.size();i++)
                if(!visited.contains(start.children.get(i)))
                    if(!areIndependent(start.children.get(i),end,visited,false,evidence))
                        return false;
        }

        return true;
    }
}
