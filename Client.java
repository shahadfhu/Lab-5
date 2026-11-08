import java.io.*;
import java.net.*;
import java.util.Scanner;

// Client (User Interface): sends a word to the Process Server only
public class Client {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a word: ");
        String word = sc.nextLine().trim();

        try (Socket socket = new Socket("localhost", 7777);     // TCP to process server
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(word);                    // send the word
            System.out.println(in.readLine());    // print the result
        }
    }
}
