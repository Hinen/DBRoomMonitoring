package Manager;

import Data.Constants;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

public class SMTPManager {
    private static SMTPManager singleton = new SMTPManager();
    public static SMTPManager get() { return singleton; }

    private Properties prop;
    private Session session;
    private InternetAddress senderAddress;
    private InternetAddress[] receiverAddress;

    private class Mail {
        public String title;
        public String text;
        public int type;

        public Mail(String title, String text, int type) {
            this.title = title;
            this.text = text;
            this.type = type;
        }
    }

    private List<Mail> mailList = Collections.synchronizedList(new LinkedList<Mail>());
    private List<MessagingException> exceptionList = Collections.synchronizedList(new LinkedList<MessagingException>());

    private SMTPManager() {
        System.out.println("Initializing Manager.SMTPManager...");

        try {
            prop = new Properties();
            prop.put("mail.smtp.host", Constants.SMTPConfig.SMTP_TYPE);
            prop.put("mail.smtp.port", Constants.SMTPConfig.SMTP_PORT);
            prop.put("mail.smtp.ssl.protocols", Constants.SMTPConfig.TLS_VERSION);
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.auth", "true");

            session = Session.getDefaultInstance(prop, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    String pass = new String(Base64.getDecoder().decode(Constants.SMTPConfig.MAIL_PASSWORD));
                    return new PasswordAuthentication(Constants.SMTPConfig.MAIL_USER, pass);
                }
            });

            senderAddress = new InternetAddress(Constants.SMTPConfig.MAIL_USER);

            //
            List<InternetAddress> receiverAddressList = new ArrayList<>();
            for (int i = 0; i < Constants.SMTPTarget.MAIL_RECEIVER.length; i++)
                receiverAddressList.add(new InternetAddress(Constants.SMTPTarget.MAIL_RECEIVER[i]));

            receiverAddress = receiverAddressList.toArray(new InternetAddress[receiverAddressList.size()]);
        } catch (AddressException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        synchronized (mailList) {
            for (Mail mail : mailList) {
                try {
                    sendMail(mail);
                } catch (MessagingException e) {
                    if (mail.type == Constants.MonitoringType.SMTP_EXCEPTION) {
                        // 보낼 메일의 Type이 SMTP_EXCEPTION 인데 또 exception이 난거라면 exceptionList에 넣어도 의미없으니 printStackTrace만 한다.
                        e.printStackTrace();
                    } else {
                        // 데드락 혹은 ConcurrentException이 발생 할 위험이 있으므로 exception이 날 경우 list에 넣어두고 sync문 밖에서 따로 처리한다.
                        exceptionList.add(e);
                    }
                }
            }

            mailList.clear();
        }

        synchronized (exceptionList) {
            for (MessagingException e : exceptionList)
                addMail(e, Constants.MonitoringType.SMTP_EXCEPTION);

            exceptionList.clear();
        }
    }

    private void sendMail(Mail mail) throws MessagingException {
        String requestStr = String.format("Request Send %s Mail...", Constants.MonitoringType.getMonitoringTypeStr(mail.type));
        System.out.println(requestStr);

        //
        MimeMessage message = new MimeMessage(session);
        message.setFrom(senderAddress);
        message.addRecipients(Message.RecipientType.TO, receiverAddress);

        message.setSubject(mail.title);
        message.setText(mail.text);

        Transport.send(message);

        String outPut = mail.type == Constants.MonitoringType.MONITORING_EXCEPTION ? "Success Send Exception Mail" : "Success Send Mail";
        System.out.println(outPut);
    }

    public void addMail(final String title, final String text, final int type) {
        synchronized (mailList) {
            mailList.add(new Mail(title, text, type));
        }
    }

    public void addMail(final Exception e) {
        addMail(e, Constants.MonitoringType.MONITORING_EXCEPTION);
    }

    public void addMail(final Exception e, final int type) {
        e.printStackTrace();

        if (type != Constants.MonitoringType.DB_EXECUTE_QUERY_FAIL &&
            type != Constants.MonitoringType.MONITORING_EXCEPTION)
            return;

        synchronized (mailList) {
            String title = "Monitoring Server Exception_" + DateManager.get().getNowTime();
            String text = e.getMessage() + "\n\n";
            StackTraceElement[] stackTraceElements = e.getStackTrace();
            for (int i = 0; i < stackTraceElements.length; i++)
                text += stackTraceElements[i] + "\n";

            if (e.getCause() != null)
                text += "\n\n" + e.getCause();

            mailList.add(new Mail(title, text, type));
        }
    }
}
