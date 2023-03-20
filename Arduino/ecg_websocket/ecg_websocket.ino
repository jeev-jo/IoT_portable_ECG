#include <ESP8266WiFi.h>
#include <WebSocketServer.h>
 
WiFiServer server(80);
WebSocketServer webSocketServer;
 
const char* ssid = "HUAWEI Y7 Prime 2019";
const char* password =  "7510931190";

 
void setup(){
  Serial.begin(115200);
  pinMode(14 , INPUT); // Setup for leads off detection LO +
  pinMode(12, INPUT);
  WiFi.begin(ssid, password);
 
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi..");
  }
 
  Serial.println(WiFi.localIP());
  server.begin();
}
 
void loop(){
  WiFiClient client = server.available();
  if (client.connected() && webSocketServer.handshake(client)) {
 
    String data;
    Serial.println("client connected");
    while (client.connected()) {
      if((digitalRead(10) == 1)||(digitalRead(11) == 1)){
        Serial.println('!');
      }
      else{
        data = analogRead(A0);
        webSocketServer.sendData(data);
        Serial.println(data);
      }
      //Wait for a bit to keep serial data from saturating
      delay(1);
    }
    Serial.println("The client disconnected");
  }       
}