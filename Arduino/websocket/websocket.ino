#include <ESP8266WiFi.h>
//#include <ESPAsyncWebServer.h>
#include <WebSocketServer.h>
 
WiFiServer server(80);
WebSocketServer webSocketServer;
 
const char* ssid = "HUAWEI Y7 Prime 2019";
const char* password =  "7510931190";
//AsyncWebServer server(80);
//AsyncWebSocket ws("/ws");
int inputPin = A0;
 
//void onWsEvent(AsyncWebSocket * server, AsyncWebSocketClient * client, AwsEventType type, void * arg, uint8_t *data, size_t len){
 
  //if(type == WS_EVT_CONNECT){
 
    //Serial.println("Websocket client connection received");
    //client->text("Hello from ESP32 Server");
 
  //} else if(type == WS_EVT_DISCONNECT){
    //Serial.println("Client disconnected");
 
  //}
//}
 
void setup(){
  Serial.begin(115200);
  pinMode(inputPin, INPUT);
  WiFi.begin(ssid, password);
 
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi..");
  }
 
  Serial.println(WiFi.localIP());
  //ws.onEvent(onWsEvent); 
  //server.addHandler(&ws);
 
  server.begin();
}
 
void loop(){
  WiFiClient client = server.available();
  if (client.connected() && webSocketServer.handshake(client)) {
 
    String data;
    Serial.println("client connected");
    while (client.connected()) {
      data = analogRead(inputPin);
      webSocketServer.sendData(data);
    }
    Serial.println("The client disconnected");
  }       
}