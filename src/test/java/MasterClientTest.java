import Configuration.Configuration;
import Controller.WebServer;
import Model.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
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

/**
 * @author Robin Duda
 *
 * Tests the client to the master.
 */

@RunWith(VertxUnitRunner.class)
public class MasterClientTest {
    private Vertx vertx;
    private VotingDBMock database = new VotingDBMock();

    @Rule
    public Timeout timeout = Timeout.seconds(10);

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new WebServer(database), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    /**
     * Ensure that voting results are transmitted to master after the voting expires.
     */
    @Test
    public void shouldUploadVoteToMaster(TestContext context) {
        Async async = context.async();

        vertx.createHttpServer().requestHandler(request -> {
            request.bodyHandler(body -> {
                VoteBallot result = (VoteBallot) Serializer.unpack(body.toJsonObject().getJsonObject("voting"), VoteBallot.class);
                Token token = (Token) Serializer.unpack(body.toJsonObject().getJsonObject("token"), Token.class);

                context.assertTrue(new TokenFactory(Configuration.SERVER_SECRET).verifyToken(token));
                context.assertEquals("id", result.getId());
                context.assertEquals(1, result.getVotes().size());

                async.complete();
            });

        }).listen(Configuration.MASTER_PORT);


        vertx.createHttpClient().post(Configuration.WEB_PORT, "localhost", "/api/create", result -> {
            context.assertEquals(HttpResponseStatus.OK.code(), result.statusCode());

        }).end(new JsonObject()
                .put("token", APITest.getServerToken())
                .put("voting", APITest.getVotingConfiguration())
                .encode());
    }
}
