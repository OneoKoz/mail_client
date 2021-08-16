package receipt.decoder;

import java.util.Base64;

public class MyBase64 implements Idecoder{

    String disposition;

    public MyBase64(String disposition) {
        this.disposition = disposition;
    }

    @Override
    public byte[] decode(String line) {
        if(disposition.equals("inline")){
            return Base64.getDecoder().decode(line);
        }
        return Base64.getMimeDecoder().decode(line);
    }
}
