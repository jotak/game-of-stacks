package demo.gos.ui;

import demo.gos.common.Commons;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static demo.gos.common.Commons.getKafkaConfig;
import static demo.gos.common.Commons.getUiPort;

public class UIVerticle extends AbstractVerticle {

    private final Map<String, GameObject> gameObjects = new HashMap<>();

    public UIVerticle() {
    }

    public static void main(String[] args) {
        Vertx.clusteredVertx(Commons.vertxOptions().setClustered(true), ar -> ar.result().deployVerticle(new UIVerticle()));
    }

    @Override
    public void start() {
        HttpServerOptions serverOptions = new HttpServerOptions().setPort(getUiPort());

        Router router = Router.router(vertx);

        // Allow events for the designated addresses in/out of the event bus bridge
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

        router.get("/health").handler(ctx -> ctx.response().end());

        // TODO: replace http API with eventbus messages
        // Listen to objects creation
        router.post("/display").handler(this::displayGameObject);
        router.post("/displayMore").handler(this::displayGameObjects);

        // Create a router endpoint for the static content.
        router.route().handler(StaticHandler.create());

        // Start the web server and tell it to use the router to handle requests.
        vertx.createHttpServer().requestHandler(router)
                .listen(serverOptions.getPort(), serverOptions.getHost());

        EventBus eb = vertx.eventBus();
        eb.consumer("init-session", msg -> {
            // Send all game objects already registered
            List<JsonObject> objects = gameObjects.entrySet().stream().map(entry -> {
                JsonObject json = new JsonObject()
                        .put("id", entry.getKey())
                        .put("style", entry.getValue().style)
                        .put("text", entry.getValue().text);
                if (entry.getValue().x != null) {
                    json.put("x", entry.getValue().x)
                            .put("y", entry.getValue().y);
                }
                return json;
            }).collect(Collectors.toList());
            msg.reply(new JsonArray(objects));
        });


        // use producer for interacting with Apache Kafka
        KafkaProducer<String, String> producer = KafkaProducer.create(vertx, getKafkaConfig());

        eb.consumer("play", msg -> producer.write(KafkaProducerRecord.create("game", "play")));
        eb.consumer("pause", msg -> producer.write(KafkaProducerRecord.create("game", "pause")));
        eb.consumer("reset", msg -> producer.write(KafkaProducerRecord.create("game", "reset")));


        // Objects timeout
        vertx.setPeriodic(5000, loopId -> {
            long now = System.currentTimeMillis();
            gameObjects.entrySet().removeIf(entry -> {
                if (now - entry.getValue().lastCheck > 2000) {
                    eb.publish("removeGameObject", entry.getKey());
                    return true;
                }
                return false;
            });
        });
    }

    private void displayGameObject(RoutingContext ctx) {
        ctx.request().bodyHandler(buf -> {
            JsonObject json = buf.toJsonObject();
            ctx.response().end();
            gameObjects.compute(json.getString("id"), (key, go) -> {
                GameObject out = (go == null) ? new GameObject() : go;
                boolean changed = out.mergeWithJson(json);
                if (changed) {
                    vertx.eventBus().publish("displayGameObject", json);
                }
                return out;
            });
        });
    }

    private void displayGameObjects(RoutingContext ctx) {
        ctx.request().bodyHandler(buf -> {
            JsonArray arr = buf.toJsonArray();
            ctx.response().end();
            arr.forEach(o -> {
                JsonObject json = (JsonObject) o;
                gameObjects.compute(json.getString("id"), (key, go) -> {
                    GameObject out = (go == null) ? new GameObject() : go;
                    boolean changed = out.mergeWithJson(json);
                    if (changed) {
                        vertx.eventBus().publish("displayGameObject", json);
                    }
                    return out;
                });
            });
        });
    }

    private static class GameObject {
        private String style;
        private String text;
        private Double x;
        private Double y;
        private long lastCheck;

        GameObject() {
        }

        boolean mergeWithJson(JsonObject json) {
            lastCheck = System.currentTimeMillis();
            boolean changed = false;
            if (json.containsKey("style")) {
                String style = json.getString("style");
                changed = !style.equals(this.style);
                this.style = style;
            }
            if (json.containsKey("text")) {
                String text = json.getString("text");
                changed |= !text.equals(this.text);
                this.text = text;
            }
            if (json.containsKey("x")) {
                Double x = json.getDouble("x");
                changed |= !x.equals(this.x);
                this.x = x;
            }
            if (json.containsKey("y")) {
                Double y = json.getDouble("y");
                changed |= !y.equals(this.y);
                this.y = y;
            }
            return changed;
        }
    }
}
