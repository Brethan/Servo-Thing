#include <Servo.h>

Servo servo;


void setup() {
  // put your setup code here, to run once:
  servo.attach(2);
  servo.write(15);
  Serial.begin(9600);

}

void loop() {
  // put your main code here, to run repeatedly:

}


void serialEvent() {
  String data = Serial.readStringUntil('\n');
  int angle = data.toInt();

  servo.write(angle);
}
