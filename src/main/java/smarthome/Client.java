package smarthome;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter device ID or exit: ");
            String deviceId = scanner.nextLine();
            if (deviceId.equalsIgnoreCase("exit")) {
                System.out.println("Exiting the program");
                break;
            }

            System.out.print("Enter command (TURN_ON / TURN_OFF): ");
            String command = scanner.nextLine().toUpperCase();

            if (!command.equals("TURN_ON") && !command.equals("TURN_OFF")) {
                System.out.println("Invalid command. Please enter TURN_ON or TURN_OFF.");
                continue;
            }

            try {
                URL url = new URL("http://192.168.1.23:8080/actuators/" + deviceId + "/updateStatus");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String jsonInput = "{\"status\": \"" + command + "\"}";

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInput.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    System.out.println("Status changed successfully.");
                } else {
                    System.out.println("Error: Actuator not found or invalid status.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
