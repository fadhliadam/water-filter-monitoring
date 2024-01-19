#define API_KEY "API-KEY"
#define DATABASE_URL "DATABASE-URL"

#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include "Wire.h"
#include <OneWire.h>
#include <DallasTemperature.h>

#define WIFI_SSID "kknunsika"
#define WIFI_PASSWORD "jayajayajaya"

#define DS18B20PIN 4
OneWire oneWire(DS18B20PIN);
DallasTemperature sensor(&oneWire);

#define turbidity_pin 34
float volt;
float ntu;

FirebaseData fbdo;

FirebaseAuth auth;
FirebaseConfig config;

unsigned long sendDataPrevMillis = 0;
int count = 0;
bool signupOK = false;


void setup() {
    Serial.begin(115200);
    sensor.begin();

    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    while (WiFi.status() != WL_CONNECTED) {
        delay(1000);
        Serial.println("Tidak terhubung ke WiFi...");
    }
    Serial.println("Terhubung ke WiFi...");

    config.api_key = API_KEY;

    config.database_url = DATABASE_URL;

    if (Firebase.signUp(&config, &auth, "", "")){
        Serial.println("ok");
        signupOK = true;
    }
    else{
        Serial.printf("%s\n", config.signer.signupError.message.c_str());
    }

    Firebase.begin(&config, &auth);
    Firebase.reconnectNetwork(true);
}

void loop() {
    if (Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 15000 || sendDataPrevMillis == 0)){
        sendDataPrevMillis = millis();
        float ntu = kekeruhan();
        float temp = suhu();
        String turbiStatus = turbidityStatus(ntu);

        if (Firebase.RTDB.setFloat(&fbdo, "water/temp_value", temp)){
            Serial.println("PASSED");
            Serial.println("PATH: " + fbdo.dataPath());
            Serial.println("TYPE: " + fbdo.dataType());
        }
        else {
            Serial.println("FAILED");
            Serial.println("REASON: " + fbdo.errorReason());
        }
        count++;

        if (Firebase.RTDB.setString(&fbdo, "water/turbi_status", turbiStatus)){
            Serial.println("PASSED");
            Serial.println("PATH: " + fbdo.dataPath());
            Serial.println("TYPE: " + fbdo.dataType());
        }
        else {
            Serial.println("FAILED");
            Serial.println("REASON: " + fbdo.errorReason());
        }
        count++;

        if (Firebase.RTDB.setFloat(&fbdo, "water/turbi_value", ntu)){
            Serial.println("PASSED");
            Serial.println("PATH: " + fbdo.dataPath());
            Serial.println("TYPE: " + fbdo.dataType());
        }
        else {
            Serial.println("FAILED");
            Serial.println("REASON: " + fbdo.errorReason());
        }
        Serial.println("");
    }
}

float round_to_dp( float in_value, int decimal_place )
{
    float multiplier = powf( 10.0f, decimal_place );
    in_value = roundf( in_value * multiplier ) / multiplier;
    return in_value;
}

float kekeruhan() {
    volt = 0;
    for(int i=0; i<800; i++)
    {
        volt += ((float)analogRead(turbidity_pin)/1023)*5*0.21;
    }
    volt = volt/800;
    volt = round_to_dp(volt,2);

    if (volt < 2.5) {
        ntu = 400.0;
    } else if (volt >= 2.5 && volt <= 4.2) {
        ntu = (-282.99*volt)+1188.558;
    }
    Serial.print("volt: ");
    Serial.print(volt);
    Serial.println(" v");
    Serial.print("Turbidity: ");
    Serial.print(ntu);
    Serial.println(" NTU");

    return ntu;
}

float suhu(){
    sensor.requestTemperatures();
    float tempinC = sensor.getTempCByIndex(0);
    Serial.print("Temperature = ");
    Serial.print(tempinC);
    Serial.println("ºC");

    return tempinC;
}

String turbidityStatus(float ntu) {
    String turbiStatus;

    if (ntu < 1.0) {
        turbiStatus = "Sangat Baik";
    } else if (ntu >= 1.0 && ntu <= 5.0) {
        turbiStatus = "Baik";
    } else if (ntu > 5.0 && ntu <= 50.0) {
        turbiStatus = "Cukup Baik";
    } else if (ntu > 50.0 && ntu <= 100.0) {
        turbiStatus = "Cukup Buruk";
    } else {
        turbiStatus = "Buruk";
    }

    Serial.println("Status = "+turbiStatus);
    Serial.println("");

    return turbiStatus;
}