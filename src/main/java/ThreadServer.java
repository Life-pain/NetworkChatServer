import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentSkipListSet;

public class ThreadServer implements Runnable {
    private static ConcurrentSkipListSet<ThreadForClient> clientsSet = new ConcurrentSkipListSet<>();
    private ThreadForClient newThreadClient;
    private final String settingsPath = "./src/main/resources/settings.txt";
    private FileWriter writerToLog;
    private final String logPath = "./src/main/resources/file.log";
    private SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Override
    public void run() {
        try (ServerSocket servSocket = new ServerSocket(getPort())) {
            writerToLog = new FileWriter(logPath, true);
            writerToLog.write(formatter.format(new Date(System.currentTimeMillis())) + " сервер запущен\n");
            writerToLog.flush();
            while (true) {
                try {
                    Socket clientSocket = servSocket.accept();
                    writerToLog.write(formatter.format(new Date(System.currentTimeMillis())) + " новое подключение\n");
                    writerToLog.flush();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    out.println("Введите имя:");
                    String name = in.readLine();
                    newThreadClient = new ThreadForClient(clientSocket, name);
                    clientsSet.add(newThreadClient);
                    newThreadClient.start();
                } catch (IOException ex) {
                    ex.printStackTrace(System.out);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        int result = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(settingsPath));) {
            result = Integer.parseInt(reader.readLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private class ThreadForClient extends Thread implements Comparable {

        private PrintWriter out;
        private Socket clientSocket;
        private String threadName;
        private FileWriter writerToLogForClient;

        public ThreadForClient(Socket socket, String name) {
            super(name);
            threadName = name;
            clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ) {
                writerToLogForClient = new FileWriter(logPath, true);
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                String msg;
                writerToLogForClient.write(formatter.format(new Date(System.currentTimeMillis())) + " Клиент \""
                        + threadName + "\" подключился\n");
                writerToLogForClient.flush();
                for (ThreadForClient clientThread : clientsSet) {
                    if (clientThread == this) continue;
                    clientThread.out.println("Пользователь " + threadName + " вошел в чат");
                }
                while (true) {
                    msg = in.readLine();
                    if (msg.equals("exit")) {
                        out.println(msg);
                        break;
                    } else {
                        writerToLogForClient.write(formatter.format(new Date(System.currentTimeMillis()))
                                + " " + threadName + ": " + msg + "\n");
                        writerToLogForClient.flush();
                        for (ThreadForClient clientThread : clientsSet) {
                            if (clientThread == this) continue;
                            clientThread.out.printf("%s: %s\n", threadName, msg);
                        }
                    }
                }
                writerToLogForClient.write(formatter.format(new Date(System.currentTimeMillis())) +
                        " Клиент \"" + threadName + "\" отключился\n");
                writerToLogForClient.flush();
                for (ThreadForClient clientThread : clientsSet) {
                    if (clientThread == this) continue;
                    clientThread.out.println("Пользователь " + threadName + " вышел из чата");
                }
                writerToLogForClient.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int compareTo(Object o) {
            return clientSocket.toString().compareTo(o.toString());
        }
    }
}
