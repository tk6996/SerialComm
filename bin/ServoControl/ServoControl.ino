#include <Servo.h>
Servo Horizontal = Servo();
Servo Vertical = Servo();
void setup() {
  Serial.begin(9600);
  Horizontal.attach(9);
  Vertical.attach(10);
  Vertical.write(150);
  Horizontal.write(90);
}

void loop() {
  if (Serial.available() > 0)
  {
    switch(Serial.read())
    {
      case 1 : Horizontal.write((int)Serial.read()); break;
      case 2 : Vertical.write((int)Serial.read()); break;
    }
  }
  delay(100);
}
