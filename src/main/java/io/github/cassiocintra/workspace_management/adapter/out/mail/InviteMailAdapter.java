package io.github.cassiocintra.workspace_management.adapter.out.mail;

import io.github.cassiocintra.workspace_management.application.port.out.EmailPort;
import io.github.cassiocintra.workspace_management.domain.invite.Invite;
import io.github.cassiocintra.workspace_management.domain.workspace.WorkspaceRole;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class InviteMailAdapter implements EmailPort {

    private final JavaMailSender mailSender;
    private final String template;

    @Value("${users.invite.frontend-url}")
    private String frontendUrl;

    @Value("${users.mail.from}")
    private String from;

    public InviteMailAdapter(JavaMailSender mailSender) throws IOException {
        this.mailSender = mailSender;
        this.template = new ClassPathResource("templates/email/invite.html")
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public void sendInvite(Invite invite, String workspaceName, String inviterName) {
        try {
            String html = template
                    .replace("{{workspaceName}}", workspaceName)
                    .replace("{{inviterName}}", inviterName != null ? inviterName : "Alguém")
                    .replace("{{inviterInitials}}", initials(inviterName))
                    .replace("{{roleLabel}}", roleLabel(invite.getRole()))
                    .replace("{{expiresIn}}", expiresIn(invite.getExpiresAt()))
                    .replace("{{acceptUrl}}", frontendUrl + "/invites/" + invite.getToken())
                    .replace("{{sdkGuideUrl}}", frontendUrl);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(invite.getEmail());
            helper.setSubject("Você foi convidado para o " + workspaceName);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send invite email to " + invite.getEmail(), e);
        }
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        return Arrays.stream(name.trim().split("\\s+"))
                .filter(w -> !w.isEmpty())
                .limit(2)
                .map(w -> String.valueOf(w.charAt(0)).toUpperCase())
                .collect(Collectors.joining());
    }

    private String roleLabel(WorkspaceRole role) {
        return switch (role) {
            case ADMIN -> "Admin";
            case EDITOR -> "Editor";
            case VIEWER -> "Visualizador";
        };
    }

    private String expiresIn(Instant expiresAt) {
        long hours = ChronoUnit.HOURS.between(Instant.now(), expiresAt);
        if (hours < 24) return hours + " hora" + (hours == 1 ? "" : "s");
        long days = hours / 24;
        return days + " dia" + (days == 1 ? "" : "s");
    }
}
