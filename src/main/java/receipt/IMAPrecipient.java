package receipt;

import dispatch.MailType;
import org.w3c.dom.ls.LSOutput;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.table.TableRowSorter;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class IMAPrecipient {

    private static DataOutputStream dos;
    private static final int delay = 500;
    private final int flag = 500;
    private volatile int count;
    private volatile int numMail = -1;
    private final List<String> allMail = new ArrayList<>();
    private List<String> mailBody;
    private StringBuilder stringBuilder = new StringBuilder();
    private StringBuilder saveBuilder = new StringBuilder();
    private volatile boolean checker;
    private BoxType boxType = BoxType._INBOX;

    private final String user;
    private final String pass;
    private final MailType mailType;

    public IMAPrecipient(String user, String pass, MailType mailType) {
        this.user = user;
        this.pass = pass;
        this.mailType = mailType;
    }

    public void setNumMail(int numMail) {
        this.numMail = numMail;
    }

    public void setBoxType(BoxType boxType) {
        this.boxType = boxType;
    }

    public int getNumMail() {
        return numMail;
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

    public void receive() throws Exception {
        allMail.clear();
        numMail = -1;
        SSLSocket sock = (SSLSocket) (SSLSocketFactory.getDefault())
                .createSocket(mailType.getAddress().replace("smtp", "imap"), 993);
        final BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

        Thread serverTH = new Thread(() -> {
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println("SERVER: " + line);
                    if (numMail == -1) {
                        if (line.contains("* ") && line.contains(" EXISTS")) {
                            String[] temp = line.split(" ");
                            numMail = Integer.parseInt(temp[1]);
                        }
                    }
                    collectHeaderMail(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverTH.start();

        dos = new DataOutputStream(sock.getOutputStream());
        send(null);
        send("A1 LOGIN " + user + " " + pass);
        send("A2 SELECT " + boxType.getName());
        while(numMail==-1)Thread.onSpinWait();
        if ((numMail - flag + 1) > 0) {
            count = flag;
            send("A3 FETCH " + (numMail - flag + 1) + ":* (BODY[HEADER.FIELDS (Date from subject)])");
        } else {
            count = numMail;
            send("A3 FETCH " + 1 + ":* (BODY[HEADER.FIELDS (Date from subject)])");
        }

        send("A4 LOGOUT");
        while (count > 0) Thread.onSpinWait();
    }

    private void send(String s) throws Exception {
        if (s != null) {
            dos.writeBytes(s + "\n");
            System.out.println("CLIENT: " + s);
        }
        Thread.sleep(delay);
    }

    public void receiveMailBody(int numberMail) {
        mailBody = new ArrayList<>();
        stringBuilder = new StringBuilder();
        try {
            final String[] line = new String[1];
            SSLSocket sock = (SSLSocket) (SSLSocketFactory.getDefault())
                    .createSocket(mailType.getAddress().replace("smtp", "imap"), 993);
            final BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            Thread readTH = new Thread(() -> {
                try {
                    while ((line[0] = br.readLine()) != null) {
                        System.out.println("SERVER: " + line[0]);
                        if (line[0].startsWith("A4 ")) {
                            if(stringBuilder.toString().isEmpty()){
                                allMail.add(saveBuilder.toString());
                            }
                            br.close();
                            dos.close();
                            sock.close();
                            return;
                        }
                        collectBodyMail(line[0]);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readTH.start();

            dos = new DataOutputStream(sock.getOutputStream());
            send(null);
            send("A1 LOGIN " + user + " " + pass);
            send("A2 SELECT " + boxType.getName());
            send("A3 FETCH " + numberMail + " (BODY[TEXT])");
            send("A4 LOGOUT");
            //Thread.onSpinWait();
            while (readTH.isAlive()) Thread.sleep(delay);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean checkConnection() {
        try {
            SSLSocket sock = (SSLSocket) (SSLSocketFactory.getDefault())
                    .createSocket(mailType.getAddress().replace("smtp", "imap"), 993);
            final BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            final String[] line = new String[1];
            (new Thread(() -> {
                try {
                    while ((line[0] = br.readLine()) != null) {
                        System.out.println("SERVER: " + line[0]);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            })).start();

            dos = new DataOutputStream(sock.getOutputStream());
            send(null);
            send("A0 LOGIN " + user + " " + pass);
            while (!line[0].contains("A0")) Thread.onSpinWait();
            if (line[0].contains("A0 OK ")) {
                send("A1 LOGOUT");
                return false;
            } else {
                send("A1 LOGOUT");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void collectHeaderMail(String line) {
        if (count > 0) {
            if (!line.isEmpty() && line.charAt(0) != '*' && !line.equals(")")) {
                String temp = line.toLowerCase();
                if (temp.contains("from:") || temp.contains("subject:") || temp.contains("date:")) {
                    stringBuilder.append("\n");
                }
                stringBuilder.append(line).append(" ");
            }
            if (line.equals(")")) {
                allMail.add(0, stringBuilder.toString());
                stringBuilder = new StringBuilder();
                int temp = count - 1;
                count = temp;
            }
        }
    }

    private void collectBodyMail(String line) {
        if ((line.startsWith("Content-Type: ") || line.startsWith("Content-Transfer-Encoding: ")
                || line.contains("charset=") || line.startsWith("Content-Disposition: ")
                || line.contains("Content-Id: "))
                && !line.startsWith("Content-Type: multipart/")) {
            stringBuilder.append("\n").append(line
                    .replace("charset=", "\ncharset=")
                    .replace("Content-Transfer-Encoding: ", "\nContent-Transfer-Encoding: "));
            checker = true;
            return;
        }
        if (line.length() > 1 && line.charAt(0) == '-' && line.charAt(1) == '-') {
            checker = false;
            mailBody.add(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            return;
        }
        if(line.length() > 1 && line.charAt(0) == ')'){
            return;
        }
        if (checker) {
            stringBuilder.append("\n").append(line.length() > 0 ? line : " ");
        } else {
            saveBuilder.append(line);
        }

    }

    public List<String> updateMail() {
        final int[] newNumber = {-1};
        List<String> newMails = new ArrayList<>();
        try {
            SSLSocket sock = (SSLSocket) (SSLSocketFactory.getDefault())
                    .createSocket(mailType.getAddress().replace("smtp", "imap"), 993);
            final BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            (new Thread(() -> {
                try {
                    String line;
                    while ((line = br.readLine()) != null) {
                        System.out.println("SERVER: " + line);
                        if (newNumber[0] == -1) {
                            if (line.contains("* ") && line.contains(" EXISTS")) {
                                String[] temp = line.split(" ");
                                newNumber[0] = Integer.parseInt(temp[1]) - numMail;
                                numMail = Integer.parseInt(temp[1]);
                                count = newNumber[0];
                            }
                        }
                        collectHeaderMail(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            })).start();

            dos = new DataOutputStream(sock.getOutputStream());
            send(null);
            send("A1 LOGIN " + user + " " + pass);
            send("A2 SELECT " + boxType.getName());
            send("A3 FETCH " + (numMail - newNumber[0] + 1) + ":* (BODY[HEADER.FIELDS (Date from subject)])");
            send("A4 LOGOUT");
            while (count > 0) Thread.onSpinWait();
            for (int i = 0; i < newNumber[0]; i++) {
                newMails.add(allMail.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newMails;
    }

    public List<String> getAllMail() {
        return allMail;
    }

    public List<String> getMailBody() {
        return mailBody;
    }

    public boolean deleteMail(int number) {
        try {
            SSLSocket sock = (SSLSocket) (SSLSocketFactory.getDefault())
                    .createSocket(mailType.getAddress().replace("smtp", "imap"), 993);
            final BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            final String[] line = new String[1];
            (new Thread(() -> {
                try {
                    while ((line[0] = br.readLine()) != null) {
                        System.out.println("SERVER: " + line[0]);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            })).start();

            dos = new DataOutputStream(sock.getOutputStream());
            send(null);
            send("A0 LOGIN " + user + " " + pass);
            send("A1 SELECT " + boxType.getName());
            send("A2 STORE "+number+" +Flags (\\Deleted)");
            if(!boxType.getName().equals(BoxType.TRASH.getName())){
                send("A3 COPY "+number+":"+number+" "+BoxType.TRASH.getName());
            }
            send("A4 EXPUNGE");
            send("A5 LOGOUT");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}

