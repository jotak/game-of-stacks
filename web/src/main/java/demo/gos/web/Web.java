package demo.gos.web;

import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class Web {
    @Inject
    private Vertx vertx;

    @Inject
    @Channel("controls")
    private Emitter<JsonObject> gameEmitter;

    public void init(@Observes Router router) {
        BridgeOptions opts = new BridgeOptions()
            .addOutboundPermitted(new PermittedOptions().setAddress("displayGameObject"))
            .addOutboundPermitted(new PermittedOptions().setAddress("endGame"))
            .addInboundPermitted(new PermittedOptions().setAddress("play"))
            .addInboundPermitted(new PermittedOptions().setAddress("pause"))
            .addInboundPermitted(new PermittedOptions().setAddress("reset"));

        // Create the event bus bridge and add it to the router.
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        sockJSHandler.bridge(opts);
        router.route("/eventbus/*").handler(sockJSHandler);

        EventBus eb = vertx.eventBus();
        eb.consumer("play", msg -> publishGameEvent("play"));
        eb.consumer("pause", msg -> publishGameEvent("pause"));
        eb.consumer("reset", msg -> publishGameEvent("reset"));
    }

    @Incoming("display")
    public void display(JsonArray arr) {
        vertx.eventBus().publish("displayGameObject", arr);
    }

    @Incoming("gameover")
    public void gameover(JsonObject o) {
        vertx.eventBus().publish("endGame", new JsonObject().put("winner", o.getString("winner")));
    }

    private void publishGameEvent(String type) {
        System.out.println("Publishing: " + type);
        gameEmitter.send(new JsonObject().put("type", type));
    }
}
