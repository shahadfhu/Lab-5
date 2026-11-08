import java.io.*;
import java.net.*;
import java.nio.file.*;

// Data Server: stores the files and sends them to the Process Server (port 8888)
public class DataServer {
    static final int PORT = 8888;
    static final String FOLDER = "data";   // folder that contains the text files

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Data Server running on port " + PORT);

        while (true) {
            Socket socket = server.accept();               // wait for the process server
            new Thread(() -> sendFiles(socket)).start();   // serve each request in a thread
        }
    }

    static void sendFiles(Socket socket) {
        try (DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            File[] files = new File(FOLDER).listFiles(File::isFile);
            if (files == null) files = new File[0];

            out.writeInt(files.length);                    // 1) number of files
            for (File f : files) {
                byte[] content = Files.readAllBytes(f.toPath());
                out.writeUTF(f.getName());                 // 2) file name
                out.writeInt(content.length);              // 3) file size
                out.write(content);                        // 4) file content
            }
            out.flush();
            System.out.println("Sent " + files.length + " file(s) to " + socket.getInetAddress());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
