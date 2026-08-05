/*
 * File: esp32_sensors_firebase.ino
 * Includes functionality for ESP32 hardware.
 */
#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <HTTPClient.h>
#include <OneWire.h>
#include <DallasTemperature.h>

// ─── Pin Configurations ───────────────────────────────────────────────
#define PIR_PIN 27
#define LDR_PIN 34
#define ACS712_PIN 35
#define GAS_PIN 32
#define TEMP_PIN 33

#define RELAY1 26
#define RELAY2 25
#define RELAY3 12
#define RELAY4 14
#define RELAY5 4
#define RELAY6 16

OneWire oneWire(TEMP_PIN);
DallasTemperature ds18b20(&oneWire);

// ─── WiFi & Firebase Configurations ───────────────────────────────────
const char* ssid = "ASHEN SOYSA";
const char* password = "Ashen@2004";

// ⚠️ IMPORTANT: Update these based on your active database configuration.
// Option A (Official Project DB): "https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app"
// Option B (Your Custom DB): "https://smart-factory-63600-default-rtdb.asia-southeast1.firebasedatabase.app"
const String firebaseHost = "https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app";
const String firebaseAuth = "AIzaSyAMqFfoTPH_dOYCkkF_wy1qg5BWnEUFWGg"; // API Key from google-services.json

void setup() {
  Serial.begin(115200);
  
  // Connect to Wi-Fi
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi connected!");

  // Configure sensor pins
  pinMode(PIR_PIN, INPUT);
  pinMode(GAS_PIN, INPUT);
  
  // Configure relay pins
  pinMode(RELAY1, OUTPUT); pinMode(RELAY2, OUTPUT);
  pinMode(RELAY3, OUTPUT); pinMode(RELAY4, OUTPUT);
  pinMode(RELAY5, OUTPUT); pinMode(RELAY6, OUTPUT);

  // Initialize all relays to OFF (relays are Active-Low, so HIGH = OFF)
  digitalWrite(RELAY1, HIGH); digitalWrite(RELAY2, HIGH);
  digitalWrite(RELAY3, HIGH); digitalWrite(RELAY4, HIGH);
  digitalWrite(RELAY5, HIGH); digitalWrite(RELAY6, HIGH);

  ds18b20.begin();
}

void loop() {
  int pirState = digitalRead(PIR_PIN);
  int ldrValue = analogRead(LDR_PIN);
  int gasValue = analogRead(GAS_PIN);
  int currentValue = analogRead(ACS712_PIN);

  ds18b20.requestTemperatures();
  float temperature = ds18b20.getTempCByIndex(0);

  // ─── Relay Control Logic (Active-Low: LOW = ON, HIGH = OFF) ─────────
  digitalWrite(RELAY1, pirState ? LOW : HIGH);        // Turn ON if motion detected
  digitalWrite(RELAY2, ldrValue > 2000 ? LOW : HIGH); // Turn ON if dark
  digitalWrite(RELAY4, gasValue > 3000 ? LOW : HIGH); // Turn ON if high gas levels
  digitalWrite(RELAY3, temperature > 31 ? LOW : HIGH); // Turn ON if temperature high

  // Fire Risk Condition: Temperature > 35°C AND Gas > 3100
  bool isFire = (temperature > 35 && gasValue > 3100);
  if (isFire) {
    digitalWrite(RELAY6, LOW); // Trigger fire alarm/sprinkler relay
    digitalWrite(RELAY5, LOW); // Trigger hazard indicator relay
  } else {
    digitalWrite(RELAY6, HIGH);
    digitalWrite(RELAY5, HIGH);
  }

  // Overcurrent protection logic
  if (currentValue > 3500) {
    digitalWrite(RELAY5, LOW); // Trigger safety cut-off
  }

  // ─── Push Data to Firebase Realtime Database ──────────────────────────
  if (WiFi.status() == WL_CONNECTED) {
    WiFiClientSecure client;
    client.setInsecure(); // Skip SSL certificate verification for database REST requests
    
    HTTPClient http;
    
    // Construct the direct database URL pointing to the "SensorData" node
    String url = firebaseHost + "/SensorData.json";
    if (firebaseAuth.length() > 0) {
      url += "?auth=" + firebaseAuth;
    }
    
    http.begin(client, url);
    http.addHeader("Content-Type", "application/json");
    
    // Construct JSON payload with keys matching the Android application's model
    String jsonData = "{"
                      "\"temperature\":" + String(temperature) + ","
                      "\"gas\":" + String(gasValue) + ","
                      "\"motion\":" + String(pirState) + ","
                      "\"ldr\":" + String(ldrValue) + ","
                      "\"current\":" + String(currentValue) + ","
                      "\"fire\":" + String(isFire ? "true" : "false") +
                      "}";
                      
    Serial.println("Updating Firebase: " + jsonData);
    
    // We use PUT instead of POST to update/overwrite the SensorData node directly.
    // POST creates random child IDs (e.g. -Nabc123) which prevents the Android app from reading it.
    int code = http.PUT(jsonData);
    
    if (code > 0) {
      Serial.print("Data updated successfully. HTTP Code: ");
      Serial.println(code);
    } else {
      Serial.print("Error sending data. HTTP Error: ");
      Serial.println(http.errorToString(code).c_str());
    }
    
    http.end();
  } else {
    Serial.println("WiFi not connected. Skipping upload...");
  }

  delay(1000); // Send data every 1 second (recommended to avoid rate-limiting and heat)
}
