package dispatch;

public enum MailType {
    MAIL_RU("smtp.mail.ru"),
    GMAIL_COM("smtp.gmail.com"),
    YANDEX_RU("smtp.yandex.ru");

    private final String address;

    MailType(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public static MailType resolveTypeById(String name) {
        for (MailType mailType : MailType.values()) {
            if (mailType.getAddress().equals(name)) {
                return mailType;
            }
        }
        return MAIL_RU;
    }
}
