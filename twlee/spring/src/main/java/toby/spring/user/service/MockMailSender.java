package toby.spring.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class MockMailSender implements MailSender {
    private List<String> targets = new ArrayList<>();

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        log.info("메일 발송");
        for (String to : Objects.requireNonNull(simpleMessage.getTo())) {
            log.info("To={}", to);
            targets.add(to);
        }
        log.info("From={}", simpleMessage.getFrom());
        log.info("Subject={}", simpleMessage.getSubject());
        log.info("Text={}", simpleMessage.getText());
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        log.info("메일 발송 완료!");
    }

    public List<String> getTargets() {
        return targets;
    }
}
