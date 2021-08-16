package receipt;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class MailInfoHeader {

    private String from;
    private String subtract;
    private String date;


    public MailInfoHeader(String line) throws UnsupportedEncodingException {
        String[] info = line.split("\n|: ");
        for (int i = 0; i < info.length; i++) {
            switch (info[i].toLowerCase()) {
                case "from":
                    from = info[++i].toLowerCase().trim().startsWith("=?utf-8?") ? decodeBase64(info[i]) : info[i];
                    break;
                case "subject":
//                    subtract = info[++i].matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$") ?
//                            new String(Base64.getDecoder().decode(info[i])) : new String(info[i].getBytes(StandardCharsets.ISO_8859_1));
                    subtract = info[++i].toLowerCase().trim().startsWith("=?utf-8?") ? decodeBase64(info[i]) : info[i];
                    break;
                case "date":
                    date = info[++i];
                    break;
            }
        }
    }

    private String decodeBase64(String line) {
        StringBuilder stringBuilder = new StringBuilder();
        if (line.contains("?=<")) {
            line = line.replace("?=<", "?= <");
        }
        String[] lines = line.split("\\s|\\?=\\s");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].toLowerCase().startsWith("=?utf-8?")) {
                String[] temp = lines[i].split("\\?");
                if (temp[2].equalsIgnoreCase("q")) {
                    QuotedPrintableCodec quotedPrintableCodec = new QuotedPrintableCodec(Charsets.UTF_8);
                    try {
                        stringBuilder.append(quotedPrintableCodec.decode(temp[temp.length - 1]));
                    } catch (DecoderException e) {
                        e.printStackTrace();
                    }
                } else {
                    stringBuilder.append(new String(Base64.getDecoder().decode(temp[temp.length - 1])));
                }
                continue;
            }
            stringBuilder.append(" ").append(lines[i]);
        }
        return stringBuilder.toString();
    }

    public String getFrom() {
        return from;
    }

    public String getSubtract() {
        return subtract;
    }

    public String getDate() {
        return date;
    }
}
