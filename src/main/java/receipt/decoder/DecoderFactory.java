package receipt.decoder;

import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.apache.commons.codec.binary.Base64;
import receipt.MailBody;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class DecoderFactory implements Idecoder {

    public Idecoder getIdecoder() {
        return idecoder;
    }

    private Idecoder idecoder;

    public DecoderFactory(MailBody mailBody) {
        chooseDecoder(mailBody);
    }

    private void chooseDecoder(MailBody mailBody) {
        switch (mailBody.getEncoding().trim().toLowerCase()) {
            case "quoted-printable":
                idecoder = new QuotedPrintable();
                break;
            case "base64":
                idecoder = new MyBase64(mailBody.getDisposition());
                break;
            case "7bit":
                idecoder = null;
                break;
            case "":
                idecoder = null;
        }

//        if (line.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$")) {
//            idecoder = new MyBase64();
//            return;
//        }

    }

    @Override
    public byte[] decode(String line) {
        if (Objects.equals(idecoder, null)) {
            return line.getBytes(StandardCharsets.UTF_8);
        }
        return idecoder.decode(line);
    }

}
