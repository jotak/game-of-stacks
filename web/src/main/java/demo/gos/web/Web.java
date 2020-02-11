package demo.gos.web;

import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.smallrye.reactive.messaging.annotations.Merge;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Objects;

@ApplicationScoped
public class Web {
    @Inject
    private Vertx vertx;

    @Inject
    private EventBus eventBus;

    @Inject
    @Channel("game")
    private Emitter<JsonObject> gameEmitter;

    public void init(@Observes Router router) {
        BridgeOptions opts = new BridgeOptions()
            .addOutboundPermitted(new PermittedOptions().setAddress("displayGameObject"))
            .addOutboundPermitted(new PermittedOptions().setAddress("removeGameObject"))
            .addOutboundPermitted(new PermittedOptions().setAddress("endGame"))
            .addInboundPermitted(new PermittedOptions().setAddress("init-session"))
            .addInboundPermitted(new PermittedOptions().setAddress("play"))
            .addInboundPermitted(new PermittedOptions().setAddress("pause"))
            .addInboundPermitted(new PermittedOptions().setAddress("reset"));

        // Create the event bus bridge and add it to the router.
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        sockJSHandler.bridge(opts);
        router.route("/eventbus/*").handler(sockJSHandler);

        eventBus.consumer("play", msg -> publishGameEvent("play"));
        eventBus.consumer("pause", msg -> publishGameEvent("pause"));
        eventBus.consumer("reset", msg -> publishGameEvent("reset"));

    }

    @Incoming("display")
    public void display(JsonObject o) {
        displayGameObject(o);
    }

    @Incoming("game")
    @Merge(Merge.Mode.MERGE)
    public void game(JsonObject o) {
        if(Objects.equals(o.getString("type"), "end")) {
            vertx.eventBus().publish("endGame", new JsonObject().put("winner", o.getString("winner")));
        }
    }

    private void publishGameEvent(String type) {
        gameEmitter.send(new JsonObject().put("type", type));
    }

    private void displayGameObject(JsonObject json) {
        vertx.eventBus().publish("displayGameObject", json);
    }
}
