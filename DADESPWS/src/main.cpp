#include <HTTPClient.h>
#include "ArduinoJson.h"
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <PubSubClient.h>
#include <Adafruit_Sensor.h>
#include <DHT.h>
#include <DHT_U.h>
#include <ESP32Servo.h>

Servo myservo;
String previousContent = "";  // Variable global para almacenar el estado anterior del contenido del topic de MQTT


HTTPClient http;

// MQTT configuration ;; variables necesarias
WiFiClient espClient;
PubSubClient client(espClient);
char msg[50];

using namespace std;
const int DEVICE_ID = 124;

int test_delay = 1000; // so we don't spam the API
boolean describe_tests = true;
int cont=0;

String serverName = "http://192.168.0.33:8084";

// Replace WifiName and WifiPassword by your WiFi credentials
#define STASSID "WIFI_SSID"    //"Your_Wifi_SSID"
#define STAPSK "WIFI_PASS" //"Your_Wifi_PASSWORD"
#define DHTPIN 15 // pin de placa del sensor de humedad y temperatura
#define ACTUADOR_PIN 16 // pin de placa del actuador
#define TEMPERATURE_THRESHOLD 30

#define DHTTYPE    DHT11     // DHT 11


DHT_Unified dht(DHTPIN, DHTTYPE);
uint32_t delayMS;


// Server IP, where de MQTT broker is deployed
const char *MQTT_BROKER_ADRESS = "192.168.0.33";
const uint16_t MQTT_PORT = 1883;

// Name for this MQTT client
const char *MQTT_CLIENT_NAME = "Client_5";

String mensaje="";
boolean change=false;
void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Received on ");
  Serial.print(topic);
  Serial.print(": ");

  String content = "";
  for (size_t i = 0; i < length; i++) {
    content.concat((char)payload[i]);
  }
  Serial.print(content);
  Serial.println();

  // Verificamos si el contenido ha cambiado
  if (content != previousContent) {
    if (content == "ON") {
      myservo.write(0);
      delay(160);
      myservo.write(90);
      delay(5000);
    } else if (content == "OFF") {
      myservo.write(180);
      delay(150);
      myservo.write(90);
      delay(5000);
    }
    // Actualizamos el estado anterior
    previousContent = content;
  }
}

  


// inicia la comunicacion MQTT
// inicia establece el servidor y el callback al recibir un mensaje
void InitMqtt()
{
  client.setServer(MQTT_BROKER_ADRESS, MQTT_PORT);
  client.setCallback(callback);
}


// Setup
void setup()
{

  Serial.begin(9600);
  Serial.println();
    // pinMode(ACTUADOR_PIN, OUTPUT);
  myservo.attach(ACTUADOR_PIN);
  myservo.write(90);
  delay(1000);
  Serial.print("Connecting to ");
  Serial.println(STASSID);

  WiFi.mode(WIFI_STA);
  WiFi.begin(STASSID, STAPSK);

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }

  InitMqtt();

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println("Setup!");




dht.begin();
  Serial.println(F("DHTxx Unified Sensor"));
  // Print temperature sensor details.
  sensor_t sensor;
  dht.temperature().getSensor(&sensor);
  Serial.println(F("------------------------------------"));
  Serial.println(F("Temperature Sensor"));
  Serial.print  (F("Sensor Type: ")); Serial.println(sensor.name);
  Serial.print  (F("Driver Ver:  ")); Serial.println(sensor.version);
  Serial.print  (F("Unique ID:   ")); Serial.println(sensor.sensor_id);
  Serial.print  (F("Max Value:   ")); Serial.print(sensor.max_value); Serial.println(F("°C"));
  Serial.print  (F("Min Value:   ")); Serial.print(sensor.min_value); Serial.println(F("°C"));
  Serial.print  (F("Resolution:  ")); Serial.print(sensor.resolution); Serial.println(F("°C"));
  Serial.println(F("------------------------------------"));
  // Print humidity sensor details.
  dht.humidity().getSensor(&sensor);
  Serial.println(F("Humidity Sensor"));
  Serial.print  (F("Sensor Type: ")); Serial.println(sensor.name);
  Serial.print  (F("Driver Ver:  ")); Serial.println(sensor.version);
  Serial.print  (F("Unique ID:   ")); Serial.println(sensor.sensor_id);
  Serial.print  (F("Max Value:   ")); Serial.print(sensor.max_value); Serial.println(F("%"));
  Serial.print  (F("Min Value:   ")); Serial.print(sensor.min_value); Serial.println(F("%"));
  Serial.print  (F("Resolution:  ")); Serial.print(sensor.resolution); Serial.println(F("%"));
  Serial.println(F("------------------------------------"));
  // Set delay between sensor readings based on sensor details.
  delayMS = sensor.min_delay / 1000;




}

// conecta o reconecta al MQTT
// consigue conectar -> suscribe a topic y publica un mensaje
// no -> espera 5 segundos


void ConnectMqtt()
{
  Serial.print("Starting MQTT connection...");
  if (client.connect(MQTT_CLIENT_NAME))
  {
    client.subscribe("5");
    // client.publish("dad", "connected");
  }
  else
  {
    Serial.print("Failed MQTT connection, rc=");
    Serial.print(client.state());
    Serial.println(" try again in 5 seconds");

    delay(5000);
  }
}

// gestiona la comunicación MQTT
// comprueba que el cliente está conectado
// no -> intenta reconectar
// si -> llama al MQTT loop
String response;

String serializeSensorValueBody(int idSensor, int nPlaca, float humedad, long timestamp, float temperatura, int idGroup)
{
  // StaticJsonObject allocates memory on the stack, it can be
  // replaced by DynamicJsonDocument which allocates in the heap.
  DynamicJsonDocument doc(2048);

  // Add values in the document
doc["idSensor"] = idSensor;
doc["nPlaca"] = nPlaca;
doc["humedad"] = humedad;
doc["timestamp"] = timestamp;
doc["temperatura"] = temperatura;
doc["idGroup"] = idGroup;

  // Generate the minified JSON and send it to the Serial port.
  String output;
  serializeJson(doc, output);
  Serial.println(output);

  return output;
}


String serializeActuatorStatusBody(int nPlaca, int idActuador, long timestamp, bool activo, bool encendido, int idGroup)
{
  DynamicJsonDocument doc(2048);

  doc["nPlaca"] = nPlaca;
  doc["idActuador"] = idActuador;
  doc["timestamp"] = timestamp;
  doc["activo"] = activo;
  doc["encendido"] = encendido;
  doc["idGroup"] = idGroup;


  String output;
  serializeJson(doc, output);
  return output;
}
String serializeDeviceBody(String deviceSerialId, String name, String mqttChannel, int idGroup)
{
  DynamicJsonDocument doc(2048);

  doc["deviceSerialId"] = deviceSerialId;
  doc["name"] = name;
  doc["mqttChannel"] = mqttChannel;
  doc["idGroup"] = idGroup;

  String output;
  serializeJson(doc, output);
  return output;
}

void deserializeActuatorStatusBody(String responseJson)
{
  if (responseJson != "")
  {
    DynamicJsonDocument doc(2048);

    // Deserialize the JSON document
    DeserializationError error = deserializeJson(doc, responseJson);

    // Test if parsing succeeds.
    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    // Fetch values.
    int nPlaca = doc["nPlaca"];
    int idActuador = doc["idActuador"];
    long timestamp = doc["timestamp"];
    bool activo = doc["activo"];
    bool encendido = doc["encendido"];
    int idGroup = doc["idGroup"];

    Serial.println("Actuator status deserialized: [nPlaca: " + String(nPlaca) + ", idActuador: " + String(idActuador) + ", timestamp: " + String(timestamp) + ", activo: " + String(activo) + ", encendido: " + String(encendido) + ", idGroup: " + String(idGroup) + "]");  }
}

void deserializeDeviceBody(int httpResponseCode)
{

  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String responseJson = http.getString();
    DynamicJsonDocument doc(2048);

    DeserializationError error = deserializeJson(doc, responseJson);

    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    int idDevice = doc["idDevice"];
    String deviceSerialId = doc["deviceSerialId"];
    String name = doc["name"];
    String mqttChannel = doc["mqttChannel"];
    int idGroup = doc["idGroup"];

    Serial.println(("Device deserialized: [idDevice: " + String(idDevice) + ", name: " + name + ", deviceSerialId: " + deviceSerialId + ", mqttChannel" + mqttChannel + ", idGroup: " + idGroup + "]").c_str());
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}

void deserializeSensorsFromDevice(int httpResponseCode)
{

  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String responseJson = http.getString();
    // allocate the memory for the document
    DynamicJsonDocument doc(ESP.getMaxAllocHeap());

    // parse a JSON array
    DeserializationError error = deserializeJson(doc, responseJson);

    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    // extract the values
    JsonArray array = doc.as<JsonArray>();
    for (JsonObject sensor : array)
    {
      int idSensor = doc["idSensor"];
      int nPlaca = doc["nPlaca"];
      float humedad = doc["humedad"];
      long timestamp = doc["timestamp"];
      float temperatura = doc["temperatura"];
      int idGroup = doc["idGroup"];
      Serial.println("Sensor deserialized: [idSensor: " + String(idSensor) + ", nPlaca: " + String(nPlaca) + ", humedad: " + String(humedad) + ", timestamp: " + String(timestamp) + ", temperatura: " + String(temperatura) + ", idGroup: " + String(idGroup) + "]");    }
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}

void deserializeActuatorsFromDevice(int httpResponseCode)
{

  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String responseJson = http.getString();
    // allocate the memory for the document
    DynamicJsonDocument doc(ESP.getMaxAllocHeap());

    // parse a JSON array
    DeserializationError error = deserializeJson(doc, responseJson);

    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    // extract the values
    JsonArray array = doc.as<JsonArray>();
    for (JsonObject actuador : array)
    {
      int nPlaca = doc["nPlaca"];
      int idActuador = doc["idActuador"];
      long timestamp = doc["timestamp"];
      bool activo = doc["activo"];
      bool encendido = doc["encendido"];
      int idGroup = doc["idGroup"];

     Serial.println("Actuator deserialized: [nPlaca: " + String(nPlaca) + ", idActuador: " + String(idActuador) + ", timestamp: " + String(timestamp) + ", activo: " + String(activo) + ", encendido: " + String(encendido) + ", idGroup: " + String(idGroup) + "]");    }
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}

void test_response(int httpResponseCode)
{
  delay(test_delay);
  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String payload = http.getString();
    Serial.println(payload);
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}

void describe(char *description)
{
  if (describe_tests)
    Serial.println(description);
}


void GET_tests()
{
      // SENSORES
  describe("Test GET all sensors");
  String serverPath = serverName + "/api/sensor/all*";
  http.begin(serverPath.c_str());
  deserializeDeviceBody(http.GET());

  describe("Test GET all sensors with Connection");
  serverPath = serverName + "/api/sensor";
  http.begin(serverPath.c_str());
  deserializeSensorsFromDevice(http.GET());

  describe("Test GET Sensor by idSensor");
  serverPath = serverName + "/api/sensor/:idSensor";
  http.begin(serverPath.c_str());
  deserializeSensorsFromDevice(http.GET());

  describe("Test GET last Sensor Byid");
  serverPath = serverName + "/api/sensor/last";
  http.begin(serverPath.c_str());
  deserializeSensorsFromDevice(http.GET());
  
  describe("Test GET last Sensor ByGroup");
  serverPath = serverName + "/api/sensor/group/last";
  http.begin(serverPath.c_str());
  deserializeSensorsFromDevice(http.GET());

      // ACTUADORES
  describe("Test GET all actuators");
  serverPath = serverName + "/api/actuador/all";
  http.begin(serverPath.c_str());
  deserializeActuatorsFromDevice(http.GET());

  describe("Test GET all actuators with Connection");
  serverPath = serverName + "/api/actuador";
  http.begin(serverPath.c_str());
  deserializeActuatorsFromDevice(http.GET());

  describe("Test GET actuador ById");
  serverPath = serverName + "/api/actuador/:idActuador";
  http.begin(serverPath.c_str());
  deserializeActuatorsFromDevice(http.GET());
}

void POST_tests()
{
  //post de un sensor con datos inventados
  sensors_event_t event;
  dht.temperature().getEvent(&event);
  float temperatura = event.temperature;
  dht.humidity().getEvent(&event);
  float humedad = event.relative_humidity;
  String sensor_value_body = serializeSensorValueBody(10, 1, humedad,millis(), temperatura,5);
  describe("Test POST sensor with path and body and response");
  String serverPath = serverName + "/api/sensor";
  http.begin(serverPath.c_str());
  test_response(http.POST(sensor_value_body));


  //post de un actuador
 
  String actuator_states_body = serializeActuatorStatusBody(1,201,millis(),false, true,1);
  describe("Test POST with actuator state");
  serverPath = serverName + "/api/actuador";
  http.begin(serverPath.c_str());
  test_response(http.POST(actuator_states_body));

}

// gestiona la comunicación MQTT
// comprueba que el cliente está conectado
// no -> intenta reconectar
// si -> llama al MQTT loop


// La función de callback que se ejecutará cuando recibamos un mensaje desde el servidor MQTT



void HandleMqtt()
{
  if (!client.connected())
  {
   ConnectMqtt();
  }
  client.loop();
}




void loop() {


if (cont == 1000) {
    cont = 0;
    sensors_event_t event;
    dht.temperature().getEvent(&event);
    float temp=event.temperature;
    dht.humidity().getEvent(&event);
    float hum= event.relative_humidity;
    if (isnan(event.temperature)) {
      Serial.println(F("Error leyendo la temperatura!"));
    } else {
      Serial.print(F("Temperatura: "));
      Serial.print(temp);
      Serial.println(F("°C"));
      POST_tests();


    }
}
else{
  cont++;
}
HandleMqtt();
}