#include "skidSteer.h"
#include "configuration.h"
#include <stdio.h>

/* Variables */
long lcdLastUpdate;
int speedX = 0;
int speedY = 0;

SkidSteer skidSteer;

/* Function prototypes*/
void updateLCD(void);
long getBaudrate();

/* Put your setup code in this function, to run once: */
void setup() {
#ifndef BLUETOOTH_MODULE_BAUD_RATE
  /* If baudrate is not defined or unknown, then read
       it automatically
  */
  BLUETOOTH_MODULE_SERIAL_PORT.begin(getBaudrate());
#else
  BLUETOOTH_MODULE_SERIAL_PORT.begin(BLUETOOTH_MODULE_BAUD_RATE);
#endif /* BLUETOOTH_MODULE_BAUD_RATE */

  Serial.begin(9600);
  Serial.println("Version 1.0");

#ifdef HAS_OLED_LCD
  /* Init OLED display connected at i2c address 0x3C */
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);
  display.clearDisplay();
#endif /* HAS_OLED_LCD */

  lcdLastUpdate = millis();
}

void loop() {

  if (BLUETOOTH_MODULE_SERIAL_PORT.available() ) {
    char command = BLUETOOTH_MODULE_SERIAL_PORT.read();

    /* Buffer for the Android console. Send optionally response
       messages back to the Android app which will appear in the console.
       Max lenght for the message is 10 chars
    */
    char response[10];
    /* Clear the response buffer */
    memset(response, 0, sizeof(response));

    switch (command) {

      /* Commands send by Android app.
         Format: #VALUE where # is the letter prefix and VALUE is an interger 0-255
         Example: X100 == Move robot in x direction at speed of 100*/

      case 'X':
        /* Prefix for the android phone accelerometer x axis */
        speedX = BLUETOOTH_MODULE_SERIAL_PORT.parseInt();
        sprintf(response, "X%dOK\r", speedX);
        Serial.println(response);
        BLUETOOTH_MODULE_SERIAL_PORT.write(response);
        BLUETOOTH_MODULE_SERIAL_PORT.flush();
        break;

      case 'Y':
        /* Prefix for the android phone accelerometer y axis */
        speedY = BLUETOOTH_MODULE_SERIAL_PORT.parseInt();
        sprintf(response, "Y%dOK\r", speedY);
        Serial.println(response);
        BLUETOOTH_MODULE_SERIAL_PORT.write(response);
        BLUETOOTH_MODULE_SERIAL_PORT.flush();
        break;
    }
  }

  /* Update LCD every 500 ms */
  if (millis() >= (lcdLastUpdate + 500)) {
    updateLCD();
    lcdLastUpdate = millis();
  }
  skidSteer.move(speedX, speedY);
}



void updateLCD() {

#ifdef HAS_OLED_LCD
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(0, 5);
  display.print("Speed Y: ");
  display.print(speedY);
  display.setCursor(0, 15);
  display.print("Speed X: ");
  display.print(speedX);
  display.display();
#endif /* HAS_OLED_LCD */
}


/* Automatic baudrate detector by "retrolefty", code taken from here:
   https://forum.arduino.cc/index.php?topic=38160.0
   @retval        Baud rate
*/
long getBaudrate() {
  static int recpin = -1;

start:
  if (recpin == -1) {
    recpin = 1;   // Test if bluetooth module is connected to serial port 1
  } else if (recpin == 1) {
    recpin = 18;  // Test if bluetooth module is connected to serial port 2
  } else if (BLUETOOTH_MODULE_SERIAL_PORT == 18) {
    recpin = 14;  // Test if bluetooth module is connected to serial port 3
  }
  while (digitalRead(recpin) == 1) {} // wait for low bit to start
  long baud;
  long rate = pulseIn(recpin, LOW);  // measure zero bit width from character 'U'
  if (rate < 12)
    baud = 115200;
  else if (rate < 20)
    baud = 57600;
  else if (rate < 29)
    baud = 38400;
  else if (rate < 40)
    baud = 28800;
  else if (rate < 60)
    baud = 19200;
  else if (rate < 80)
    baud = 14400;
  else if (rate < 150)
    baud = 9600;
  else if (rate < 300)
    baud = 4800;
  else if (rate < 600)
    baud = 2400;
  else if (rate < 1200)
    baud = 1200;
  else
    baud = 0;

  if (baud == 0 && recpin != 14) {
    goto start;
  } else {
    recpin = -1;
  }
  return baud;
}


