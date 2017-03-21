#ifndef SKID_STEER_H
#define SKID_STEER_H

#include <Arduino.h> 

#define MOTOR_SPEED_MAX 255
#define MOTOR_SPEED_MIN -255

/* Defines for the motor shield channel A */
#define PIN_MOTOR_LEFT_DIRECTION 12
#define PIN_MOTOR_LEFT_BRAKE 9
#define PIN_MOTOR_LEFT_SPEED 3

/* Defines for the motor shield channel B */
#define PIN_MOTOR_RIGHT_DIRECTION 13
#define PIN_MOTOR_RIGHT_BRAKE 8
#define PIN_MOTOR_RIGHT_SPEED 11


class SkidSteer {
  private:
    /* Variables */
    bool skidSteerIsReady = false;
    bool skidSteerInitializationFailed = false;
    long delayUntil = 0;
    
    /* Private function prototypes */
    void initialize();
    void releaseMotorBrakes(boolean releaseBrakes);

  public:
    /* Public function prototypes */
    void move(int y, int x);
    void stop();
    void delay(long timeInMillis);

    boolean isDelaying();
    long getRemainingDelayTime();
};



#endif /*SKID_STEER_H*/
