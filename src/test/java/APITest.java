import Configuration.Configuration;
import Controller.WebServer;
import Model.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Instant;

/**
 * @author Robin Duda
 *         <p>
 *         Tests the api methods for the controller service.
 *         <p>
 *         /vote - send a vote to the server.
 *         /get - get voting details.
 *         /add - create a new voting by the master
 *         /terminate - remove a voting and everything attached to it by the master.
 */

@RunWith(VertxUnitRunner.class)
public class APITest {
    private Vertx vertx;
    private static TokenFactory tokenFactory = new TokenFactory(Configuration.SERVER_SECRET);

    @Rule
    public Timeout timeout = Timeout.seconds(15);

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new WebServer(new VotingDBMock()), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testSendVote(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient().post(Configuration.WEB_PORT, "localhost", "/api/vote", result -> {
            context.assertEquals(HttpResponseStatus.OK.code(), result.statusCode());
            async.complete();
        }).end(
                new JsonObject()
                        .put("id", "id")
                        .put("votes", new JsonArray()
                                        .add(new JsonObject()
                                                .put("key", "key")
                                                .put("option", "option 1")
                                                .put("value", "value b"))
                                        .add(new JsonObject()
                                                .put("key", "key")
                                                .put("option", "option 2")
                                                .put("value", "value c"))
                        ).encode()
        );
    }

    @Test
    public void testGetVoting(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient().getNow(Configuration.WEB_PORT, "localhost", "/api/metadata/id", result -> {
            context.assertEquals(HttpResponseStatus.OK.code(), result.statusCode());

            result.bodyHandler(body -> {
                Voting voting = (Voting) Serializer.unpack(body.toJsonObject(), Voting.class);

                context.assertEquals("gosu", voting.getOwner());
                context.assertEquals("Vote #1", voting.getTopic());
                context.assertEquals("id", voting.getId());
                context.assertEquals(2, voting.getOptions().size());
                context.assertEquals(3, voting.getOptions().get(0).getValues().size());

                async.complete();
            });
        });
    }

    @Test
    public void testGetMissingVoting(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient().getNow(Configuration.WEB_PORT, "localhost", "/api/metadata/0", result -> {
            context.assertEquals(404, result.statusCode());
            async.complete();
        });
    }

    @Test
    public void testAddNewVoting(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient().post(Configuration.WEB_PORT, "localhost", "/api/add", result -> {
            context.assertEquals(HttpResponseStatus.OK.code(), result.statusCode());
            async.complete();
        }).end(new JsonObject()
                .put("token", getServerToken())
                .put("voting", getVotingConfiguration())
                .encode());
    }

    @Test
    public void testAddNewVotingInvalidToken(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient().post(Configuration.WEB_PORT, "localhost", "/api/add", result -> {
            context.assertEquals(HttpResponseStatus.UNAUTHORIZED.code(), result.statusCode());
            async.complete();
        }).end(new JsonObject()
                .put("token", getServerTokenInvalid())
                .put("voting", getVotingConfiguration())
                .encode());
    }

    @Test
    public void testTerminateVoting(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient().post(Configuration.WEB_PORT, "localhost", "/api/terminate", result -> {
            context.assertEquals(HttpResponseStatus.OK.code(), result.statusCode());
            async.complete();
        }).end(new JsonObject()
                .put("token", getServerToken())
                .put("voting", getVotingConfiguration())
                .encode());
    }

    @Test
    public void testTerminateVotingInvalidToken(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient().post(Configuration.WEB_PORT, "localhost", "/api/terminate", result -> {
            context.assertEquals(HttpResponseStatus.UNAUTHORIZED.code(), result.statusCode());
            async.complete();
        }).end(new JsonObject().put("token", getServerTokenInvalid()).encode());
    }

    public static JsonObject getServerToken() {
        return Serializer.json(new Token(tokenFactory, Configuration.SERVER_NAME));
    }

    public static JsonObject getServerTokenInvalid() {
        return Serializer.json(new Token(new TokenFactory("invalid_secret".getBytes()), Configuration.SERVER_NAME));
    }

    public static JsonObject getVotingConfiguration() {
        return new JsonObject()
                .put("topic", "test-voting")
                .put("owner", "someone")
                .put("duration",
                        new JsonObject()
                                .put("begin", Instant.now().getEpochSecond() * 1000)
                                .put("end", (Instant.now().getEpochSecond() + 1) * 1000))
                .put("options",
                        new JsonArray()
                                .add(new JsonObject()
                                        .put("name", "query 1")
                                        .put("values",
                                                new JsonArray()
                                                        .add("value 1")
                                                        .add("value 2")
                                                        .add("value 3")))
                                .add(new JsonObject()
                                        .put("name", "query 2")
                                        .put("values",
                                                new JsonArray()
                                                        .add("value a")
                                                        .add("value b")
                                                        .add("value c"))));
    }
}
