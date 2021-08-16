package receipt.decoder;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;

import java.nio.charset.StandardCharsets;


public class QuotedPrintable implements Idecoder {

    QuotedPrintableCodec quotedPrintableCodec = new QuotedPrintableCodec(Charsets.ISO_8859_1);

    @Override
    public byte[] decode(String line) {
        try {
            return quotedPrintableCodec.decode(line).getBytes();
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        return null;
    }
}

