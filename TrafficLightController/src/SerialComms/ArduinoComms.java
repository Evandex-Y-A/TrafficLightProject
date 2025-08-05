package SerialComms;

import com.fazecast.jSerialComm.*;
import java.util.Scanner;
/**
 *
 * @author evandex
 */
public class ArduinoComms {
    private SerialPort serialPort;
    private UI.TrafficFrame frame;
    
    public ArduinoComms(UI.TrafficFrame frame) {
        this.frame = frame;
    }
    public boolean connect(String port) {
        try {
            if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            }
        
            serialPort = SerialPort.getCommPort(port);
            serialPort.setBaudRate(115200);
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        
            if (serialPort.openPort()) {
                new Thread(this::readState).start();
                System.out.println("Connected to " + port);
                return true;
            } else {
                System.err.println("Failed to open port: " + port);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error connecting to " + port + ": " + e.getMessage());
            return false;
        }
    }
    
    private void readState() {
        Scanner scanner = new Scanner(serialPort.getInputStream());
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.startsWith("STATE:")) updateStates(line.substring(6));
            else if (line.startsWith("ORDER:")) frame.getCanvas().updateOrder(line.substring(6));
        }
        scanner.close();
    }
    
    public void sendCommand(String command) {
        if (checkConnection()) {
            byte[] data = (command + "\n").getBytes();
            serialPort.writeBytes(data, data.length);
        }
    }
    
    private void updateStates(String state) {
        int light = Character.getNumericValue(state.charAt(0));
        switch (light) {
            case 0:
                state = state.replaceFirst(Integer.toString(light), "A");
                break;
            case 1:
                state = state.replaceFirst(Integer.toString(light), "B");
                break;
            case 2:
                state = state.replaceFirst(Integer.toString(light), "C");
                break;
            case 3:
                state = state.replaceFirst(Integer.toString(light), "D");
                break;
            case 4:
                state = state.replaceFirst(Integer.toString(light), "E");
                break;
        }
        frame.getCanvas().updateLightState(state);
    }
    
    private boolean checkConnection() {
        if (serialPort == null) return false;
        else return serialPort.isOpen();
    }
    public void disconnect() {
        if (checkConnection()) serialPort.closePort();
    }
    
    public String[] getAvailablePorts() {
        try {
            System.out.println("Attempting to get serial ports...");
            SerialPort[] ports = SerialPort.getCommPorts();
            System.out.println("Found " + ports.length + " ports");
        
            if (ports.length == 0) {
                System.out.println("No serial ports detected. Possible causes:");
                System.out.println("1. No devices connected");
                System.out.println("2. Driver issues");
                System.out.println("3. Permission problems");
                System.out.println("4. Java native access restrictions");
                return new String[]{"No ports available"};
            }
        
            String[] portNames = new String[ports.length];
            for (int i = 0; i < ports.length; i++) {
                String name = ports[i].getSystemPortName();
                String description = ports[i].getDescriptivePortName();
                portNames[i] = name + " - " + description;
            }
            return portNames;
        } catch (Exception e) {
            System.err.println("Error getting serial ports: " + e.getMessage());
            return new String[0];
        }
    }
}
