import Model.*;
import io.vertx.core.Future;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         <p>
 *         Mock for the voting database.
 */
public class VotingDBMock implements AsyncVotingStore {
    private ArrayList<Voting> votings = new ArrayList<>();
    private ArrayList<VoteResult> votes = new ArrayList<>();

    public VotingDBMock() {
        ArrayList<Query> options = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        values.add("a");
        values.add("b");
        values.add("c");

        options.add(
                new Query()
                        .setName("q1")
                        .setValues(values)
        );

        options.add(
                new Query()
                        .setName("q2")
                        .setValues(values)
        );

        votings.add(new Voting()
                        .setId("id")
                        .setTopic("Vote #1")
                        .setOwner("gosu")
                        .setOptions(options)
        );
        // upserted by mongodb
        votes.add(new VoteResult("id"));

        ArrayList<Option> voteList = new ArrayList<>();
        Option option = new Option();
        option.setKey("my_key");
        option.setOption("the_option");
        option.setValue("the_value");
        voteList.add(option);

        vote(Future.future(), new Vote().setId("id").setOptions(voteList));
    }

    @Override
    public void vote(Future<Void> future, Vote vote) {
        for (int i = 0; i < votes.size(); i++) {
            if (votes.get(i).getId().equals(vote.getId()))
                for (Option option : vote.getOptions())
                    votes.get(i).getVotes().add(option);
        }

        future.complete();
    }

    @Override
    public void get(Future<Voting> future, String id) {
        boolean found = false;

        for (int i = 0; i < votings.size(); i++) {
            if (votings.get(i).getId().equals(id)) {
                future.complete(votings.get(i));
                found = true;
            }
        }

        if (!found)
            future.fail(new AsyncVotingStoreMissingVotingException());
    }

    @Override
    public void results(Future<VoteResult> future, String id) {
        future.complete(votes.get(0));
    }

    @Override
    public void add(Future<Void> future, Voting voting) {
        votings.add(voting);
        future.complete();
    }

    @Override
    public void terminate(Future<Void> future, Voting voting) {
        for (int i = 0; i < votings.size(); i++) {
            if (votings.get(i).getId().equals(voting.getId())) {
                votings.remove(i);
                break;
            }
        }
        future.complete();
    }


}
