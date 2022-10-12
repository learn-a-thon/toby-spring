package toby.spring.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DummyMailSender implements MailSender {
    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        log.info("메일 발송");
        log.info("To={}", simpleMessage.getTo());
        log.info("From={}", simpleMessage.getFrom());
        log.info("Subject={}", simpleMessage.getSubject());
        log.info("Text={}", simpleMessage.getText());
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        log.info("메일 발송 완료!");
    }
}
