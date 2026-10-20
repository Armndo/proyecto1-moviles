#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth no está habilitado en la configuración
#endif

BluetoothSerial SerialBT;

// Asignación de pines actualizada
const byte Led_1 = 23; // Pin 9 display (segmento f - Izquierda)
const byte Led_2 = 21; // Pin 7 display (segmento a - Adelante)
const byte Led_3 = 19; // Pin 6 display (segmento b - Derecha)
const byte Led_4 = 22; // Pin 2 display (segmento d - Reversa)
const byte Led_5 = 18; // Pin 10 display (segmento g - Stop / Centro)

char DataBluetooth;

void setup() {
  Serial.begin(115200);
  
  // Nombre visible para vincular desde Android
  SerialBT.begin("ESP32_LEDS"); 
  Serial.println("Bluetooth iniciado. Listo para vincular.");

  pinMode(Led_1, OUTPUT);
  pinMode(Led_2, OUTPUT);
  pinMode(Led_3, OUTPUT);
  pinMode(Led_4, OUTPUT);
  pinMode(Led_5, OUTPUT);

  digitalWrite(Led_1, LOW);
  digitalWrite(Led_2, LOW);
  digitalWrite(Led_3, LOW);
  digitalWrite(Led_4, LOW);
  digitalWrite(Led_5, LOW);
}

void loop() {
  if (SerialBT.available()) {
    DataBluetooth = SerialBT.read();
    Serial.println(DataBluetooth);

    if (DataBluetooth == 'I') {
      digitalWrite(Led_1, HIGH);
      digitalWrite(Led_2, LOW);
      digitalWrite(Led_3, LOW);
      digitalWrite(Led_4, LOW);
      digitalWrite(Led_5, LOW);
      SerialBT.print("i");
    }
    else if (DataBluetooth == 'A') {
      digitalWrite(Led_1, LOW);
      digitalWrite(Led_2, HIGH);
      digitalWrite(Led_3, LOW);
      digitalWrite(Led_4, LOW);
      digitalWrite(Led_5, LOW);
      SerialBT.print("a");
    }
    else if (DataBluetooth == 'D') {
      digitalWrite(Led_1, LOW);
      digitalWrite(Led_2, LOW);
      digitalWrite(Led_3, HIGH);
      digitalWrite(Led_4, LOW);
      digitalWrite(Led_5, LOW);
      SerialBT.print("d");
    }
    else if (DataBluetooth == 'R') {
      digitalWrite(Led_1, LOW);
      digitalWrite(Led_2, LOW);
      digitalWrite(Led_3, LOW);
      digitalWrite(Led_4, HIGH);
      digitalWrite(Led_5, LOW);
      SerialBT.print("r");
    }
    else if (DataBluetooth == 'S') {
      digitalWrite(Led_1, LOW);
      digitalWrite(Led_2, LOW);
      digitalWrite(Led_3, LOW);
      digitalWrite(Led_4, LOW);
      digitalWrite(Led_5, HIGH);
      SerialBT.print("s");
    }
  }
}