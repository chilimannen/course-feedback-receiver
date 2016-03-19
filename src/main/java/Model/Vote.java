package Model;

import java.util.ArrayList;

/**
 * Created by Robin on 2016-03-19.
 */
public class Vote {
    private String id;
    private ArrayList<Option> options = new ArrayList<>();

    public String getId() {
        return id;
    }

    public Vote setId(String id) {
        this.id = id;
        return this;
    }

    public ArrayList<Option> getOptions() {
        return options;
    }

    public Vote setOptions(ArrayList<Option> options) {
        this.options = options;
        return this;
    }
}
