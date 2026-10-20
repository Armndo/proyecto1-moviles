#include <WiFi.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <WebSocketsServer.h>

const char* ssid = "ESP32 Tacometro WiFi";
const char* password = "123456789_wifi";

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 32
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);

WebSocketsServer webSocket = WebSocketsServer(81);

const int SENSOR_PIN = 18;
volatile unsigned long conteoPulsos = 0;
volatile unsigned long ultimoTiempoPulso = 0;
const unsigned long TIEMPO_DEBOUNCE_MICROS = 5000; // Filtro antirrebote

unsigned long tiempoAnterior = 0;
const unsigned long INTERVALO = 1000;

void IRAM_ATTR calcularPulso() {
  unsigned long tiempoActualMicros = micros();
  if (tiempoActualMicros - ultimoTiempoPulso > TIEMPO_DEBOUNCE_MICROS) {
    conteoPulsos++;
    ultimoTiempoPulso = tiempoActualMicros;
  }
}

void webSocketEvent(uint8_t num, WStype_t type, uint8_t * payload, size_t length) {
  switch (type) {
    case WStype_DISCONNECTED:
      Serial.printf("[%d] Cliente desconectado\n", num);
      break;
    case WStype_CONNECTED: {
      IPAddress ip = webSocket.remoteIP(num);
      Serial.printf("[%d] Cliente conectado desde: %d.%d.%d.%d\n", num, ip[0], ip[1], ip[2], ip[3]);
      break;
    }
    case WStype_TEXT:
      break;
  }
}

void setup() {
  Serial.begin(115200);

  WiFi.softAP(ssid, password);
  Serial.print("Access Point listo. IP: ");
  Serial.println(WiFi.softAPIP());

  webSocket.begin();
  webSocket.onEvent(webSocketEvent);

  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println(F("Error: No se detectó la pantalla SSD1306"));
    for (;;);
  }
  display.clearDisplay();
  display.setTextColor(WHITE);

  pinMode(SENSOR_PIN, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(SENSOR_PIN), calcularPulso, FALLING);
  tiempoAnterior = millis();
}

void loop() {
  webSocket.loop();
  unsigned long tiempoActual = millis();

  if (tiempoActual - tiempoAnterior >= INTERVALO) {
    noInterrupts();
    unsigned long pulsos = conteoPulsos;
    conteoPulsos = 0;
    interrupts();

    // Hélice de 4 aspas: (Pulsos * 60) / 4
    float rpm = (pulsos * 60.0) / 2.0;

    Serial.printf("Pulsos reales: %lu | RPM: %.0f\n", pulsos, rpm);

    display.clearDisplay();
    display.setTextSize(1);
    display.setCursor(0, 0);
    display.print("TACO_WIFI: ");
    display.print(WiFi.softAPgetStationNum());

    display.setTextSize(2);
    display.setCursor(0, 14);
    display.print((int)rpm);

    display.setTextSize(1);
    display.setCursor(85, 20);
    display.print("RPM");
    display.display();

    String datosRpm = String((int)rpm);
    webSocket.broadcastTXT(datosRpm);

    tiempoAnterior = tiempoActual;
  }
}