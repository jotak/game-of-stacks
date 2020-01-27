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

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class Web {
    private final Map<String, GameObject> gameObjects = new HashMap<>();

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
            .addInboundPermitted(new PermittedOptions().setAddress("init-session"))
            .addInboundPermitted(new PermittedOptions().setAddress("play"))
            .addInboundPermitted(new PermittedOptions().setAddress("pause"))
            .addInboundPermitted(new PermittedOptions().setAddress("reset"));

        // Create the event bus bridge and add it to the router.
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        sockJSHandler.bridge(opts);
        router.route("/eventbus/*").handler(sockJSHandler);

        eventBus.consumer("init-session", msg -> {
            // Send all game objects already registered
            List<JsonObject> objects = gameObjects.entrySet().stream().map(entry -> {
                JsonObject json = new JsonObject()
                    .put("id", entry.getKey())
                    .put("sprite", entry.getValue().sprite)
                    .put("action", entry.getValue().action)
                    .put("value", entry.getValue().value);
                if (entry.getValue().x != null) {
                    json.put("x", entry.getValue().x)
                        .put("y", entry.getValue().y);
                }
                return json;
            }).collect(Collectors.toList());
            msg.reply(new JsonArray(objects));
        });

        eventBus.consumer("play", msg -> publishGameEvent("play"));
        eventBus.consumer("pause", msg -> publishGameEvent("pause"));
        eventBus.consumer("reset", msg -> publishGameEvent("reset"));

        // Objects timeout
        vertx.setPeriodic(5000, loopId -> {
            long now = System.currentTimeMillis();
            gameObjects.entrySet().removeIf(entry -> {
                if (now - entry.getValue().lastCheck > 2000) {
                    eventBus.publish("removeGameObject", entry.getKey());
                    return true;
                }
                return false;
            });
        });
    }

    @Incoming("display")
    public void display(JsonObject o) {
        displayGameObject(o);
    }

    private void publishGameEvent(String type) {
        gameEmitter.send(new JsonObject().put("type", type));
    }

    private void displayGameObject(JsonObject json) {
        gameObjects.compute(json.getString("id"), (key, go) -> {
            GameObject out = (go == null) ? new GameObject() : go;
            boolean changed = out.mergeWithJson(json);
            if (changed) {
                vertx.eventBus().publish("displayGameObject", json);
            }
            return out;
        });
    }

    private static class GameObject {
        private String action;
        private String sprite;
        private Double x;
        private Double y;
        private Double value;
        private long lastCheck;

        GameObject() {
        }

        boolean mergeWithJson(JsonObject json) {
            lastCheck = System.currentTimeMillis();
            boolean changed = false;
            if (json.containsKey("sprite")) {
                String sprite = json.getString("sprite");
                changed = !Objects.equals(sprite, this.sprite);
                this.sprite = sprite;
            }
            if (json.containsKey("action")) {
                String action = json.getString("action");
                changed |= !Objects.equals(action, this.action);
                this.action = action;
            }
            if (json.containsKey("value")) {
                Double value = json.getDouble("value");
                changed |= !Objects.equals(value, this.value);
                this.value = value;
            }
            if (json.containsKey("x")) {
                Double x = json.getDouble("x");
                changed |= !Objects.equals(x, this.x);
                this.x = x;
            }
            if (json.containsKey("y")) {
                Double y = json.getDouble("y");
                changed |= !Objects.equals(y, this.y);
                this.y = y;
            }
            return changed;
        }
    }
}
