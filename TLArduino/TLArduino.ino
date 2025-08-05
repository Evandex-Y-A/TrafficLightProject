const int Lights[5][3] = {
  {2, 3, 4},
  {7, 6, 5}, // Alvin why must you do this to me
  {8, 9, 10},
  {11, 12, 13},
  {A4, A5, A3} // Alvin what the FUCK
};

unsigned long Delays[5][3] = {
  {1000, 1000, 1000},
  {1000, 1000, 1000},
  {1000, 1000, 1000},
  {1000, 1000, 1000},
  {1000, 1000, 1000}
};
char Labels[5] = {'A','B','C','D','E'};
int TLOrder[5] = {0, 1, 2, 3, 4};

enum Colors {
  RED,
  YELLOW,
  GREEN,
  REDYELLOW
};

struct TrafficState {
  bool isPaused = false;
  int currentLight = 0;
  int state = 0;
  unsigned long lastCheckedTime = 0;
} trafficState;

void setup() {
  for (int i = 0; i < 5; i++)
    for (int j = 0; j < 3; j++) {
      pinMode(Lights[i][j], OUTPUT);
      digitalWrite(Lights[i][j], LOW);
  }

  for (int i = 0; i < 5; i++) {
    digitalWrite(Lights[i][RED], HIGH);
  }

  trafficState.lastCheckedTime = millis();
  Serial.begin(115200);
  Serial.println("HI IS THE MIC ON?");
}

void loop() {
  readSerial();
  Traffic();
}

void Traffic() {
  if (trafficState.isPaused) return;

  int currentIndex = TLOrder[trafficState.currentLight];
  int nextIndex = TLOrder[(trafficState.currentLight + 1) % 5];
  unsigned long currentTime = millis();
  unsigned long elapsed = currentTime - trafficState.lastCheckedTime;

  switch (trafficState.state) {
    case RED:
      if (elapsed >= Delays[currentIndex][RED]) {
        digitalWrite(Lights[currentIndex][YELLOW], HIGH);
        trafficState.state = REDYELLOW;
        trafficState.lastCheckedTime = currentTime;
      }
      break;
    
    case REDYELLOW:
      if (elapsed >= Delays[currentIndex][YELLOW]) {
        digitalWrite(Lights[currentIndex][RED], LOW);
        digitalWrite(Lights[currentIndex][YELLOW], LOW);
        digitalWrite(Lights[currentIndex][GREEN], HIGH);
        trafficState.state = GREEN;
        trafficState.lastCheckedTime = currentTime;
      }
      break;
    
    case GREEN:
      if (elapsed >= Delays[currentIndex][GREEN]) {
        digitalWrite(Lights[currentIndex][GREEN], LOW);
        digitalWrite(Lights[currentIndex][YELLOW], HIGH);
        trafficState.state = YELLOW;
        trafficState.lastCheckedTime = currentTime;
        digitalWrite(Lights[nextIndex][YELLOW], HIGH);
      }
      break;

    case YELLOW:
      if (elapsed >= Delays[currentIndex][YELLOW]) {
        digitalWrite(Lights[currentIndex][RED], HIGH);
        digitalWrite(Lights[currentIndex][YELLOW], LOW);

        digitalWrite(Lights[nextIndex][RED], LOW);
        digitalWrite(Lights[nextIndex][YELLOW], LOW);
        digitalWrite(Lights[nextIndex][GREEN], HIGH);
        trafficState.currentLight = (trafficState.currentLight + 1) % 5;

        trafficState.state = GREEN;
        trafficState.lastCheckedTime = currentTime;
      }
      break;
  }
}

void sendStates() {
  for (int i = 0; i < 5; i++) {

    Serial.print("STATE:");
    Serial.print(Labels[TLOrder[i]]);
    Serial.print(",");
    Serial.print(digitalRead(Lights[i][RED]) ? "1" : "0");
    Serial.print(",");
    Serial.print(digitalRead(Lights[i][YELLOW]) ? "1" : "0");
    Serial.print(",");
    Serial.print(digitalRead(Lights[i][GREEN]) ? "1" : "0");
    Serial.println();
  }
}

void readSerial() {
  if (Serial.available() <= 0) return;
  String input = Serial.readStringUntil('\n');
  input.trim();

  if (input.equals("PAUSE")) {
    trafficState.isPaused = true;
    Serial.println("Paused");
  }
  else if (input.equals("RESUME")) {
    trafficState.isPaused = false;
    Serial.println("Resumed");
  }
  else if (input.startsWith("SET")) {
    input.remove(0, 3);
    handleSet(input); // does nothing currently
  }
  else {
    Serial.println("UNKNOWN COMMAND");
  }
}


void handleSet(String truncatedInput) {
  return; // Add this later
}
