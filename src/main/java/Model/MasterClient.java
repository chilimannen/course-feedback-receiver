package Model;

import Configuration.Configuration;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 *         <p>
 *         A client used to communicate with masters.
 */
public class MasterClient implements AsyncMasterClient {
    private TokenFactory serverToken = new TokenFactory(Configuration.SERVER_SECRET);
    private Vertx vertx;

    public MasterClient(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void upload(AsyncVotingStore votings, Voting voting) {
        vertx.setTimer(getMillisDuration(voting), event -> {
            Future<VoteBallot> storage = Future.future();

            storage.setHandler(result -> {
                vertx.createHttpClient().post(Configuration.MASTER_PORT, "localhost", "/api/upload", handler -> {
                }).end(
                        new JsonObject()
                                .put("token", getServerToken())
                                .put("voting", Serializer.json(result.result()))
                                .encode()
                );
            });

            votings.results(storage, voting.getId());
        });
    }

    private long getMillisDuration(Voting voting) {
        return voting.getDuration().getEnd() - voting.getDuration().getBegin();
    }

    private JsonObject getServerToken() {
        return new JsonObject(Json.encode(new Token(serverToken, Configuration.SERVER_NAME)));
    }
}
