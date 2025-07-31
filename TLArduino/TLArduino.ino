const int Lights[5][3] = {
  {2, 3, 4},
  {5, 6, 7},
  {8, 9, 10},
  {11, 12, 13},
  {A0, A1, A2}
};

unsigned long Delays[5][3] = {
  {1000, 1000, 1000},
  {1000, 1000, 1000},
  {1000, 1000, 1000},
  {1000, 1000, 1000},
  {1000, 1000, 1000}
};

int TLOrder[5] = {0, 1, 2, 3, 4};

enum Colors {
  RED,
  YELLOW,
  GREEN
};

struct TrafficState {
  bool isPaused = false;
  int currentLight = 0;
  int currentColor = 0;
  int currentIndex = 0;
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

  trafficState.currentLight = TLOrder[trafficState.currentIndex];

  Serial.begin(115200);
  Serial.println("HI IS THE MIC ON?");
}

void loop() {
  Traffic();
}

void Traffic() {
  switch (trafficState.currentColor) {
    case RED:
      delay(Delays[trafficState.currentLight][RED]);
      digitalWrite(Lights[trafficState.currentLight][YELLOW], HIGH);
      trafficState.currentColor = YELLOW;
      break;

    case YELLOW:
      delay(Delays[trafficState.currentLight][YELLOW]);
      digitalWrite(Lights[trafficState.currentLight][RED], LOW);
      digitalWrite(Lights[trafficState.currentLight][YELLOW], LOW);
      digitalWrite(Lights[trafficState.currentLight][GREEN], HIGH);
      trafficState.currentColor = GREEN;
      break;
    
    case GREEN:
      
      delay(Delays[trafficState.currentLight][GREEN]);
      digitalWrite(Lights[trafficState.currentLight][GREEN], LOW);
      trafficState.currentIndex = nextIndex(trafficState.currentIndex);
      int nextLight = TLOrder[trafficState.currentIndex];
      digitalWrite(Lights[nextLight][RED], HIGH);
      trafficState.currentLight = nextLight;
      trafficState.currentColor = RED;
      break;
  }
}

int nextIndex(int current) {
  if (current == 4) return 0;
  else return current + 1;
}
