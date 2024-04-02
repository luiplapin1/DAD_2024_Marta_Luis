package es.us.lsi.dad;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RestServer extends AbstractVerticle {

    private Map<Integer, Sensor_humedad_Entity> sensors = new HashMap<>();
    private Map<Integer, Actuador_Entity> actuadores = new HashMap<>();
    
    private Gson gson;

    public void start(Promise<Void> startFuture) {
        // Creating some synthetic data
        createSomeData(25);

        // Instantiating a Gson serialize object using specific date format
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();


        Router router = Router.router(vertx);

  
        vertx.createHttpServer().requestHandler(router::handle).listen(8084, result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });


        router.route("/api/sensor*").handler(BodyHandler.create());
//        router.get("/api/sensor").handler(this::getAllWithParams);
        router.get("/api/sensor/all").handler(this::getAllSensors);
        router.get("/api/sensor/:sensorid").handler(this::getSensor);
        router.post("/api/sensor").handler(this::addSensor);
        router.delete("/api/sensor/:id").handler(this::deleteSensor);
        router.put("/api/sensor/:id").handler(this::updateSensor);
        
        router.route("/api/actuador*").handler(BodyHandler.create());
//        router.get("/api/actuador").handler(this::getAllWithParams);
        router.get("/api/actuador/all").handler(this::getAllActuadores);
        router.get("/api/actuador/:actuadorid").handler(this::getActuador);
        router.post("/api/actuador").handler(this::addActuador);
        router.delete("/api/actuador/:id").handler(this::deleteActuador);
        router.put("/api/actuador/:id").handler(this::updateActuador);
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        try {
            sensors.clear();
            stopPromise.complete();
        } catch (Exception e) {
            stopPromise.fail(e);
        }
        super.stop(stopPromise);
    }

 // Sensor Endpoints
    private void getAllSensors(RoutingContext routingContext) {
        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .setStatusCode(200)
                .end(gson.toJson(new SensorEntityListWrapper(sensors.values())));
    }

    private void getSensor(RoutingContext routingContext) {
        int id = 0;
        try {
            id = Integer.parseInt(routingContext.request().getParam("sensorid"));

            if (sensors.containsKey(id)) {
                Sensor_humedad_Entity sensor = sensors.get(id);
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(sensor != null ? gson.toJson(sensor) : "");
            } else {
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(204)
                        .end();
            }
        } catch (Exception e) {
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(204)
                    .end();
        }
    }

    private void addSensor(RoutingContext routingContext) {
        final Sensor_humedad_Entity sensor = gson.fromJson(routingContext.getBodyAsString(), Sensor_humedad_Entity.class);
        sensors.put(sensor.getId(), sensor);
        routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(gson.toJson(sensor));
    }

    private void deleteSensor(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("id"));
        if (sensors.containsKey(id)) {
            Sensor_humedad_Entity sensor = sensors.get(id);
            sensors.remove(id);
            routingContext.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(gson.toJson(sensor));
        } else {
            routingContext.response()
                    .setStatusCode(204)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end();
        }
    }

    private void updateSensor(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("id"));
        Sensor_humedad_Entity sensor = sensors.get(id);
        final Sensor_humedad_Entity updatedSensor = gson.fromJson(routingContext.getBodyAsString(), Sensor_humedad_Entity.class);
        sensor.setTimestamp(updatedSensor.getTimestamp());
        sensor.setHumedad(updatedSensor.getHumedad());
        sensor.setTemperatura(updatedSensor.getTemperatura());
        sensors.put(sensor.getId(), sensor);
        routingContext.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(gson.toJson(sensor));
    }

    // Actuador Endpoints
 // Actuador Endpoints
    private void getAllActuadores(RoutingContext routingContext) {
        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .setStatusCode(200)
                .end(gson.toJson(new ActuadorEntityListWrapper(actuadores.values())));
    }

    private void getActuador(RoutingContext routingContext) {
        int id = 0;
        try {
            id = Integer.parseInt(routingContext.request().getParam("actuadorid"));

            if (actuadores.containsKey(id)) {
                Actuador_Entity actuador = actuadores.get(id);
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(actuador != null ? gson.toJson(actuador) : "");
            } else {
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(204)
                        .end();
            }
        } catch (Exception e) {
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(204)
                    .end();
        }
    }

    private void addActuador(RoutingContext routingContext) {
        final Actuador_Entity actuador = gson.fromJson(routingContext.getBodyAsString(), Actuador_Entity.class);
        actuadores.put(actuador.getidActuador(), actuador);
        routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(gson.toJson(actuador));
    }

    private void deleteActuador(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("id"));
        if (actuadores.containsKey(id)) {
            Actuador_Entity actuador = actuadores.get(id);
            actuadores.remove(id);
            routingContext.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(gson.toJson(actuador));
        } else {
            routingContext.response()
                    .setStatusCode(204)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end();
        }
    }

    private void updateActuador(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("id"));
        Actuador_Entity actuador = actuadores.get(id);
        final Actuador_Entity updatedActuador = gson.fromJson(routingContext.getBodyAsString(), Actuador_Entity.class);
        actuador.setTimestamp(updatedActuador.getTimestamp());
        actuador.setActivo(updatedActuador.getActivo());
        actuador.setEncendido(updatedActuador.getEncendido());
        actuadores.put(actuador.getidActuador(), actuador);
        routingContext.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(gson.toJson(actuador));
    }


    private void createSomeData(int number) {
        Random rnd = new Random();
        IntStream.range(0, number).forEach(elem -> {
            int id = elem + 1; // Ajustamos el ID para que comience en 1 en lugar de 0
            int nPlaca=rnd.nextInt();
            long timestamp = Calendar.getInstance().getTimeInMillis() + rnd.nextInt(1000); // Agregamos un número aleatorio al timestamp
            float humedad = rnd.nextFloat() * 100; // Generamos un valor aleatorio entre 0 y 100 para la humedad
            float temperatura = rnd.nextFloat() * 50; // Generamos un valor aleatorio entre 0 y 50 para la temperatura
            sensors.put(id, new Sensor_humedad_Entity(id, nPlaca, timestamp, humedad, temperatura));
        });
    }


}
