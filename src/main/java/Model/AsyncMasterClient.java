package Model;

/**
 * @author Robin Duda
 *         <p>
 *         A connection to a master.
 */
public interface AsyncMasterClient {
    /**
     * Command a master to create a vote.
     *
     * @param votings store where the resultsl may be fetched.
     * @param voting  to be created.
     */
    void upload(AsyncVotingStore votings, Voting voting);
}
