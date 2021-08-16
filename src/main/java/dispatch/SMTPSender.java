package dispatch;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class SMTPSender {

    private static DataOutputStream dos;
    private static final HashMap<String, String> allFileType =  ReadFileType.getAllFileType();

    private final String user;
    private final String pass;
    private final MailType mailType;
    private final int port = 465;
    private final int delay = 500;

    public SMTPSender(String user, String pass, MailType mailType) {
        this.user = user;
        this.pass = pass;
        this.mailType = mailType;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public MailType getMailType() {
        return mailType;
    }

    public int getPort() {
        return port;
    }

    public int getDelay() {
        return delay;
    }

    public void send(String recipient, String topicEmail, String textMessage, List<File> attachments) throws Exception {
        String username = new String(Base64.getEncoder().encode(user.getBytes()));
        String password = new String(Base64.getEncoder().encode(pass.getBytes()));

        SSLSocket sock = (SSLSocket) (SSLSocketFactory.getDefault()).createSocket(mailType.getAddress(), port);
        final BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        (new Thread(() -> {
            try {
                String line;
                while ((line = br.readLine()) != null)
                    System.out.println("SERVER: " + line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).start();

        dos = new DataOutputStream(sock.getOutputStream());

        send("EHLO " + mailType.getAddress());
        send("AUTH LOGIN");
        send(username);
        send(password);
        send("MAIL FROM: <" + user + ">");
        send("RCPT TO: <" + recipient + ">");
        send("DATA");
        send("Subject: =?utf-8?b?" + new String(Base64.getEncoder().encode(topicEmail.getBytes())));
        send("MIME-Version: 1.0");
        send("Content-Type: multipart/mixed; boundary=\"simple boundary\"");
        send("");
        send("--simple boundary");
        sendText(textMessage);
        if (!attachments.isEmpty()) {
            attachments.forEach(this::sendFile);
        }
        send(".");
        send("QUIT");
    }

    private void send(String s) {
        try {
            dos.writeBytes(s + "\r\n");
            System.out.println("CLIENT: " + s);
            Thread.sleep(delay);
        } catch (InterruptedException | IOException e) {
            //
        }
    }

    private void sendText(String textMessage) throws Exception {
        send("Content-Type: text/html; charset=utf-8");
        send("Content-Transfer-Encoding: base64");
        send("");
        send(Base64.getEncoder().encodeToString(textMessage.getBytes(StandardCharsets.UTF_8)));
        send("--simple boundary");
    }

    private void sendFile(File file) {
        try {
            send("Content-Type: " + getFileExtension(file) + "; name=\"" + file.getName() + "\"");
            send("Content-Transfer-Encoding: base64");
            send("Content-Disposition: attachment; filename=\"" + file.getName() + "\"");
            send("");
            send(encodeFileBase64(file));
            send("--simple boundary");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        String type = "";
        // если в имени файла есть точка и она не является первым символом в названии файла
        if (fileName.lastIndexOf(".") != -1 && fileName.indexOf(".") != 0) {
            // то вырезаем все знаки после последней точки в названии файла, то есть ХХХХХ.txt -> txt
            type = fileName.substring(fileName.lastIndexOf(".") + 1);
            // в противном случае возвращаем заглушку, то есть расширение не найдено
        }
        return allFileType.get(type) == null ? "text/plain" : allFileType.get(type);
    }

    private String encodeFileBase64(File file) {
        String encodedfile = null;
        try (FileInputStream fileInputStreamReader = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            fileInputStreamReader.read(bytes);
            encodedfile = Base64.getMimeEncoder().encodeToString(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encodedfile;

    }

}
