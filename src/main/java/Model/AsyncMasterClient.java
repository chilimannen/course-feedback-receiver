package Model;

/**
 * @author Robin Duda
 *         <p>
 *         A connection to a master.
 */
public interface AsyncMasterClient {
    /**
     * Upload vote results to master.
     *
     * @param votings store where the results may be fetched.
     * @param voting  to be uploaded when its duration is out.
     */
    void upload(AsyncVotingStore votings, Voting voting);
}
