#include "skidSteer.h"
#include "configuration.h"
#include <stdio.h>
#include <stdarg.h>


/* Variables */
long lcdLastUpdate = 0;
int speedX = 0;
int speedY = 0;

SkidSteer skidSteer;

/* Function prototypes*/
void updateLCD(void);
void sendResponse(char * response);
void sendResponse(const char * formatter, ...);

/* Put your setup code in this function, to run once: */
void setup() {
  Serial.begin(9600);
  BLUETOOTH_MODULE_SERIAL_PORT.begin(BLUETOOTH_MODULE_BAUD_RATE);
  BLUETOOTH_MODULE_SERIAL_PORT.setTimeout(BLUETOOTH_READ_TIMEOUT);

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
    long timeReadingStart = millis();
    long value;

    switch (command) {

      /* Commands send by Android app.
         Format: #VALUE where # is the letter prefix and VALUE is an interger 0-255
         Example: X100 == Move in x direction at speed of 100 */

      case 'Y':   /* Command accelerometer tilt in y direction */
      case 'W':   /* Command joystick y-axis*/
        speedY = BLUETOOTH_MODULE_SERIAL_PORT.parseInt();;
        break;

      case 'X':   /* Command accelerometer tilt in x direction */
      case 'A':   /* Command joystick x-axis */
        speedX = BLUETOOTH_MODULE_SERIAL_PORT.parseInt();;
        break;
      case 'D':   /* Command wait or delay */
        value = BLUETOOTH_MODULE_SERIAL_PORT.parseInt();
        sendResponse("Arduino Tank: Delaying for %d ms", value);
        skidSteer.delay(value);
        break;
      case 'N':   /* Command move amount of distance in cm backward */
        value = BLUETOOTH_MODULE_SERIAL_PORT.parseInt();
        sendResponse("Arduino Tank: Moving %d cm backward", value);
        skidSteer.moveDistance((int)value, false);
        break;
      case 'H':   /* Command move amount of distance in cm forward */
        value = BLUETOOTH_MODULE_SERIAL_PORT.parseInt();
        sendResponse("Arduino Tank: Moving %d cm forward", value);
        skidSteer.moveDistance((int)value, true);
        break;
      case 'J':   /* Command rotate counterclockwise amount of degrees */
        value = BLUETOOTH_MODULE_SERIAL_PORT.parseInt();
        sendResponse("Arduino Tank: Rotating %d degrees CCW", value);
        skidSteer.rotate((int)value, true);
        break;
      case 'K':   /* Command rotate clockwise amount of degrees */
        value = BLUETOOTH_MODULE_SERIAL_PORT.parseInt();
        sendResponse("Arduino Tank: Rotating %d degrees CW", 100);
        skidSteer.rotate((int)value, false);
        break;
      case 'S':   /* Command set x speed to 0 and y speed to 0-255 */
        value = BLUETOOTH_MODULE_SERIAL_PORT.parseInt();
        sendResponse("Arduino Tank: Speed set to %d. Max value is set to %d and Min %d", value, MOTOR_SPEED_MAX, MOTOR_SPEED_MIN);
        speedY = value;
        speedX = 0;
        break;
      default:
        sendResponse("Arduino Tank: Unknown command \"%c\"", command);

        break;
    }
  }

  /* Update LCD every 500 ms */
  if (millis() >= (lcdLastUpdate + 100)) {
    updateLCD();
    lcdLastUpdate = millis();
  }

  /* Check that delay function is inactive */
  if (!skidSteer.isDelaying()) {
    skidSteer.move(speedX, speedY);
  }
}




/* Updates content of the OLED LCD if LCD is connected */
void updateLCD() {

#ifdef HAS_OLED_LCD
  char text[40];
  if (skidSteer.isDelaying()) {
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
   @param formatter: String that contains a format string
   @param values: Additioanl int values reltated to formatter string
*/
void sendResponse(const char * formatter, ...) {
  va_list args;
  va_start(args, formatter);

  char response[80] = {"\0"};
  vsprintf(response, formatter, args);

  BLUETOOTH_MODULE_SERIAL_PORT.write(response);
  BLUETOOTH_MODULE_SERIAL_PORT.write("\r");
  BLUETOOTH_MODULE_SERIAL_PORT.flush();

  va_end(args);

}

/* Send message to Android App console
   @param response: pointer to message to send
*/
void sendResponse(char * response) {
  BLUETOOTH_MODULE_SERIAL_PORT.write(response);
  BLUETOOTH_MODULE_SERIAL_PORT.write("\r");
  BLUETOOTH_MODULE_SERIAL_PORT.flush();
}



