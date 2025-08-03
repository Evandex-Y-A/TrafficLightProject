package SerialComms;

import com.fazecast.jSerialComm.*;
import java.util.Scanner;
/**
 *
 * @author evandex
 */
public class ArduinoComms {
    private SerialPort serialPort;
    
    public boolean connect(String port) {
        serialPort = SerialPort.getCommPort(port);
        serialPort.setBaudRate(115200);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        
        if (serialPort.openPort()) {
            new Thread(this::readState).start();
            return true;
        }
        return false;
    }
    
    private void readState() {
        Scanner scanner = new Scanner(serialPort.getInputStream());
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.startsWith("STATE:")) parseStates(line);
        }
    }
    
    public void sendCommand(String command) {
        if (checkConnection()) {
            byte[] data = (command + "\n").getBytes();
            serialPort.writeBytes(data, data.length);
        }
    }
    
    private void parseStates(String state) {
        // what code is in this??????
        // Format: (order),(red),(yellow),(green) => int,bool,bool,bool
        
    }
    
    private boolean checkConnection() {
        return (serialPort.isOpen() && serialPort != null);
    }
    public void disconnect() {
        if (checkConnection()) serialPort.closePort();
    }
}
