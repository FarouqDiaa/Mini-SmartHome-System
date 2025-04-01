package smarthome;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SmarthomeApplication {

    public static final ConcurrentHashMap<String, PrintWriter> actuators = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(SmarthomeApplication.class, args);
        new Thread(() -> startTcpServer(5001)).start();
    }

    private static void startTcpServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TCP Server started on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleDeviceConnection(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting TCP server: " + e.getMessage());
        }
    }

    private static void handleDeviceConnection(Socket socket) {
        String deviceId = null;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            deviceId = in.readLine();
            if (deviceId != null && !deviceId.isBlank()) {
                synchronized (actuators) {
                    if (actuators.containsKey(deviceId)) {
                        System.out.println("Device ID " + deviceId + " is already in use. Rejecting connection.");
                        return;
                    }
                    actuators.put(deviceId, out);
                }
                System.out.println("Added " + deviceId);
            }

            while (socket.isConnected()) {
                String message = in.readLine();
                if (message == null) {
                    break;
                }
                System.out.println("Received from " + deviceId + ": " + message);
            }

        } catch (IOException e) {
            System.err.println("Error handling device: " + e.getMessage());
        }
        if (deviceId != null) {
            synchronized (actuators) {
                actuators.remove(deviceId);
            }
            System.out.println(deviceId + " disconnected");
        }

    }
}

@RestController
@RequestMapping("/actuators")
class ActuatorController {

    @PostMapping("/{deviceId}/updateStatus")
    public ResponseEntity<?> updateStatus(@PathVariable String deviceId, @RequestBody StatusUpdateRequest request) {
        PrintWriter out;
        synchronized (SmarthomeApplication.actuators) {
            out = SmarthomeApplication.actuators.get(deviceId);
        }

        if (out == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Device not found");
        }

        try {
            synchronized (out) {
                out.println(request.getStatus());
            }
            return ResponseEntity.ok("Command sent to " + deviceId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending command");
        }
    }

    static class StatusUpdateRequest {

        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
