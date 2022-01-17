#include <SPI.h>
#include <SD.h>
#include <SoftwareSerial.h>
#include <WiFiEsp.h>
#include <Adafruit_VC0706.h>
//#include <WiFi.h>

char Wifi_SSID[] = "iman TP-LINK";
char Wifi_Pass[] = "Iman.1373";

char Server_IP[] = "54.38.106.229";
int Server_Port = 1717;

WiFiEspClient Client;
//SoftwareSerial CameraConnection = SoftwareSerial(69, 7);
Adafruit_VC0706 cam = Adafruit_VC0706(&Serial2);

String Cmd, Response;
File PicFile;
bool SendResult;

int InputPin = 24;
int InputValue = 0;

//SoftwareSerial esp(17, 16); // RX, TX

int WifiStatus = WL_IDLE_STATUS;

void setup() { 

//  #if !defined(SOFTWARE_SPI)
//  #if defined(__AVR_ATmega1280__) || defined(__AVR_ATmega2560__)
//    if(chipSelect != 53) pinMode(53, OUTPUT); // SS on Mega
//  #else
//    if(chipSelect != 10) pinMode(10, OUTPUT); // SS on Uno, etc.
//  #endif
//  #endif
  
  Serial.begin(115200);
  Serial.println("Serial Port Started");

  pinMode(InputPin, INPUT);
  Serial.println("Pin 24 setted as Input");
  
  Serial1.begin(115200);
//  esp.println("AT+UART_DEF=9600,8,1,0,0");
  
  WiFi.init(&Serial1);
  if(WiFi.status() == WL_NO_SHIELD){
    Serial.println("WiFi not available !");
  }else{
    Serial.println("WiFi Initialized");
  }

  if (SD.begin(53)) {
    Serial.println("SD Card Initialized");
  }else{
    Serial.println("Card failed, or not present");
  }

//  while(WifiStatus != WL_CONNECTED){
//    Serial.println("Attempting to Connect to : ");
//    Serial.println(Wifi_SSID);
//
//    WifiStatus = WiFi.begin(Wifi_SSID, Wifi_Pass);
//  }

  Serial.println("Wifi Connected : ");

  printWifiStatus();

  pinMode(22, OUTPUT);
  digitalWrite(22, HIGH);

  if(cam.begin(115200)){
    Serial.println("Camera Initialized ...");
  }else{
    Serial.println("Camera not Found !");
  }
  
//  ConnectWifi();
}

void loop() {
  InputValue = digitalRead(InputPin);
  if(InputValue == LOW){
    delay(1000);
    Serial.println("Preparing to Send Picture");
    SendResult = SendPic("M2.jpg");
    if(SendResult == true){
      Serial.println("Send Result : True");
    }else{
      Serial.println("Send Result : False");
    }
  }
}

void printWifiStatus()
{
  // print the SSID of the network you're attached to
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print your WiFi shield's IP address
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  // print the received signal strength
  long rssi = WiFi.RSSI();
  Serial.print("Signal strength (RSSI):");
  Serial.print(rssi);
  Serial.println(" dBm");
}

bool SendPic(String FileName)
{  
  if(Client.connect(Server_IP, Server_Port)){
    Serial.println("TCP Connection is Ready ...");
  }else{
    Serial.println("TCP Connection Failed !");
    PrintEspData();
    return false;
  }
    
  PicFile = SD.open(FileName, O_READ);
  if(PicFile){
    Cmd = "ADD_PIC#" + String(PicFile.size());
  }else{
    Serial.println("Read File Failed !");
    Client.stop();
    return false;
  }
  Serial.println("Command to Send : " + Cmd);
  if(Client.println(Cmd)){
    Serial.println("Command Sent : " + Cmd);
    Response = Client.readStringUntil(char(13));
    while(Client.available()){
      int S = Client.read();
    }
    Serial.println("Response : " + String(Response));
    if(Response == "1"){
      byte buf[2048];
      delay(100);
      Serial.println("Sending Picture ...");
      while(PicFile.available()){
        PicFile.read(buf, 2048);
        Client.write(buf, 2048);
      }
      Serial.println("Send File Completed");
      PicFile.close();
      Client.stop();
      PrintEspData(); 
      Serial.println("Picture Sent Successfully .");
    }else{
      Serial.println("Error on Server !");
      Client.stop();
      return false;
    }
  }else{
    Serial.println("Send command to server failed !");
    PrintEspData();
    Client.stop();
    return false;
  }

  delay(1000);

  return true;
}

void PrintEspData()
{
  if(Serial1.available()) // check if the esp has data
  {
    while(Serial1.available())
    {
      // The esp has data so display its output to the serial window 
      char c = Serial1.read(); // read the next character.
      Serial.write(c);
    } 
    Serial.println(); 
  }
}

