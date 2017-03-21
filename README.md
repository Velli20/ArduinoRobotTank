# ArduinoRobotTank


![CAD Assembly](https://cloud.githubusercontent.com/assets/25138671/24061863/de0f838e-0b61-11e7-8827-5f46b928c86c.png)

Fully 3D-printable robot for Arduino Mega/Uno/Due Controller Board. Designed to fit up to 2 pcs of Arduino shields on top of the controller board.

Parts to print
--------------

- Chassis_top.stl
- Chassis_bottom.stl
- Motor_mount_plate.stl (2 pcs)
- Wheel_cover_14_t.stl (2 pcs)
- Track_wheel_14t.stl (6 pcs)
- Motor_cover.stl (2 pcs)
- Tank_link_pin.stl (68 pcs)
- tank_link.stl (68 pcs)
- Front_cover_for_HC_SR04.stl (2 pcs)

Optional parts to print
-----------------------

- Chassis_fastener_holder.stl (29 pcs). These are for holding chassis nuts in place so it is easier to assemble the robot.

Fasteners
---------

- ISO10642 Screw M3x8 40 pcs
- ISO14579 Screw M3x12 8 pcs
- ISO14579 Screw M3x25 4 pcs
- ISO4032 Nut M3 46 pcs

Fasteners for LCD:
-----------------

- ISO4762 Screw M2x8 4 pcs
- ISO4032 Nut M2 4 pcs

Fasteners for Ultrasonic HC-SR04 Distance Sensor:
-------------------------------------------------

- ISO4762 Screw M2x8 8 pcs
- Nylon Spacers M2x5 8 pcs
- ISO4762 Screw M2x12 8 pcs

Electronics
-----------

- 2x geared DC motors
- Arduino DUE/Mega/UNO Controller Board
- L298P Motor Shield R3
- 7.4 V LiPo battery

Optional electronics
--------------------

- ESP8266 WiFI Development Board 
- SSD1306 0.96" I2C 128X64 OLED LCD
- Ultrasonic HC-SR04 Distance Sensor 2 pcs
- HC-05 Bluetooth module 

Code example
------------

Usage of the skid steering algorithm:

```C++
#define MOTOR_SPEED_MAX 255     // Define max speed
#define MOTOR_SPEED_MIN -255    // Define min speed

SkidSteer skidSteer;

void loop() {
    skidSteer.move(255, 100);   // Move at speed 255 in x direction and 100 at y direction
}
```

Images
------

![Printed](https://cloud.githubusercontent.com/assets/25138671/24061920/10a73d28-0b62-11e7-8fdf-348cbf21785a.jpg)
![Printed](https://cloud.githubusercontent.com/assets/25138671/24061924/1616bc5c-0b62-11e7-92f6-fcb6090c6ec5.jpg)
