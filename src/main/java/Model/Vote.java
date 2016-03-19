package Model;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *
 * A list of votes and selected options from a client.
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
