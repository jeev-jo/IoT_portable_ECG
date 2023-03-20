#include <ESP8266WiFi.h>
#include <ESPAsyncWebServer.h>
#include <ArduinoJson.h>
#include <AsyncWebSocket.h>

AsyncWebSocket ws("/ecg");
AsyncWebServer server(80);
int inputPin = A0; 
int val = 0;

const char *ssid =  "HUAWEI Y7 Prime 2019";     // replace with your wifi ssid and wpa2 key
const char *pass =  "7510931190";

WiFiClient client;
void onWsEvent(AsyncWebSocket * server, AsyncWebSocketClient * client, AwsEventType type, void * arg, uint8_t *data, size_t len){
 
  if(type == WS_EVT_CONNECT){
 
    Serial.println("Websocket client connection received");
    client->text("Hello from ESP32 Server");
 
  } else if(type == WS_EVT_DISCONNECT){
    Serial.println("Client disconnected");
 
  }
}
 
void setup() 
{
  Serial.begin(9600);
  pinMode(inputPin, INPUT);
  delay(10);
               
  Serial.println("Connecting to ");
  Serial.println(ssid); 
 
  WiFi.begin(ssid, pass); 
  while (WiFi.status() != WL_CONNECTED) 
  {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected"); 
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

   server.on("/", HTTP_GET, [](AsyncWebServerRequest *request){
    request->send_P(200, "text/html", "<html><body><h1>ECG Server</h1></body></html>");
  });

  // Set up the WebSocket server
  //ws.onEvent([](AsyncWebSocket *server, AsyncWebSocketClient *client, AwsEventType type, void *arg, uint8_t *data, size_t len){
    //if (type == WS_EVT_CONNECT) {
      //Serial.println("WebSocket client connected");
    //} else if (type == WS_EVT_DISCONNECT) {
      //Serial.println("WebSocket client disconnected");
    ///}
  //});
  ws.onEvent(onWsEvent);  
  server.addHandler(&ws);

  // Start the server
  server.begin();
}



void loop() {
  // put your main code here, to run repeatedly:
  float ecgData = analogRead(inputPin);

  // Send ECG data to WebSocket clients
  ws.textAll(String(ecgData));
}
