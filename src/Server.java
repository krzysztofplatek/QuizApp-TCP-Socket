import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(5000, 250);
            serverSocket.setReuseAddress(true);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Polaczono z nowym uzytkownikiem: " + clientSocket.getInetAddress().getHostAddress());
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


        private ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            String fromClient = "", fromServer = "";

            Questions questions = new Questions(clientSocket);
            String[] arrayQuestions;

            PrintWriter out = null;
            BufferedReader in = null;

            try {
                do {
                    arrayQuestions = questions.getQuestions();

                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    do {
                        for (int i = 1; i <= arrayQuestions.length; i++) {
                            if (i == arrayQuestions.length) {
                                int result = questions.calculateResult();
                                do {
                                    out.println("Ukonczono test [WYNIK: " + result + "] => 'exit' - wyjscie");
                                } while (!fromClient.equals("exit"));
                                fromServer = null;
                                break;
                            }

                            long start = 0;
                            long stop;

                            fromServer = arrayQuestions[i];
                            if (fromServer != null) {
                                out.println(fromServer);
                                start = System.currentTimeMillis();
                            }

                            fromClient = in.readLine();

                            stop = System.currentTimeMillis();

                            if ((stop - start) > 30000) {
                                do {
                                    out.println("Przekroczono czas 30s na odpowiedz => 'exit' - wyjscie");
                                } while (!fromClient.equals("exit"));
                            }

                            questions.passAnswerToFile(fromClient);

                            System.out.println("Client: " + fromClient);
                        }
                    } while (fromServer != null);
                } while (!fromClient.equals("exit"));
            } catch (IOException e) {
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