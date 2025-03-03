// EmailService.java

package com.unchk.AGRT_Backend.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.unchk.AGRT_Backend.enums.ApplicationStatus;

@Service
@RequiredArgsConstructor
public class EmailService {

        private final JavaMailSender emailSender;

        public void sendApplicationConfirmationEmail(String to, String candidateName, String announcementTitle) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject("Confirmation de votre candidature");

                String emailContent = String.format(
                                "Bonjour %s,\n\n" +
                                                "Nous confirmons la bonne réception de votre candidature pour le poste : %s.\n\n"
                                                +
                                                "Votre dossier est maintenant en cours d'examen. Nous vous contacterons dès que possible "
                                                +
                                                "pour vous tenir informé de l'avancement de votre candidature.\n\n" +
                                                "Cordialement,\n" +
                                                "L'équipe de recrutement",
                                candidateName,
                                announcementTitle);

                message.setText(emailContent);
                emailSender.send(message);
        }

        public void sendApplicationUnderReviewEmail(String to, String candidateName, String announcementTitle) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject("Mise à jour de votre candidature - En cours d'examen");

                String emailContent = String.format(
                                "Bonjour %s,\n\n" +
                                                "Nous vous informons que votre candidature pour le poste : %s est actuellement en cours d'examen.\n\n"
                                                +
                                                "Notre équipe analyse attentivement votre dossier. Nous vous tiendrons informé(e) de la suite "
                                                +
                                                "du processus dans les meilleurs délais.\n\n" +
                                                "Cordialement,\n" +
                                                "L'équipe de recrutement",
                                candidateName,
                                announcementTitle);

                message.setText(emailContent);
                emailSender.send(message);
        }

        public void sendApplicationAcceptedEmail(String to, String candidateName, String announcementTitle) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject("Félicitations - Candidature acceptée");

                String emailContent = String.format(
                                "Bonjour %s,\n\n" +
                                                "Nous avons le plaisir de vous informer que votre candidature pour le poste : %s a été retenue.\n\n"
                                                +
                                                "Nous vous contacterons très prochainement pour discuter des prochaines étapes et organiser "
                                                +
                                                "une rencontre pour finaliser les modalités de votre recrutement.\n\n" +
                                                "Toutes nos félicitations !\n\n" +
                                                "Cordialement,\n" +
                                                "L'équipe de recrutement",
                                candidateName,
                                announcementTitle);

                message.setText(emailContent);
                emailSender.send(message);
        }

        public void sendApplicationRejectedEmail(String to, String candidateName, String announcementTitle,
                        String rejectionReason) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject("Réponse concernant votre candidature");

                String emailContent = String.format(
                                "Bonjour %s,\n\n" +
                                                "Nous vous remercions de l'intérêt que vous avez porté à notre établissement en postulant "
                                                +
                                                "pour le poste : %s.\n\n" +
                                                "Après un examen attentif de votre candidature, nous regrettons de vous informer que "
                                                +
                                                "nous ne pouvons pas donner une suite favorable à votre demande.\n\n" +
                                                "%s\n\n" +
                                                "Nous vous souhaitons beaucoup de succès dans vos futures recherches.\n\n"
                                                +
                                                "Cordialement,\n" +
                                                "L'équipe de recrutement",
                                candidateName,
                                announcementTitle,
                                rejectionReason != null && !rejectionReason.isEmpty() ? "Motif : " + rejectionReason
                                                : "Nous avons reçu de nombreuses candidatures et avons dû faire des choix difficiles.");

                message.setText(emailContent);
                emailSender.send(message);
        }

        public void sendStatusUpdateEmail(String to, String candidateName, String announcementTitle,
                        ApplicationStatus newStatus, String reason) {
                switch (newStatus) {
                        case PENDING:
                                sendApplicationConfirmationEmail(to, candidateName, announcementTitle);
                                break;
                        case UNDER_REVIEW:
                                sendApplicationUnderReviewEmail(to, candidateName, announcementTitle);
                                break;
                        case ACCEPTED:
                                sendApplicationAcceptedEmail(to, candidateName, announcementTitle);
                                break;
                        case REJECTED:
                                sendApplicationRejectedEmail(to, candidateName, announcementTitle, reason);
                                break;
                        case CANCELLED:
                                // Gérer le cas de l'annulation si nécessaire
                                sendApplicationRejectedEmail(to, candidateName, announcementTitle, reason);
                                break;
                        default:
                                throw new IllegalStateException("Statut de candidature non reconnu : " + newStatus);
                }
        }

        public void sendPasswordResetEmail(String to, String candidateName, String otpCode) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject("Réinitialisation de votre mot de passe");

                String emailContent = String.format(
                                "Bonjour %s,\n\n" +
                                                "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
                                                "Voici votre code de réinitialisation : %s\n\n" +
                                                "Ce code est valable pendant 24 heures. Si vous n'avez pas demandé cette réinitialisation, "
                                                +
                                                "veuillez ignorer cet email.\n\n" +
                                                "Cordialement,\n" +
                                                "L'équipe de recrutement",
                                candidateName,
                                otpCode);

                message.setText(emailContent);
                emailSender.send(message);
        }
}