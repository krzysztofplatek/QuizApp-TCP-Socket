import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Questions {
    private BufferedReader reader1;
    private BufferedReader reader2;
    private FileWriter writer;
    private BufferedWriter out;
    private FileWriter writer2;
    private BufferedWriter out2;
    private String line;
    private final String questionsFile = "bazaPytan.txt";
    private String studentAnswers;
    private String studentResults;
    private final String goodAnswers = "prawidloweOdpowiedzi.txt";


    public Questions(Socket socket) {
        try {
            studentResults = "wyniki" + socket.getPort() + ".txt";
            studentAnswers = "bazaOdpowiedzi" + socket.getPort() + ".txt";

            Path path = Paths.get(studentAnswers);
            Path path2 = Paths.get(studentResults);

            reader1 = new BufferedReader(new FileReader(questionsFile));
            reader2 = new BufferedReader(new FileReader(goodAnswers));
            Files.deleteIfExists(path);
            Files.deleteIfExists(path2);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public String[] getQuestions() throws IOException {
        String concatenateStrings = "";

        Lock lock = new ReentrantLock();

        lock.lock();
        try {
            while ((line = reader1.readLine()) != null) {
                concatenateStrings = concatenateStrings.concat(line);
            }
        } finally {
            lock.unlock();
        }

        return concatenateStrings.split("-");
    }

    public void passAnswerToFile(String answer) {
        try {
            writer = new FileWriter(studentAnswers, true);
            out = new BufferedWriter(writer);
            out.write(answer + "\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void passResultToFile(int result) {
        try {
            writer2 = new FileWriter(studentResults, true);
            out2 = new BufferedWriter(writer2);
            out2.write("WYNIK => " + result + "\n");
            out2.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int calculateResult() {
        int points = 0;

        Lock lock = new ReentrantLock();

        lock.lock();

        try {
            BufferedReader reader1 = Files.newBufferedReader(Path.of(studentAnswers));
            BufferedReader reader2 = Files.newBufferedReader(Path.of(goodAnswers));

            String line1, line2;
            while ((line1 = reader1.readLine()) != null) {
                line2 = reader2.readLine();
                if (line1.equals(line2)) {
                    points++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        passResultToFile(points);
        return points;
    }
}


