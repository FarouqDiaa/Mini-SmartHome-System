package smarthome;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Actuator {

    private static final String hostName = "localhost";
    private static final int portNumber = 5001;

    private String actuatorId;
    private Status status;

    public enum Status {
        ON, OFF
    }

    public Actuator(String actuatorId, Status status) {
        this.actuatorId = actuatorId;
        this.status = status;
        handleConnection();
    }

    public void handleConnection() {
        Socket socket = null;
        PrintWriter out = null;
        Scanner input = null;
        try {
            socket = new Socket(hostName, portNumber);
            out = new PrintWriter(socket.getOutputStream(), true);
            input = new Scanner(socket.getInputStream());

            out.println(actuatorId);
            System.out.println("Actuator " + actuatorId + " connected to server");

            while (input.hasNextLine()) {
                String command = input.nextLine();
                Status previousStatus = status;

                if ("TURN_ON".equalsIgnoreCase(command)) {
                    if (status == Status.ON) {
                        System.out.println("Actuator " + actuatorId + " is already ON");
                        out.println("Actuator " + actuatorId + " is already ON");
                    } else {
                        status = Status.ON;
                        System.out.println("Actuator " + actuatorId + " turned ON");
                        out.println("Actuator " + actuatorId + " changed from " + previousStatus + " to ON");
                    }
                } else if ("TURN_OFF".equalsIgnoreCase(command)) {
                    if (status == Status.OFF) {
                        System.out.println("Actuator " + actuatorId + " is already OFF");
                        out.println("Actuator " + actuatorId + " is already OFF");
                    } else {
                        status = Status.OFF;
                        System.out.println("Actuator " + actuatorId + " turned OFF");
                        out.println("Actuator " + actuatorId + " changed from " + previousStatus + " to OFF");
                    }
                } else {
                    System.out.println("Unknown command received: " + command);
                    out.println("Unknown command received");
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling connection: " + e.getMessage());
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
            System.out.println("Actuator " + actuatorId + " disconnected from server");
            System.out.println("Please choose another name for the actuator.");
            System.out.println("Exiting the program.");
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Actuator ID: ");
        String actuatorId = scanner.nextLine();

        System.out.print("Enter Actuator Status (ON/OFF): ");
        String statusInput = scanner.nextLine();
        Status status;

        if ("ON".equalsIgnoreCase(statusInput)) {
            status = Status.ON;
        } else if ("OFF".equalsIgnoreCase(statusInput)) {
            status = Status.OFF;
        } else {
            System.out.println("Invalid status. Defaulting to OFF.");
            status = Status.OFF;
        }

        new Actuator(actuatorId, status);
        scanner.close();
    }
}
