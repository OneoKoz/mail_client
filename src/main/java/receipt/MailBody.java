package receipt;

import dispatch.ReadFileType;
import receipt.decoder.DecoderFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MailBody {

    private String encoding = "";
    private String fileType;
    private String body;
    private String disposition = "";
    private String name;
    private String charset;

    private final DecoderFactory decoderFactory;
    public static HashMap<String, String> allFileType = ReadFileType.getAllFileType();

    public MailBody(String line) {
        String[] lines = line.split("\n");
        for (String s : lines) {
            if (s.contains("Content-Transfer-Encoding: ")) {
                encoding = s.split(": ")[1].toLowerCase();
            } else if (s.contains("Content-Type: ")) {
                String[] temp = s.split(": |;");
                fileType = allFileType.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(temp[1].trim().toLowerCase())).findAny()
                        .toString();
                fileType = fileType.split("[\\[=]")[1];
            } else if (s.contains("Content-Disposition: ")) {
                String[] temp = s.split(": |;");
                disposition = temp[1];
            } else {
                if (s.startsWith("charset=") && !s.startsWith("charset=3D")) {
                    String[] temp = s.split("=");
                    charset = temp[1].trim().toLowerCase();
                } else if (s.contains("name=") && (body == null || body.isEmpty())) {
                    String[] temp = s.split("\"");
                    name = temp[1];
                } else if (!s.startsWith("MIME-Version:")) {
                    body = body != null ? body.concat(s.trim()) : s.trim();
                }
            }
        }
        if (Objects.equals(fileType, null)) {
            fileType = "";
            if (body.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$")) {
                encoding = "base64";
            } else {
                encoding = "";
            }
        }
        decoderFactory = new DecoderFactory(this);
        if (Objects.equals(disposition, "attachment") && fileType.isEmpty()) {
            fileType = "txt";
        }
    }

    public boolean isHtmlText() {
        return Objects.equals(allFileType.get(fileType), "text/html");
    }

    public boolean isPlaintext() {
        return Objects.equals(allFileType.get(fileType), "text/plain");
    }

    public byte[] decode() {
        return decoderFactory.decode(body);
    }

    public String getName() {
        return name;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getFileType() {
        return fileType;
    }

    public String getDisposition() {
        return disposition;
    }
}
