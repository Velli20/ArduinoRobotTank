#include "skidSteer.h"

/* Private code */

void SkidSteer::initialize() {
  pinMode(PIN_MOTOR_LEFT_DIRECTION, OUTPUT);
  pinMode(PIN_MOTOR_LEFT_BRAKE, OUTPUT);
  pinMode(PIN_MOTOR_RIGHT_DIRECTION, OUTPUT);
  pinMode(PIN_MOTOR_RIGHT_BRAKE, OUTPUT);

  releaseMotorBrakes(true);

  skidSteerIsReady = true;
}


void SkidSteer::releaseMotorBrakes(boolean releaseBrakes) {
  digitalWrite(PIN_MOTOR_LEFT_BRAKE, releaseBrakes ? LOW : HIGH);
  digitalWrite(PIN_MOTOR_RIGHT_BRAKE, releaseBrakes ? LOW : HIGH);
}

/* Public code */

void SkidSteer::move(int y, int x) {
  if (!skidSteerIsReady) {
    initialize();
  }
  int motorSpeedLeft = x + y;
  int motorSpeedRight = x - y;
  float leftMotorScaled, rightMotorScaled;
  float leftMotorScale, rightMotorScale, maxMotorScale;


  //calculate the scale of the results in comparision base 8 bit PWM resolution
  leftMotorScale =  motorSpeedLeft / 255.0;
  leftMotorScale = abs(leftMotorScale);
  rightMotorScale =  motorSpeedRight / 255.0;
  rightMotorScale = abs(rightMotorScale);

  //choose the max scale value if it is above 1
  maxMotorScale = max(leftMotorScale, rightMotorScale);
  maxMotorScale = max(1, maxMotorScale);

  //and apply it to the mixed values
  leftMotorScaled = constrain(motorSpeedLeft / maxMotorScale, -255, 255);
  rightMotorScaled = constrain(motorSpeedRight / maxMotorScale, -255, 255);


  digitalWrite(PIN_MOTOR_LEFT_DIRECTION, !(leftMotorScaled > 0));
  digitalWrite(PIN_MOTOR_RIGHT_DIRECTION, !(rightMotorScaled > 0));

  analogWrite(PIN_MOTOR_LEFT_SPEED, leftMotorScaled < 0 ? leftMotorScaled * -1 : leftMotorScaled);
  analogWrite(PIN_MOTOR_RIGHT_SPEED, rightMotorScaled < 0 ? rightMotorScaled * -1 : rightMotorScaled);
}

void SkidSteer::stop() {
  analogWrite(PIN_MOTOR_LEFT_SPEED, 0);
  analogWrite(PIN_MOTOR_RIGHT_SPEED, 0);

}




