package Controller;

import Configuration.Configuration;
import Model.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * @author Robin Duda
 *         <p>
 *         REST API routes for receiving votings from the master and
 *         to receive votes from users and transmit voting metadata.
 */
public class APIRouter {
    private TokenFactory serverToken = new TokenFactory(Configuration.SERVER_SECRET);
    private AsyncVotingStore votings;
    private AsyncMasterClient client;

    public APIRouter(Router router, AsyncVotingStore votings, AsyncMasterClient client) {
        this.votings = votings;
        this.client = client;

        router.post("/api/create").handler(this::create);
        router.post("/api/vote").handler(this::vote);
        router.post("/api/terminate").handler(this::terminate);
        router.get("/api/metadata/:id").handler(this::metadata);
    }

    private void create(RoutingContext context) {
        HttpServerResponse response = context.response();

        if (authorized(context)) {
            Voting voting = (Voting) Serializer.unpack(context.getBodyAsJson().getJsonObject("voting"), Voting.class);
            Future<Void> future = Future.future();

            future.setHandler(storage -> {
                if (storage.succeeded())
                    response.setStatusCode(HttpResponseStatus.OK.code()).end();
                else
                    response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            });

            client.upload(votings, voting);
            votings.add(future, voting);
        }
    }

    private void terminate(RoutingContext context) {
        HttpServerResponse response = context.response();

        if (authorized(context)) {
            Voting voting = (Voting) Serializer.unpack(context.getBodyAsJson().getJsonObject("voting"), Voting.class);
            Future<Void> future = Future.future();

            future.setHandler(storage -> {
                if (storage.succeeded())
                    response.setStatusCode(HttpResponseStatus.OK.code()).end();
                else
                    response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            });
            votings.terminate(future, voting);
        }
    }


    private void vote(RoutingContext context) {
        HttpServerResponse response = context.response();
        Vote vote = (Vote) Serializer.unpack(context.getBodyAsJson(), Vote.class);
        Future<Void> future = Future.future();

        future.setHandler(storage -> {
            if (storage.succeeded())
                response.setStatusCode(HttpResponseStatus.OK.code()).end();
            else
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
        });

        votings.vote(future, vote);
    }


    private void metadata(RoutingContext context) {
        HttpServerResponse response = context.response();
        String id = context.request().getParam("id");
        Future<Voting> future = Future.future();

        future.setHandler(storage -> {
            if (storage.succeeded())
                response.setStatusCode(HttpResponseStatus.OK.code())
                        .end(Serializer.pack(storage.result()));
            else
                response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
        });

        votings.get(future, id);
    }

    private boolean authorized(RoutingContext context) {
        Token token = (Token) Serializer.unpack(context.getBodyAsJson().getJsonObject("token"), Token.class);
        boolean authorized = serverToken.verifyToken(token);

        if (!authorized)
            context.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();

        return authorized;
    }
}
