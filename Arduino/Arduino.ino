int roomLDR = 0;
int initLDRValue = 0;

void setup() {
  initLDR();

  Serial.begin(9600);
}

void initLDR() {
  // 처음 실행될 때 주변의 빛 값을 먼저 read하여 밝기 기준 값을 설정하고, 
  // 이후 loop에서는 해당 값을 최대 값으로 기준으로 잡아 mapping 해준다.
  initLDRValue = analogRead(roomLDR);
}

void loop() {
  writeLDR();
  delay(100);
}

void writeLDR() {
  int ldrValue = map(analogRead(roomLDR), 0, initLDRValue, 0, 1023) / 4;
  ldrValue = ldrValue < 0 
    ? 0 
    : ldrValue > 255 
      ? 255 
      : ldrValue;
  
  Serial.println("ldr : " + String(ldrValue));
}