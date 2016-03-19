package Model;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;

/**
 * @author Robin Duda
 * <p/>
 * Async voting store implementation using MongoDB.
 */
public class VotingDB implements AsyncVotingStore {
    private static final String VOTES = "receiver_votes";
    private static final String METADATA = "receiver_metadata";
    private MongoClient client;

    public VotingDB(MongoClient client) {
        this.client = client;
    }


    @Override
    public void vote(Future<Void> future, Vote vote) {
        JsonObject query = new JsonObject().put("id", vote.getId());
        UpdateOptions options = new UpdateOptions().setUpsert(true);
        JsonObject data = new JsonObject()
                .put("$pushAll", new JsonObject()
                        .put("votes", new JsonArray(Serializer.pack(vote.getOptions()))));

        client.updateWithOptions(VOTES, query, data, options, result -> {
            if (result.succeeded())
                future.complete();
            else {
                future.fail(result.cause());
            }
        });
    }

    @Override
    public void get(Future<Voting> future, String id) {
        JsonObject query = new JsonObject().put("id", id);

        client.findOne(METADATA, query, null, result -> {
            if (result.succeeded() && result.result() != null)
                future.complete((Voting) Serializer.unpack(result.result(), Voting.class));
            else
                future.fail(new AsyncVotingStoreMissingVotingException());
        });
    }

    @Override
    public void results(Future<VoteResult> future, String id) {
        JsonObject query = new JsonObject().put("id", id);

        client.findOne(VOTES, query, null, result -> {
            if (result.succeeded())
                future.complete((VoteResult) Serializer.unpack(result.result(), VoteResult.class));
            else
                future.fail(result.cause());
        });
    }

    @Override
    public void add(Future<Void> future, Voting voting) {
        JsonObject query = new JsonObject(Serializer.pack(voting));

        client.save(METADATA, query, result -> {
            if (result.succeeded()) {
                future.complete();
            } else
                future.fail(result.cause());
        });
    }

    @Override
    public void terminate(Future<Void> future, Voting voting) {
        JsonObject query = new JsonObject().put("id", voting.getId());

        client.removeOne(VOTES, query, result -> {
            if (result.succeeded())
                future.complete();
            else
                future.fail(result.cause());
        });

        client.removeOne(METADATA, query, result -> {
            if (result.succeeded())
                future.complete();
            else
                future.fail(result.cause());
        });
    }
}
