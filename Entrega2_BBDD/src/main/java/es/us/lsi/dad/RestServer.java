package es.us.lsi.dad;

import java.util.ArrayList;
//import java.util.Calendar;
import java.util.List;
//import java.util.Random;
//import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
//import io.vertx.core.json.JsonArray;
//import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class RestServer extends AbstractVerticle {

	private Gson gson;
	MySQLPool mySqlClient;

	public void start(Promise<Void> startFuture) {
		// tengo que cambiar el puerto y el usuario y contraseña
		MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(3306).setHost("localhost")
				.setDatabase("dadproject").setUser("root").setPassword("root");

		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

		mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);

		// Instantiating a Gson serialize object using specific date format
		Router router = Router.router(vertx);
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

		vertx.createHttpServer().requestHandler(router::handle).listen(8084, result -> {
			if (result.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(result.cause());
			}
		});

		router.route("/api/sensor*").handler(BodyHandler.create());
		router.get("/api/sensor").handler(this::getAllSensorsWithConnection);
		router.get("/api/sensor/all").handler(this::getAllSensors);
		router.get("/api/sensor/:idSensor").handler(this::getSensorById);
		router.post("/api/sensor").handler(this::addSensor);
		router.delete("/api/sensor/:idSensor").handler(this::deleteSensor);
		router.put("/api/sensor/:idSensor").handler(this::updateSensor);

		router.route("/api/actuador*").handler(BodyHandler.create());
		router.get("/api/actuador").handler(this::getAllActuadoresWithConnection);
		router.get("/api/actuador/all").handler(this::getAllActuadores);
		router.get("/api/actuador/:idActuador").handler(this::getActuadorById);
		router.post("/api/actuador").handler(this::addActuador);
		router.delete("/api/actuador/:idActuador").handler(this::deleteActuador);
		router.put("/api/actuador/:idActuador").handler(this::updateActuador);
	}

//    private Date localDateToDate(LocalDate localDate) {
//		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//	}

	public void stop(Promise<Void> stopPromise) throws Exception {
		try {
			stopPromise.complete();
		} catch (Exception e) {
			stopPromise.fail(e);
		}
		super.stop(stopPromise);
	}

	// Sensor Endpoints
	private void getAllSensors(RoutingContext routingContext) {
		mySqlClient.query("SELECT * FROM sensor;").execute(res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				List<List<Object>> result = new ArrayList<>();
				for (Row elem : resultSet) {
					List<Object> sensorData = new ArrayList<>();
					sensorData.add(elem.getInteger("idSensor"));
					sensorData.add(elem.getInteger("nPlaca"));
					sensorData.add(elem.getFloat("humedad"));
					sensorData.add(elem.getLong("timestamp"));
					sensorData.add(elem.getFloat("temperatura"));
					result.add(sensorData);
				}
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.setStatusCode(200).end(result.toString());
			} else {
				routingContext.response().setStatusCode(500)
						.end("Error al obtener los sensores: " + res.cause().getMessage());
			}
		});
	}

	private void getAllSensorsWithConnection(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("SELECT * FROM sensor;").execute(res -> {
					if (res.succeeded()) {
						// Get the result set
						RowSet<Row> resultSet = res.result();
						System.out.println(resultSet.size());
						List<Sensor_humedad_Entity> result = new ArrayList<>();
						for (Row elem : resultSet) {
							result.add(new Sensor_humedad_Entity(elem.getInteger("idSensor"), elem.getInteger("nPlaca"),
									elem.getLong("timestamp"), elem.getFloat("humedad"), elem.getFloat("temperatura")));
						}
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.setStatusCode(200).end(result.toString());
					} else {
						System.out.println("Error: " + res.cause().getLocalizedMessage());
					}
					connection.result().close();
				});
			} else {
				System.out.println(connection.cause().toString());
			}
		});
	}

	private void getSensorById(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			int idSensor = Integer.parseInt(routingContext.request().getParam("idSensor"));
			if (connection.succeeded()) {
				connection.result().preparedQuery("SELECT * FROM sensor WHERE idSensor = ?").execute(Tuple.of(idSensor),
						res -> {
							if (res.succeeded()) {
								// Get the result set
								RowSet<Row> resultSet = res.result();
								System.out.println(resultSet.size());
								List<Sensor_humedad_Entity> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new Sensor_humedad_Entity(elem.getInteger("idSensor"),
											elem.getInteger("nPlaca"), elem.getLong("timestamp"),
											elem.getFloat("humedad"), elem.getFloat("temperatura")));
								}
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());
							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
			}
		});
	}

	private void addSensor(RoutingContext routingContext) {

		// Parseamos el cuerpo de la solicitud HTTP a un objeto Sensor_humedad_Entity
		final Sensor_humedad_Entity sensor = gson.fromJson(routingContext.getBodyAsString(),
				Sensor_humedad_Entity.class);

		// Ejecutamos la inserción en la base de datos MySQL
		mySqlClient
				.preparedQuery(
						"INSERT INTO sensor (idSensor, nPlaca, humedad, timestamp, temperatura) VALUES (?, ?, ?, ?, ?)")
				.execute((Tuple.of(sensor.getId(), sensor.getnPlaca(), sensor.getHumedad(), sensor.getTimestamp(),
						sensor.getTemperatura())), res -> {
							if (res.succeeded()) {
								// Si la inserción es exitosa, respondemos con el sensor creado
								routingContext.response().setStatusCode(201).putHeader("content-type",
										"application/json; charset=utf-8");
							} else {
								// Si hay un error en la inserción, respondemos con el mensaje de error
								System.out.println("Error: " + res.cause().getLocalizedMessage());
							}
						});
	}

	private void deleteSensor(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			int idSensor = Integer.parseInt(routingContext.request().getParam("idSensor"));
			if (connection.succeeded()) {
				connection.result().preparedQuery("DELETE FROM sensor WHERE idSensor = ?").execute(Tuple.of(idSensor),
						res -> {
							if (res.succeeded()) {
								// Get the result set
								RowSet<Row> resultSet = res.result();
								System.out.println(resultSet.size());
								List<Sensor_humedad_Entity> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new Sensor_humedad_Entity(elem.getInteger("idSensor"),
											elem.getInteger("nPlaca"), elem.getLong("timestamp"),
											elem.getFloat("humedad"), elem.getFloat("temperatura")));
								}
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());

							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
			}
		});
	}

	private void updateSensor(RoutingContext routingContext) {
		// Obtenemos el ID del sensor de los parámetros de la solicitud HTTP
		int idSensor = Integer.parseInt(routingContext.request().getParam("idSensor"));

		// Obtenemos el sensor actualizado del cuerpo de la solicitud HTTP
		final Sensor_humedad_Entity updatedSensor = gson.fromJson(routingContext.getBodyAsString(),
				Sensor_humedad_Entity.class);

		// Ejecutamos la actualización en la base de datos MySQL
		mySqlClient
				.preparedQuery(
						"UPDATE sensor SET nPlaca = ?, humedad = ?, timestamp = ?, temperatura = ? WHERE idSensor = ?")
				.execute((Tuple.of(updatedSensor.getnPlaca(), updatedSensor.getHumedad(), updatedSensor.getTimestamp(),
						updatedSensor.getTemperatura(), idSensor)), res -> {
							if (res.succeeded()) {
								// Si la actualización es exitosa, respondemos con el sensor actualizado
								if (res.result().rowCount() > 0) {
									routingContext.response().setStatusCode(200)
											.putHeader("content-type", "application/json; charset=utf-8")
											.end(gson.toJson(updatedSensor));
								}
							} else {
								// Si hay un error en la actualización, respondemos con el código 500 (Error
								// interno del servidor)
								System.out.println("Error: " + res.cause().getLocalizedMessage());
							}
						});
	}

	// Actuador Endpoints

	private void getAllActuadores(RoutingContext routingContext) {
		// RoutingContext routingContext PARAMETRO DE LA FUNCION
//			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
//					.end(gson.toJson(new UserEntityListWrapper(users.values())));
		mySqlClient.query("SELECT * FROM actuador;").execute(res -> {
			if (res.succeeded()) {
				// Get the result set
				RowSet<Row> resultSet = res.result();
				System.out.println(resultSet.size());
				List<Actuador_Entity> result = new ArrayList<>();
				for (Row elem : resultSet) {
					result.add(new Actuador_Entity(elem.getInteger("nPlaca"), elem.getInteger("idActuador"),
							elem.getLong("timestamp"), elem.getBoolean("activo"), elem.getBoolean("encendido")));
				}
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.setStatusCode(200).end(result.toString());
			} else {
				System.out.println("Error: " + res.cause().getLocalizedMessage());
			}
		});
	}

	private void getAllActuadoresWithConnection(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().query("SELECT * FROM actuador;").execute(res -> {
					if (res.succeeded()) {
						// Get the result set
						RowSet<Row> resultSet = res.result();
						System.out.println(resultSet.size());
						List<Actuador_Entity> result = new ArrayList<>();
						for (Row elem : resultSet) {
							result.add(new Actuador_Entity(elem.getInteger("nPlaca"), elem.getInteger("idActuador"),
									elem.getLong("timestamp"), elem.getBoolean("activo"),
									elem.getBoolean("encendido")));
						}
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.setStatusCode(200).end(result.toString());
					} else {
						System.out.println("Error: " + res.cause().getLocalizedMessage());
					}
					connection.result().close();
				});
			} else {
				System.out.println(connection.cause().toString());
			}
		});
	}

	private void getActuadorById(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			int idActuador = Integer.parseInt(routingContext.request().getParam("idActuador"));
			if (connection.succeeded()) {
				connection.result().preparedQuery("SELECT * FROM actuador WHERE idActuador = ?")
						.execute(Tuple.of(idActuador), res -> {
							if (res.succeeded()) {
								// Get the result set
								RowSet<Row> resultSet = res.result();
								System.out.println(resultSet.size());
								List<Actuador_Entity> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new Actuador_Entity(elem.getInteger("nPlaca"),
											elem.getInteger("idActuador"), elem.getLong("timestamp"),
											elem.getBoolean("activo"), elem.getBoolean("encendido")));
								}
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());
							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
			}
		});
	}

	private void addActuador(RoutingContext routingContext) {

		// Parseamos el cuerpo de la solicitud HTTP a un objeto Sensor_humedad_Entity
		final Actuador_Entity actuador = gson.fromJson(routingContext.getBodyAsString(), Actuador_Entity.class);

		// Ejecutamos la inserción en la base de datos MySQL
		mySqlClient.preparedQuery(
				"INSERT INTO actuador (nPlaca, idActuador, timestamp, activo, encendido) VALUES (?, ?, ?, ?, ?)")
				.execute((Tuple.of(actuador.getNPlaca(), actuador.getidActuador(), actuador.getTimestamp(),
						actuador.getActivo(), actuador.getEncendido())), res -> {
							if (res.succeeded()) {
								// Si la inserción es exitosa, respondemos con el sensor creado
								routingContext.response().setStatusCode(201).putHeader("content-type",
										"application/json; charset=utf-8");
							} else {
								// Si hay un error en la inserción, respondemos con el mensaje de error
								System.out.println("Error: " + res.cause().getLocalizedMessage());
							}
						});
	}

	private void deleteActuador(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			int idActuador = Integer.parseInt(routingContext.request().getParam("idActuador"));
			if (connection.succeeded()) {
				connection.result().preparedQuery("DELETE FROM actuador WHERE idActuador = ?")
						.execute(Tuple.of(idActuador), res -> {
							if (res.succeeded()) {
								// Get the result set
								RowSet<Row> resultSet = res.result();
								System.out.println(resultSet.size());
								List<Actuador_Entity> result = new ArrayList<>();
								for (Row elem : resultSet) {
									result.add(new Actuador_Entity(elem.getInteger("nPlaca"),
											elem.getInteger("idActuador"), elem.getLong("timestamp"),
											elem.getBoolean("activo"), elem.getBoolean("encendido")));
								}
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());

							} else {
								System.out.println("Error: " + res.cause().getLocalizedMessage());
							}
							connection.result().close();
						});
			} else {
				System.out.println(connection.cause().toString());
			}
		});
	}

	private void updateActuador(RoutingContext routingContext) {
		// Obtenemos el ID del sensor de los parámetros de la solicitud HTTP
		int idActuador = Integer.parseInt(routingContext.request().getParam("idActuador"));

		// Obtenemos el sensor actualizado del cuerpo de la solicitud HTTP
		final Actuador_Entity updatedActuador = gson.fromJson(routingContext.getBodyAsString(), Actuador_Entity.class);

		// Ejecutamos la actualización en la base de datos MySQL
		mySqlClient
				.preparedQuery(
						"UPDATE actuador SET nPlaca = ?, timestamp = ?, activo = ?, encendido = ? WHERE idActuador = ?")
				.execute((Tuple.of(updatedActuador.getNPlaca(), updatedActuador.getTimestamp(),
						updatedActuador.getActivo(), updatedActuador.getEncendido(), idActuador)), res -> {
							if (res.succeeded()) {
								// Si la actualización es exitosa, respondemos con el sensor actualizado
								if (res.result().rowCount() > 0) {
									routingContext.response().setStatusCode(200)
											.putHeader("content-type", "application/json; charset=utf-8")
											.end(gson.toJson(updatedActuador));
								}
							} else {
								// Si hay un error en la actualización, respondemos con el código 500 (Error
								// interno del servidor)
								System.out.println("Error: " + res.cause().getLocalizedMessage());
							}
						});
	}

}