
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class RATClient {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in);
             Socket socket = new Socket("192.168.178.69", 8080);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                System.out.println("Connected to Server at: '" + socket.getInetAddress().getHostAddress() + "' Enter a command:");

            while (true) {
                System.out.print("Enter command: ");
                String command = scanner.nextLine();

                // Check if the command is "exit"
                if ("exit".equalsIgnoreCase(command)) {
                    System.out.println("Exiting client...");
                    break; // Exit the loop, which will close the client
                }

                out.println(command);
                System.out.println("\n[Command Sent: " + command + "]\n");

                // Read the server's response
                String serverResponse;
                System.out.println("[Command Output Start]");
                while (true) {
                    serverResponse = in.readLine();
                    if (serverResponse == null || serverResponse.equals("END_OF_RESPONSE")) {
                        break;
                    }
                    System.out.println(serverResponse);
                }
                System.out.println("[Command Output End]\n");

                System.out.print("Enter next command, or type 'exit' to quit:\n");
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
