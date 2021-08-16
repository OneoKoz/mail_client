package receipt;

public enum BoxType {
    SPAM("&BCEEPwQwBDw-"),
    _INBOX("INBOX"),
    SENT("&BB4EQgQ,BEAEMAQyBDsENQQ9BD0ESwQ1-"),
    TRASH("&BBoEPgRABDcEOAQ9BDA-"),
    DRAFTS("&BCcENQRABD0EPgQyBDgEOgQ4-");

    private final String name;

    BoxType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static BoxType resolveTypeById(String name) {
        for (BoxType boxType : BoxType.values()) {
            if (boxType.getName().equals(name)) {
                return boxType;
            }
        }
        return _INBOX;
    }

}
