import java.util.ArrayList;

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
}
