package Controller;

import Configuration.Configuration;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.JadeTemplateEngine;

/**
 * Created by Robin on 2016-03-18.
 */
public class WebServer implements Verticle {
    private Vertx vertx;

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
    }

    @Override
    public void start(Future<Void> future) throws Exception {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        setTemplating(router);
        setResources(router);
        setCatchAll(router);

        server.requestHandler(router::accept).listen(Configuration.WEB_PORT);
        future.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {

    }

    private void setTemplating(Router router) {
        JadeTemplateEngine jade = JadeTemplateEngine.create();

        router.route("/").handler(context -> {
            jade.render(context, "templates/index", result -> {
                if (result.succeeded())
                    context.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html").end(result.result());
                else
                    context.fail(result.cause());
            });
        });
    }

    private void setResources(Router router) {
        router.route("/resources/*").handler(StaticHandler.create()
                .setCachingEnabled(true));
    }

    private void setCatchAll(Router router) {
        router.route().handler(context -> {
            HttpServerResponse response = context.response();
            response.setStatusCode(404);
            response.putHeader("content-type", "application/json");
            response.end("{\"page\" : 404}");
        });
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }
}
