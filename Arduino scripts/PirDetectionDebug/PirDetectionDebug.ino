/* 
 *  Program to use the SparkFun OpenPIR sensor and output it's analog / digital values.
 *  Values are outputted to serial monitor as digitalVal, upperLimit, analogVal (scaled as voltage)
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
  // Print digital value, upper limit and analogVal (scaled)
  printAnalogValue();
  delay(100);
}

/* Read and print digitalVal, upperLimit, analogVal (scaled as voltage) */
void printAnalogValue()
{
    // Read in analog value
    unsigned int analogPIR = analogRead(PIR_AOUT);
    // Convert 10-bit analog value to a voltage
    float voltage = (float) analogPIR / 1024.0 * 5.0;
    
    // Scaled digital output
    Serial.print(5 * digitalRead(PIR_DOUT));
    Serial.print(',');
    // Upper limit
    Serial.print(2.5);
    Serial.print(',');
    // Voltage
    Serial.print(voltage); // Print voltage
    Serial.println();
}
