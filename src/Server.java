import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Server {
    static String url = "jdbc:mysql://localhost:3306/quizappdb";
    static String userName = "root";
    static String password = "1234";
    static ResultSet resultSet;
    static Connection connection;
    static Statement statement;
    static String query = "select * from questions";

    public static void main(String[] args) {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(5000, 250);
            serverSocket.setReuseAddress(true);


            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Polaczono z nowym uzytkownikiem: " + clientSocket.getInetAddress().getHostAddress());

                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    connection = DriverManager.getConnection(url, userName, password);
                    statement = connection.createStatement();
                    resultSet = statement.executeQuery(query);
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;


        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        private ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public synchronized void run() {
            String fromClient = "";

            PrintWriter out = null;
            BufferedReader in = null;

            int points = 0;

            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                while (resultSet.next()) {
                    long start = 0;
                    long stop;

                    out.println(resultSet.getString("title") + "#" + resultSet.getString("content"));

                    start = System.currentTimeMillis();

                    fromClient = in.readLine();

                    stop = System.currentTimeMillis();

                    if ((stop - start) > 30000) {
                        do {
                            out.println("Przekroczono czas 30s na odpowiedz => 'exit' - wyjscie");
                        } while (!fromClient.equals("exit"));
                    }

                    if (fromClient.equals(resultSet.getString("correctanswer"))) {
                        points++;
                    }

                    System.out.println("Client: " + fromClient);
                }

                Date date = new Date(System.currentTimeMillis());

                String queryInsert = "insert into users values ('" + clientSocket.getPort() + "', '" + formatter.format(date) + "', '" + points + "')";
                statement.executeUpdate(queryInsert);

                do {
                    out.println("Ukonczono test [WYNIK: " + points + "] => 'exit' - wyjscie");
                } while (!fromClient.equals("exit"));

            } catch (IOException | SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}