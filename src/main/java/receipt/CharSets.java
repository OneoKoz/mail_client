package receipt;

import dispatch.MailType;
import org.apache.commons.codec.Charsets;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public enum CharSets {
    SET_UTF_8(StandardCharsets.UTF_8,"utf-8"),
    SET_ISO(StandardCharsets.ISO_8859_1,"iso-8859-1");

    private Charset standardCharsets;
    private String name;


    CharSets(Charset standardCharsets, String name) {
        this.standardCharsets = standardCharsets;
        this.name = name;
    }

    public Charset getStandardCharsets() {
        return standardCharsets;
    }

    public String getName() {
        return name;
    }

    public static CharSets resolveTypeById(String name) {
        for (CharSets charSets : CharSets.values()) {
            if (charSets.getName().equals(name)) {
                return charSets;
            }
        }
        return SET_UTF_8;
    }

}
