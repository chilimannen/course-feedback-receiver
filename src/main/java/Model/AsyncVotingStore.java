package Model;

import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
public interface AsyncVotingStore {
    /**
     * Inserts a vote into a voting.
     *
     * @param future
     * @param vote   The vote to be inserted.
     */
    void vote(Future<Void> future, Vote vote);

    /**
     * Returns the metadata of a vote.
     *
     * @param future
     * @param id     the id of the vote to get.
     */
    void get(Future<Voting> future, String id);

    /**
     * Get the results of a voting (includes voted keys)
     *
     * @param future
     * @param id     the id of the vote to retrieve.
     */
    void results(Future<VoteBallot> future, String id);

    /**
     * Adds a new voting for record-keeping.
     *
     * @param future
     * @param voting the voting to be registered.
     */
    void add(Future<Void> future, Voting voting);

    /**
     * Terminates a voting, removing all attached content.
     *
     * @param future
     * @param voting the voting to be terminated.
     */
    void terminate(Future<Void> future, Voting voting);
}
