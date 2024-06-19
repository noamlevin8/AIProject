import java.util.ArrayList;
import java.util.List;


public class Variable {
    String name;
    List<String> outcomes = new ArrayList<>();
    List<Variable> parents = new ArrayList<>();
    List<Variable> children = new ArrayList<>();
    String[][] cpt;


    Variable(String name) {
        this.name = name;
    }

    void addOutcome(String outcome) {
        outcomes.add(outcome);
    }
    void addParent(Variable parent) {
        parents.add(parent);
    }
    void addChildren(Variable child) {
        children.add(child);
    }
    void addCPT(String[][] cpt) {this.cpt = cpt;}

    @Override
    public String toString() {
        String parentsOfV = "{";
        String childrenOfV = "{";

        for(Variable v : parents)
            parentsOfV += v.name + ", ";
        parentsOfV += "}";

        for(Variable v : children)
            childrenOfV += v.name + ", ";
        childrenOfV += "}";

        return "Variable{" +
                "name='" + name + '\'' +
                ", outcomes=" + outcomes + '\'' +
                ", parents=" + parentsOfV + '\'' +
                ", children=" + childrenOfV +
                '}';
    }
}