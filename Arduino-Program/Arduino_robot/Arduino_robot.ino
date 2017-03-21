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

/* Put your setup code in this function, to run once: */
void setup() {
  Serial.begin(9600);
  BLUETOOTH_MODULE_SERIAL_PORT.begin(BLUETOOTH_MODULE_BAUD_RATE);

#ifdef HAS_OLED_LCD
  /* Init OLED display connected at i2c address 0x3C */
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(WHITE);
#endif /* HAS_OLED_LCD */

  /* Send message to Android console */
  sendResponse("Arduino Tank ready");
  skidSteer.delay(11000);
}

void loop() {

  /* Check if there is data available in Bluetooth Serial Buffer */
  if (BLUETOOTH_MODULE_SERIAL_PORT.available() && !skidSteer.isDelaying()) {
    char command = BLUETOOTH_MODULE_SERIAL_PORT.read();
    
    switch (command) {

      /* Commands send by Android app.
         Format: #VALUE where # is the letter prefix and VALUE is an interger 0-255
         Example: X100 == Move in x direction at speed of 100 */

      case 'Y':   /* Command accelerometer tilt in y direction */
      case 'W':   /* Command joystick y-axis*/
          speedY = BLUETOOTH_MODULE_SERIAL_PORT.parseInt();
          break;

      case 'X':   /* Command accelerometer tilt in x direction */
      case 'A':   /* Command joystick x-axis */
          speedX = BLUETOOTH_MODULE_SERIAL_PORT.parseInt();
          break;
      case 'D':   /* Command wait or delay */
          skidSteer.delay(BLUETOOTH_MODULE_SERIAL_PORT.parseInt());
          break;
      case 'N':   /* Command move amount of distance in cm backward */
          break;
      case 'H':   /* Command move amount of distance in cm forward */
          break;
      case 'J':   /* Command rotate counterclockwise amount of degrees */
          break;
      case 'K':   /* Command rotate clockwise amount of degrees */
          break;
      case 'S':   /* Command set x speed to 0 and y speed to 0-255 */
          speedY = BLUETOOTH_MODULE_SERIAL_PORT.parseInt();
          speedX = 0;
          break;
      
      break;
    }
  }

  /* Update LCD every 500 ms */
  if (millis() >= (lcdLastUpdate + 100)) {
    updateLCD();
    lcdLastUpdate = millis();
  }

  /* Check that delay function is inactive 
  if(!skidSteer.isDelaying()) {
    skidSteer.move(speedX, speedY);
  } */
}




/* Updates content of the OLED LCD if LCD is connected */
void updateLCD() {

#ifdef HAS_OLED_LCD
  char text[40];
  if(skidSteer.isDelaying()) {
    sprintf(text, "Speed Y: %d\nSpeed X: %d\nDelay for: %d ms", speedY, speedX, skidSteer.getRemainingDelayTime());
  } else {
    sprintf(text, "Speed Y: %d\nSpeed X: %d", speedY, speedX);
  }
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
  BLUETOOTH_MODULE_SERIAL_PORT.write("\r");
  BLUETOOTH_MODULE_SERIAL_PORT.flush();
}



