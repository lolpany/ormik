package lol.lolpany.ormik.regression;

import com.sun.mail.smtp.SMTPTransport;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.SQLException;
import java.util.Properties;


public class MailSender extends EnvironmentBoundAction<Void> {
    public static final Properties MAIL_SESSION_PROPERTIES;

    static {
        Properties mailSessionProperties = new Properties();
        mailSessionProperties.put("mail.store.protocol", "imap");
        mailSessionProperties.put("mail.imap.starttls.enable", "false");
        mailSessionProperties.put("mail.imap.statuscachetimeout", 0);
        mailSessionProperties.put("mail.imap.auth", "true");
        MAIL_SESSION_PROPERTIES = mailSessionProperties;
    }


    private String host;
    private int port;
    private String from;
    private String password;
    private String to;
    private String title;
    private String content;

    public MailSender(int envNumber, String host, int port, String from, String password, String to, String title,
                      String content) throws SQLException {
        super(envNumber);

        this.host = host;
        this.port = port;
        this.from = from;
        this.password = password;
        this.to = to;
        this.title = title;
        this.content = content;
    }

    @Override
    protected Void runOn(Integer envNumber) throws Exception {
        Session session = Session.getInstance(MAIL_SESSION_PROPERTIES);
        final MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        msg.setSubject(title);
        msg.setText(content, "utf-8");
        SMTPTransport t = (SMTPTransport) session.getTransport("smtp");

        t.connect(host, port, from, password);
        t.sendMessage(msg, msg.getAllRecipients());
        t.close();
        return null;
    }
}
