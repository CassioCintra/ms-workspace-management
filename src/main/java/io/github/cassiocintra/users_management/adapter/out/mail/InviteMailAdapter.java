package io.github.cassiocintra.users_management.adapter.out.mail;

import io.github.cassiocintra.users_management.application.port.out.EmailPort;
import io.github.cassiocintra.users_management.domain.Invite;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class InviteMailAdapter implements EmailPort {

    private final JavaMailSender mailSender;

    @Value("${users.invite.base-url}")
    private String baseUrl;

    public InviteMailAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendInvite(Invite invite, String workspaceName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(invite.getEmail());
        message.setSubject("You have been invited to join " + workspaceName);
        message.setText("Click the link to accept your invitation:\n\n"
                + baseUrl + "/invite?token=" + invite.getToken()
                + "\n\nThis link expires in 72 hours.");
        mailSender.send(message);
    }
}
