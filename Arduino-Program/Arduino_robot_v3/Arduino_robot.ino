#include "skidSteer.h"
#include "configuration.h"
#include <stdio.h>

/* Variables */
long lcdLastUpdate = 0;
int speedX = 0;
int speedY = 0;

SkidSteer skidSteer;

/* Function prototypes*/
void updateLCD(void);
void sendResponse(char * response);
long getBaudrate();

/* Put your setup code in this function, to run once: */
void setup() {
#ifndef BLUETOOTH_MODULE_BAUD_RATE
  /* If baudrate is not defined or is unknown, then read it automatically */
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
  display.setTextSize(1);
  display.setTextColor(WHITE);
#endif /* HAS_OLED_LCD */

  /* Send message to Android console */
  sendResponse("Arduino Tank ready");
}

void loop() {

  if (BLUETOOTH_MODULE_SERIAL_PORT.available() ) {
    char command = BLUETOOTH_MODULE_SERIAL_PORT.read();

    switch (command) {

      /* Commands send by Android app.
         Format: #VALUE where # is the letter prefix and VALUE is an interger 0-255
         Example: X100 == Move in x direction at speed of 100 */

      case 'Y': /* Command accelerometer tilt in y direction */
      case 'F': /* Command manual control button "forward" */
      case 'B': /* Command manual control button "backward" */
      speedY = BLUETOOTH_MODULE_SERIAL_PORT.parseInt();
      break;

      case 'X': /* Command accelerometer tilt in x direction */
      case 'R': /* Command manual control button "right" */
      case 'L': /* Command manual control button "left" */
      speedX = BLUETOOTH_MODULE_SERIAL_PORT.parseInt();
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
  char text[15];
  sprintf(text, "Speed Y: %d\nSpeed X: %d", speedY, speedX);
  
  display.clearDisplay();
  display.setCursor(0, 5);
  display.print(text);
  display.display();
#endif /* HAS_OLED_LCD */
}

/* Send message to Android App console
 * @param response: pointer to message to send
*/
void sendResponse(char * response) {
  BLUETOOTH_MODULE_SERIAL_PORT.write(response);
  BLUETOOTH_MODULE_SERIAL_PORT.flush();
}

/* Automatic baudrate detector by "retrolefty", code taken from here:
   https://forum.arduino.cc/index.php?topic=38160.0
   @retval        Baud rate
*/
long getBaudrate() {
  int recpin = -1;

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


