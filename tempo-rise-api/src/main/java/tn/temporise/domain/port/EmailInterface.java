package tn.temporise.domain.port;

import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import tn.temporise.domain.model.*;

import java.io.IOException;

public interface EmailInterface {
  void sendSimpleMessage(String to, String subject, String text);

  void sendHtmlMessage(String to, String subject, String templateName, Object model)
      throws MessagingException, IOException, TemplateException;

  void sendMessageWithAttachment(String to, String subject, String text, String filePath)
      throws MessagingException;

  void sendEmailWithAttachment(EmailType type, String to, String subject, String text,
      String filename) throws MessagingException, IOException, TemplateException;

  void sendVerificationEmail(String to

  ) throws MessagingException, IOException, TemplateException;

  void sendRestPasswordConfirmationEmail(String to)
      throws MessagingException, IOException, TemplateException;

  void sendPaymentStatusEmail(WebhookResponsePayment payment)
      throws MessagingException, IOException, TemplateException;

  void sendRefundNotificationEmail(Paiement payment, String adminMessage)
      throws MessagingException, IOException, TemplateException;

  void sendContactAideResponseEmail(String to, String userFullName, String contactSubject,
      String contactMessage, String adminResponse)
      throws MessagingException, IOException, TemplateException;

  void sendRefundRejectedNotificationEmail(Paiement payment, String adminMessage)
      throws MessagingException, IOException, TemplateException;

  void sendPromoCodeEmail(String to, String subject, String message, CodePromo codePromo)
      throws MessagingException, IOException, TemplateException;

  void sendOrderNotificationEmail(Commande order, boolean isUpdate)
      throws MessagingException, IOException, TemplateException;

  void sendOrderCreatedToAdmins(Commande order)
      throws MessagingException, IOException, TemplateException;
}
