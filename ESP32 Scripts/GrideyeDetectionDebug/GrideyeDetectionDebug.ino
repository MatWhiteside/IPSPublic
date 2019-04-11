
/* 
 *  Program to use the SparkFun GridEye sensor and output originalSum+threshold and currentSum.
 *  Values are output as "currentSum, originalSum+THRESHOLD".
 *  
 *  Pin configurations should be the following when using an ESP32 Thing:
 *   - Qwiic cable (blue) -> 21
 *   - Qwiic cable (yellow) -> 22
 *   - Qwiic cable (red) -> 3V3
 *   - Qwiic cable (black) -> GND
 */

#include <Wire.h>
#include <SparkFun_GridEYE_Arduino_Library.h>

// Variables to store original sum of matrix and
// the sum of the matrix at each update
int originalSum;
int currentSum;

// Threshold constant used in detection formula (C)
const int THRESHOLD = 100;

GridEYE grideye;

void setup() {
  Serial.begin(115200);
  
  // Initialise GridEye
  Wire.begin();
  grideye.begin();
  
  // Take an initial measurement of the room which is the sum of all
  // pixel temperatures (average of 5 readings)
  for(int i = 0; i < 5; i++) {
    int sum = 0;
    for(unsigned char j = 0; j < 64; j++){
      sum += grideye.getPixelTemperature(j);
    } 
    originalSum += sum;
  }
  originalSum /= 5;
}

void loop() {
  // Keep looping taking a current measurement which is the sum of all pixel
  // temperatures (average of 5 readings)
  currentSum = 0;
  for(int i = 0; i < 5; i++) {
    int sum = 0;
    for(unsigned char j = 0; j < 64; j++){
      sum += grideye.getPixelTemperature(j);
    } 
    currentSum += sum;
  }
  currentSum /= 5;

  // Output values to monitor and plotter
  Serial.print(currentSum);
  Serial.print(",");
  Serial.print(originalSum+THRESHOLD);
  Serial.println();

  delay(100);
}
