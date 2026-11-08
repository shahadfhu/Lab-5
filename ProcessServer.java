import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

// Process Server: receives a word from clients, gets the files from the
// Data Server, counts the occurrences and returns the result (port 7777)
public class ProcessServer {
    static final int PORT = 7777;
    static final String DATA_HOST = "localhost";
    static final int DATA_PORT = 8888;

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Process Server running on port " + PORT);

        while (true) {
            Socket client = server.accept();                    // new client
            new Thread(new ClientHandler(client)).start();      // handled simultaneously
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket client;
        ClientHandler(Socket client) { this.client = client; }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

                String word = in.readLine();                     // word from the client
                System.out.println("[" + Thread.currentThread().getName() + "] word: " + word);

                int total = 0;
                // TCP connection to the Data Server
                try (Socket dataSocket = new Socket(DATA_HOST, DATA_PORT);
                     DataInputStream dataIn = new DataInputStream(dataSocket.getInputStream())) {

                    int fileCount = dataIn.readInt();
                    for (int i = 0; i < fileCount; i++) {
                        String name = dataIn.readUTF();
                        byte[] bytes = new byte[dataIn.readInt()];
                        dataIn.readFully(bytes);
                        String text = new String(bytes, StandardCharsets.UTF_8);
                        total += count(text, word);
                    }
                }
                out.println("The word \"" + word + "\" appears " + total + " time(s).");
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            } finally {
                try { client.close(); } catch (IOException ignored) {}
            }
        }

        // Count whole-word matches, ignoring case
        static int count(String text, String word) {
            int c = 0;
            for (String w : text.split("[^\\p{L}\\p{N}]+"))
                if (w.equalsIgnoreCase(word)) c++;
            return c;
        }
    }
}
