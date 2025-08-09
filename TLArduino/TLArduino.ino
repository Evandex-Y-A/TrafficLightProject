const int Lights[5][3] = {
  {2, 3, 4},
  {5, 6, 7},
  {8, 9, 10},
  {11, 12, 13},
  {A5, A4, A3} // Alvin why must you do this to me
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

void handleDelay(String command);
void handleOrder(String sequence);

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
        sendStates();
        trafficState.state = REDYELLOW;
        trafficState.lastCheckedTime = currentTime;
      }
      break;
    
    case REDYELLOW:
      if (elapsed >= Delays[currentIndex][YELLOW]) {
        digitalWrite(Lights[currentIndex][RED], LOW);
        digitalWrite(Lights[currentIndex][YELLOW], LOW);
        digitalWrite(Lights[currentIndex][GREEN], HIGH);
        sendStates();
        trafficState.state = GREEN;
        trafficState.lastCheckedTime = currentTime;
      }
      break;
    
    case GREEN:
      if (elapsed >= Delays[currentIndex][GREEN]) {
        digitalWrite(Lights[currentIndex][YELLOW], HIGH);
        sendStates();
        trafficState.state = YELLOW;
        trafficState.lastCheckedTime = currentTime;
        digitalWrite(Lights[nextIndex][YELLOW], HIGH);
      }
      break;

    case YELLOW:
      if (elapsed >= Delays[currentIndex][YELLOW]) {
        digitalWrite(Lights[currentIndex][RED], HIGH);
        digitalWrite(Lights[currentIndex][GREEN], LOW);
        digitalWrite(Lights[currentIndex][YELLOW], LOW);

        digitalWrite(Lights[nextIndex][RED], LOW);
        digitalWrite(Lights[nextIndex][YELLOW], LOW);
        digitalWrite(Lights[nextIndex][GREEN], HIGH);
        trafficState.currentLight = (trafficState.currentLight + 1) % 5;
        sendStates();
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
  Serial.println(input);
  if (input.equals("PAUSE")) {
    trafficState.isPaused = true;
    Serial.println("Paused");
  }
  else if (input.equals("RESUME")) {
    trafficState.isPaused = false;
    Serial.println("Resumed");
  }
  else if (input.startsWith("DELAY:")) {
    handleDelay(input.substring(6));
  }
  else if (input.startsWith("ORDER:")) {
    handleOrder(input.substring(6));
  }
  else {
    Serial.println("UNKNOWN COMMAND");
  }
}

// New handler function
void handleDelay(String command) {
  Serial.println("String received: " + command);
  int firstColon = command.indexOf(':');
  if (firstColon == -1) return;
  
  char light = command.charAt(0);
  String data = command.substring(firstColon+1);
  
  int firstComma = data.indexOf(',');
  int secondComma = data.indexOf(',', firstComma+1);
  
  if (firstComma == -1 || secondComma == -1) return;
  
  long redDelay = data.substring(0, firstComma).toInt();
  long yellowDelay = data.substring(firstComma+1, secondComma).toInt();
  long greenDelay = data.substring(secondComma+1).toInt();
  
  int lightIndex = -1;
  for (int i=0; i<5; i++) {
    if (Labels[i] == light) {
      lightIndex = i;
      break;
    }
  }
  
  if (lightIndex != -1) {
    Delays[lightIndex][0] = redDelay;    // RED
    Delays[lightIndex][1] = yellowDelay; // YELLOW
    Delays[lightIndex][2] = greenDelay;  // GREEN
  }
}

void handleOrder(String sequence) {
  Serial.println("String received: " + sequence);
  int order[5];
  int count = 0;
  int start = 0;
  
  for (int i=0; i<sequence.length() && count<5; i++) {
    if (sequence[i] == ',') {
      char label = sequence.charAt(start);
      int idx = -1;
      for (int j=0; j<5; j++) {
        if (Labels[j] == label) {
          idx = j;
          break;
        }
      }
      if (idx != -1) {
        order[count] = idx;
        count++;
      }
      start = i+1;
    }
  }
  
  if (count < 5 && start < sequence.length()) {
    char label = sequence.charAt(start);
    int idx = -1;
    for (int j=0; j<5; j++) {
      if (Labels[j] == label) {
        idx = j;
        break;
      }
    }
    if (idx != -1) {
      order[count] = idx;
      count++;
    }
  }
  
  if (count == 5) {
    for (int i=0; i<5; i++) {
      TLOrder[i] = order[i];
    }
  }
}
