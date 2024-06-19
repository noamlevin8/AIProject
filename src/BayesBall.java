import java.util.ArrayList;

public class BayesBall {
    public boolean bayesBall(Variable start,Variable end, ArrayList<Variable> evidence) {
        ArrayList<Variable> visited=new ArrayList<>();

        for(Variable v:start.parents)
            for(Variable v2:end.parents)
                if(v==v2 && !evidence.contains(v))
                    return false;

        for(Variable v:start.children)
            for(Variable v2:end.children)
                if(v==v2 && evidence.contains(v))
                    return false;
        return areIndependent(start,end,visited,false,evidence);
    }
    private static boolean areIndependent(Variable start, Variable end, ArrayList<Variable> visited, boolean comingFromChild, ArrayList<Variable> evidence) {
        visited.add(start);

        if(start==end)
            return false;

        if(evidence.contains(start) && comingFromChild)
            return true;

        else if(evidence.contains(start)){
            for(int i=0;i<start.parents.size();i++) {
                    if (!areIndependent(start.parents.get(i), end, visited, true, evidence))
                        return false;
            }
        }

        else if(!evidence.contains(start) && comingFromChild){
            for(int i=0;i<start.children.size();i++)
                if(!visited.contains(start.children.get(i)))
                    if(!areIndependent(start.children.get(i),end,visited,false,evidence))
                        return false;

            for(int i=0;i<start.parents.size();i++)
                    if(!areIndependent(start.parents.get(i),end,visited,true,evidence))
                        return false;
        }

        else if(!evidence.contains(start) && !comingFromChild){
            for(int i=0;i<start.children.size();i++)
                if(!visited.contains(start.children.get(i)))
                    if(!areIndependent(start.children.get(i),end,visited,false,evidence))
                        return false;
        }

        return true;
    }
}
