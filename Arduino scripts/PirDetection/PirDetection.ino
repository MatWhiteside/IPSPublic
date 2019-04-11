/* 
 *  Program to use the SparkFun OpenPIR sensor and detect if a person is present or not.
 *  If a person is detected the program will output "Person detected." else it will output
 *  "No one detected."
 *  
 *  Pin configurations should be the following when using Arduino Uni R3:
 *   - A -> A0
 *   - VCC -> 5V
 *   - GND -> GND
 *   - OUT -> Digital pin 2
 */
 
#define PIR_AOUT A0  // PIR analog output on A0
#define PIR_DOUT 2   // PIR digital output on D2

void setup() 
{
  Serial.begin(115200);
  // Set analog and digital pins as inputs
  pinMode(PIR_AOUT, INPUT);
  pinMode(PIR_DOUT, INPUT);
}

void loop() 
{
  // Check if person is detected and print result to serial monitor
  outputDetectionStatus(); 
  delay(100);
}

/* Read the digital value and output if a person is detected or not */
void outputDetectionStatus()
{
  int motionStatus = digitalRead(PIR_DOUT);

  // Output motion status
  if (motionStatus == HIGH) {
    Serial.println("Person detected.");
  } else {
    Serial.println("No one detected.");
  }
}
