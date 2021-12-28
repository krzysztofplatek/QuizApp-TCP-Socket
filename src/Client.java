import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        String fromServer, fromUser;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            socket = new Socket("127.0.0.1", 5000);
            System.out.println("Oczekiwanie na wiadomosc z serwera...");
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Nieznany adres hosta");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Bład operacji I/O");
            System.exit(1);
        }

        while ((fromServer = in.readLine()) != null) {
            String[] que = fromServer.split("#");
            System.out.println("Server: ");
            for (String s : que) System.out.println(s);

            System.out.print("Client: ");
            fromUser = reader.readLine();

            if (!fromUser.equals("exit")) {
                out.println(fromUser);
            } else {
                break;
            }

        }

        out.close();
        in.close();
        reader.close();
        socket.close();
    }
}