import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 12345;

        try (Socket socket = new Socket(hostname, port)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // دریافت پیام از سرور
            String message = in.readLine();
            System.out.println("Message from server: " + message);

            // ارسال پیام به سرور
            String clientMessage = "Hello from client!";
            out.println(clientMessage);
            System.out.println("Message sent to server: " + clientMessage);

            // خواندن محتوای فایل از سرور
            String fileContent = in.readLine();
            System.out.println("File content: " + fileContent);

        } catch (UnknownHostException e) {
            System.out.println("Server not found: " + hostname);
        } catch (IOException e) {
            System.out.println("Error in I/O: " + e.getMessage());
        }
    }
}