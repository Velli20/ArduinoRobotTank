#include "skidSteer.h"

/* Private code */

/*
 * @brief   LP298P Motor driver initialization prosedure. 
 *          If left or right pwm pin is not defined then
 *          skidSteerInitializationFailed is set to true
 */
void SkidSteer::initialize() {
  
#ifdef PIN_MOTOR_LEFT_DIRECTION || PIN_MOTOR_RIGHT_DIRECTION
  pinMode(PIN_MOTOR_LEFT_DIRECTION, OUTPUT);
  pinMode(PIN_MOTOR_RIGHT_DIRECTION, OUTPUT);
  skidSteerIsReady = true;
#else 
  skidSteerInitializationFailed = true
#endif 

#ifdef PIN_MOTOR_LEFT_BRAKE && PIN_MOTOR_RIGHT_BRAKE
  pinMode(PIN_MOTOR_LEFT_BRAKE, OUTPUT);
  pinMode(PIN_MOTOR_RIGHT_BRAKE, OUTPUT);

  releaseMotorBrakes(true);
#endif

}

/*
 * @brief   Engage the motor brakes by setting positive
 *          DC signal to the motor driver chips brake pins.
 *          This will not work if the traces to the brake inputs 
 *          are cut on the motor shield or brake pins are
 *          undefined.
 */
void SkidSteer::releaseMotorBrakes(boolean releaseBrakes) {
#ifdef PIN_MOTOR_LEFT_BRAKE && PIN_MOTOR_RIGHT_BRAKE
  digitalWrite(PIN_MOTOR_LEFT_BRAKE, releaseBrakes ? LOW : HIGH);
  digitalWrite(PIN_MOTOR_RIGHT_BRAKE, releaseBrakes ? LOW : HIGH);
#endif
}

/* Public code */

/*
 * @brief   Function that calculates left and right motor 
 *          speed based on y and x vectors. Requires that initialization
 *          is passed succesfully
 * @param   y: Speed vector in y direction 
 * @param   x: Speed vector in x direction
 */
void SkidSteer::move(int y, int x) {
  if (!skidSteerIsReady) {
    initialize();
    if(skidSteerInitializationFailed) {
      return;
    }
  } 
  int motorSpeedLeft = x + y;
  int motorSpeedRight = x - y;
  float leftMotorScaled, rightMotorScaled;
  float leftMotorScale, rightMotorScale, maxMotorScale;


  /* Calculate the scale of the results in comparision base 8 bit PWM resolution */
  leftMotorScale =  motorSpeedLeft / 255.0;
  leftMotorScale = abs(leftMotorScale);
  rightMotorScale =  motorSpeedRight / 255.0;
  rightMotorScale = abs(rightMotorScale);

  /* Choose the max scale value if it is above 1 */
  maxMotorScale = max(leftMotorScale, rightMotorScale);
  maxMotorScale = max(1, maxMotorScale);

  /* And apply it to the mixed values */
  leftMotorScaled = constrain(motorSpeedLeft / maxMotorScale, -255, 255);
  rightMotorScaled = constrain(motorSpeedRight / maxMotorScale, -255, 255);

  digitalWrite(PIN_MOTOR_LEFT_DIRECTION, !(leftMotorScaled > 0));
  digitalWrite(PIN_MOTOR_RIGHT_DIRECTION, !(rightMotorScaled > 0));

  analogWrite(PIN_MOTOR_LEFT_SPEED, leftMotorScaled < 0 ? leftMotorScaled * -1 : leftMotorScaled);
  analogWrite(PIN_MOTOR_RIGHT_SPEED, rightMotorScaled < 0 ? rightMotorScaled * -1 : rightMotorScaled);
}

/* 
 * @brief   Halts the robot movement by sending zero width pwm pulse
 *          to motor driver chip. No quarantees that robot will
 *          actually stop. To engage brakes 
 *          call releaseMotorBrakes(false) function
 */
void SkidSteer::stop() {
  analogWrite(PIN_MOTOR_LEFT_SPEED, 0);
  analogWrite(PIN_MOTOR_RIGHT_SPEED, 0);

}

/* 
 * @brief   A non blocking delay function. This wont stop
 *          main loop to excecuting the program and will not notify
 *          when then delay is complete. There are also no guarantees
 *          that delayed time is exactly the time is requested to delay.
 * @param   timesInMillis: Amount of time in milliseconds to delay
 */
void SkidSteer::delay(long timeInMillis) {
  delayUntil = millis() + timeInMillis;
}

/* 
 * @brief   Function to check if rquested delay time is  passed
 * @retval  return true if delay is complete
 */
boolean SkidSteer::isDelaying() {
  return (millis() < delayUntil);
}

/* @brief   
   @retval  Returns amount of milliseconds to delay */
long SkidSteer::getRemainingDelayTime() {
  return delayUntil - millis();
}

/* 
 * @brief   Move amount of distance in cm basing on feedback 
 *          coming from the motor sensor (i.g pulse sensor)
 * @param   distanceInCm: Distance to move in cm
 * @param   forward: Direction of the movement.
 *          True if forward false if backward
 */
void SkidSteer::moveDistance(int distanceInCm, boolean forward) {
#ifdef MOTOR_TYPE_STEPPER_MOTOR
    /* TODO */
#elif HAS_MOTOR_FEEDBACK_SENSOR
    /* TODO */
#else 
    /* No feedback sensor connected. Use timers to move distance approximately */
    int deltaTime = distanceInCm * 1000; // Lets assume that it takes one second to move one cm
    
  
#endif
}

/*
 * @brief   Rotate amount of degrees either counterclockwise or clockwise
 *          basing on the feedback coming from the accelerometer sensor.
 * @param   degreesToRotate: amount of degrees to rotate 
 * @param   CCW: Rotation direction. True if counterclockwise, else false
 */
void SkidSteer::rotate(int degreesToRotate, boolean CCW) {
  
}



