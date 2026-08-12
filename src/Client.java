import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class Client {

    private String serverAddress;
    private int serverPort;
    private String clientName;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private JFrame frame;
    private JTextArea messageArea;
    private JTextField inputField;
    private JButton sendButton;

    public Client(String serverAddress, int serverPort, String clientName) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.clientName = clientName;

        createUI();
        connectToServer();
    }

    private void createUI() {
        frame = new JFrame("Chat Client - " + clientName);

        messageArea = new JTextArea(20, 50);
        messageArea.setEditable(false);

        inputField = new JTextField(40);
        sendButton = new JButton("Send");

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(
                new JScrollPane(messageArea),
                BorderLayout.CENTER
        );

        JPanel inputPanel = new JPanel(new FlowLayout());

        inputPanel.add(inputField);
        inputPanel.add(sendButton);

        panel.add(
                inputPanel,
                BorderLayout.SOUTH
        );

        frame.getContentPane().add(panel);
        frame.pack();

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        frame.setVisible(true);

        sendButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        sendMessage();
                    }
                }
        );

        inputField.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        sendMessage();
                    }
                }
        );
    }

    private void connectToServer() {
        try {
            socket = new Socket(
                    serverAddress,
                    serverPort
            );

            out = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

            in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );

            out.println(clientName);

            new Thread(
                    new IncomingReader()
            ).start();

        } catch (IOException e) {
            messageArea.append(
                    "Unable to connect to server: "
                    + e.getMessage()
                    + "\n"
            );
        }
    }

    private void sendMessage() {
        String message = inputField.getText();

        if (!message.isEmpty()) {
            messageArea.append(
                    "You: " + message + "\n"
            );

            out.println(message);

            inputField.setText("");
        }
    }

    private class IncomingReader implements Runnable {

        @Override
        public void run() {
            String message;

            try {
                while ((message = in.readLine()) != null) {
                    messageArea.append(
                            message + "\n"
                    );
                }

            } catch (IOException e) {
                messageArea.append(
                        "Error reading from server: "
                        + e.getMessage()
                        + "\n"
                );
            }
        }
    }

    public static void main(String[] args) {

        String serverAddress = "localhost";
        int serverPort = 12346;

        String clientName =
                JOptionPane.showInputDialog(
                        "Enter your name:"
                );

        SwingUtilities.invokeLater(
                () -> new Client(
                        serverAddress,
                        serverPort,
                        clientName
                )
        );
    }
}