import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class Server {

    private static final int PORT = 12346;
    private final Set<ClientHandler> clientHandlers = new HashSet<>();

    private ServerSocket serverSocket;
    private JFrame frame;
    private JTextArea logArea;

    public Server() {
        createUI();
        startServer();
    }

    private void createUI() {
        frame = new JFrame("Chat Server");

        logArea = new JTextArea(20, 50);
        logArea.setEditable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                logArea.append("Server started...\n");

                while (true) {
                    Socket clientSocket = serverSocket.accept();

                    ClientHandler clientHandler =
                            new ClientHandler(clientSocket);

                    clientHandlers.add(clientHandler);

                    new Thread(clientHandler).start();
                }

            } catch (IOException e) {
                logArea.append(
                        "Error starting server: "
                        + e.getMessage() + "\n"
                );
            }
        }).start();
    }

    private void broadcast(String message, ClientHandler sender) {
        String fullMessage =
                sender.getClientName() + ": " + message;

        for (ClientHandler client : clientHandlers) {
            client.sendMessage(fullMessage);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Server::new);
    }

    class ClientHandler implements Runnable {

        private final Socket socket;
        private PrintWriter out;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream()
                                )
                        );

                out = new PrintWriter(
                        socket.getOutputStream(),
                        true
                );

                clientName = in.readLine();

                logArea.append(
                        clientName + " connected.\n"
                );

                String message;

                while ((message = in.readLine()) != null) {

                    logArea.append(
                            clientName + " says: "
                            + message + "\n"
                    );

                    String upperCaseMessage =
                            message.toUpperCase();

                    broadcast(upperCaseMessage, this);
                }

            } catch (IOException e) {
                logArea.append(
                        "Error handling client: "
                        + e.getMessage() + "\n"
                );

            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    logArea.append(
                            "Error closing socket: "
                            + e.getMessage() + "\n"
                    );
                }

                clientHandlers.remove(this);

                logArea.append(
                        clientName + " disconnected.\n"
                );
            }
        }

        public String getClientName() {
            return clientName;
        }

        private void sendMessage(String message) {
            out.println(message);
        }
    }
}