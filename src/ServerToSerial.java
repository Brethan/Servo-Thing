import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import com.fazecast.jSerialComm.SerialPort;

public class ServerToSerial {
    public static SerialPort port;
    public static SerialPort connectToPort(Scanner s) {
        ArrayList<SerialPort> list = new ArrayList<>();
        
        int i = 0;
        // Iterate through all available comm ports, display information to user
        for (SerialPort p : SerialPort.getCommPorts()) {
            System.out.printf("%d: %s\n", i, p.getDescriptivePortName());
            list.add(p);
            i++;
        }

        SerialPort port;
        // Give user the option to select the comm port they'd like to use
        if (list.size() == 1) {
            // preempt the selection if only 1 option
            port = list.get(0);
        } else {
            // Otherwise prompt user for a comm port
            int choice = s.nextInt();
    
            if (choice >= list.size()) 
                throw new ArrayIndexOutOfBoundsException();

            port = list.get(choice);
        }
        
        // Open the port and begin serial communication
        port.setBaudRate(9600);
        port.openPort();

        s.close();
        list.clear();

        return port;
    }

    public static void wifiSerialBridge() throws Exception {
        ServerSocket server = new ServerSocket(42);
        System.out.println("Waiting for client...");
        Socket client = server.accept();
        System.out.println("Client connected! " + client.toString());
        // AbstractBuilderFactoryFrameComponentPanel abffcp = new AbstractBuilderFactoryFrameComponentPanel(new BuilderFactoryFrameFactory(new...)); 
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        serial_region: {

            port = connectToPort(new Scanner(System.in));
            if (!port.isOpen()) {
                System.out.println("Could not open Comm port");
                break serial_region;
            }

            PrintWriter pooter = new PrintWriter(port.getOutputStream());
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                // Activates when python script is keyboard interrupted (Ctrl + C)
                if (inputLine.contains("kill server")) {
                    System.out.println("Client has killed the server.");
                    break;
                }

                // Parse roll angle from Raspberry Pi and send to the Arduino
                long servoAngle = Long.parseLong(inputLine);
                pooter.println("" + servoAngle);
                pooter.flush();
            }
            
            pooter.close();
        };
        
        // clean up
        in.close();
        port.closePort();
        client.close();
        server.close();
    } 

    public static void serialServoSweepTest() throws Exception {
        Scanner s = new Scanner(System.in);
        port = connectToPort(s);
        System.out.println(port.isOpen());
        s.close();

        PrintWriter pooter = new PrintWriter(port.getOutputStream());
        
        for (int i = 5; i < 180; i += 1) {
            pooter.println("" + i);
            pooter.flush();
            System.out.println(i);
            Thread.sleep(15);
        }

        pooter.println("" + 5);
        pooter.flush();
        port.closePort();
        
    }

    public static void main(String[] args) throws Exception {

        try {
            wifiSerialBridge();
        } catch (Exception e) {
            System.out.println("Something... happened to either the server or the serial connection :(");            
        } 
    }

    public static void fixCommPort() {
        Scanner s = new Scanner(System.in);
        SerialPort port = connectToPort(s);
        port.closePort();
    }
}
