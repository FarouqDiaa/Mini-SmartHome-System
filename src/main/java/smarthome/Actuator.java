package smarthome;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Actuator {

    private static final String hostName = "localhost";
    private static final int portNumber = 5000;

    private String actuatorId;
    private Status status;

    public enum Status {
        ON, OFF
    }

    public Actuator(String actuatorId, Status status) {
        this.actuatorId = actuatorId;
        this.status = status;
        connectToServer();
        listenForCommands();
    }

    public void connectToServer() {
        Socket socket = null;
        PrintWriter out = null;
        try {
            socket = new Socket(hostName, portNumber);
            out = new PrintWriter(socket.getOutputStream(), true);

            String actuatorOutput = "Actuator connected with id: " + actuatorId + " and status: " + status;
            out.println(actuatorOutput);

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }
    }

    public void listenForCommands() {
        Socket socket = null;
        PrintWriter out = null;
        java.util.Scanner input = null;
        try {
            socket = new Socket(hostName, portNumber);
            out = new PrintWriter(socket.getOutputStream(), true);
            input = new java.util.Scanner(socket.getInputStream());

            while (input.hasNextLine()) {
                String command = input.nextLine();
                if ("TURN_ON".equalsIgnoreCase(command)) {
                    status = Status.ON;
                    System.out.println("Actuator " + actuatorId + " turned ON");
                    out.println("Actuator " + actuatorId + " is now ON");
                } else if ("TURN_OFF".equalsIgnoreCase(command)) {
                    status = Status.OFF;
                    System.out.println("Actuator " + actuatorId + " turned OFF");
                    out.println("Actuator " + actuatorId + " is now OFF");
                } else {
                    System.out.println("Unknown command received: " + command);
                    out.println("Unknown command received");
                }
            }
        } catch (IOException e) {
            System.err.println("Error listening for commands: " + e.getMessage());
        } finally {
            if (input != null) {
                input.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }
    }
}
