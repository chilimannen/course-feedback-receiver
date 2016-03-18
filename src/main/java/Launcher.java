import Controller.WebServer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;


/**
 * Created by Robin on 2016-03-18.
 */
public class Launcher extends AbstractVerticle {

    public void start(Future<Void> future) {
        vertx.deployVerticle(
                WebServer.class.getName(),
                new DeploymentOptions().setInstances(Runtime.getRuntime().availableProcessors()),

                result -> {
                    if (result.succeeded())
                        future.complete();
                    else
                        future.fail(result.cause());
                });
    }

    public void stop(Future<Void> future) {
        future.complete();
    }
}
