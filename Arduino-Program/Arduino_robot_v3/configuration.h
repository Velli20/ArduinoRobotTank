/* Uncomment the following defination if SSD1306 0.96" I2C 128X64 OLED LCD is connected */
/* #define HAS_OLED_LCD */

#ifdef  HAS_OLED_LCD
/* Import libraries that are required to communicate with
   the SSD1306 0.96" I2C 128X64 OLED LCD.

   Download the following libraries
   https://github.com/adafruit/Adafruit-GFX-Library
   https://github.com/adafruit/Adafruit_SSD1306
*/
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <Wire.h>

#define OLED_RESET 4
Adafruit_SSD1306 display(OLED_RESET);

#endif /* HAS_OLED_LCD */

/* Uncomment the following defination if Ultrasonic HC-SR04 Distance Sensor is connected */
/* #define HAS_ULTRASONIC_DISTANCE_SENSOR */
#ifdef HAS_ULTRASONIC_DISTANCE_SENSOR

/* Import library that is reguired to communicate wit the sensor
   Link to download NewPing library
   http://playground.arduino.cc/Code/NewPing#Download */
#include <NewPing.h>

/* Defines for the ultrasonic distance sensor */
#define PIN_ULTRASONIC_TRIGGER 51                 // Arduino pin tied to trigger pin on the ultrasonic sensor.
#define PIN_ULTRASONIC_ECHO 53                    // Arduino pin tied to echo pin on the ultrasonic sensor.
#define ULTRASONIC_MAX_DISTANCE 200               // Maximum distance we want to ping for (in centimeters)

#endif /* HAS_ULTRASONIC_DISTANCE_SENSOR */

/* Serial port at which bluetooth module is connected.
   Arduino UNO has only one serial port, so change the value to
   Serial and make sure to disconnect the module when uploading the code
   to the Arduino board*/
#define BLUETOOTH_MODULE_SERIAL_PORT    Serial2
#define BLUETOOTH_MODULE_BAUD_RATE      57600
