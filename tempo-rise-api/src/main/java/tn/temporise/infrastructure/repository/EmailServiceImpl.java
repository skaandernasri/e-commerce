package tn.temporise.infrastructure.repository;

import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.DateMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.EmailInterface;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.infrastructure.persistence.entity.UtilisateurEntity;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConfigurationProperties
@Slf4j
@Async("mailExecutor")
public class EmailServiceImpl implements EmailInterface {
  private final JavaMailSender emailSender;
  private final FreeMarkerConfigurer freemarkerConfigurer;
  private final UserJpaRepo userJpaRepo;
  private final ExceptionFactory exceptionFactory;
  private final AuthJpaRepo authJpaRepo;
  private final UserRepo userRepo;
  private final DateMapper dateMapper;

  @Value("${resetPassword.token.expiration}")
  private Duration passwordResetTokenexpiration;
  @Value("${validateAccount.expiration}")
  private Duration validateAccountExpiration;
  @Value("${validateAccount.url}")
  private String validateAccountUrl;
  @Value("${resetPassword.url}")
  private String resetPasswordUrl;

  @Override
  public void sendSimpleMessage(String to, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    // message.setFrom("noreply@temporise.tn");
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    emailSender.send(message);
  }

  @Override
  public void sendHtmlMessage(String to, String subject, String templateName, Object model)
      throws MessagingException, IOException, TemplateException {
    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    Map<String, Object> templateData = new HashMap<>();

    String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(
        freemarkerConfigurer.getConfiguration().getTemplate(templateName), templateData);

    helper.setFrom("noreply@temporise.tn");
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlContent, true);

    emailSender.send(message);
  }

  @Override
  public void sendMessageWithAttachment(String to, String subject, String text, String filePath)
      throws MessagingException {
    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);

    helper.setFrom("noreply@temporise.tn");
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(text);

    FileSystemResource file = new FileSystemResource(new File(filePath));
    helper.addAttachment(file.getFilename(), file);

    emailSender.send(message);
  }

  @Override
  public void sendEmailWithAttachment(EmailType type, String to, String subject, String text,
      String filename) throws MessagingException, IOException, TemplateException {
    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    Map<String, Object> templateData = new HashMap<>();
    templateData.put("userName", to);
    // helper.setFrom("noreply@temporise.tn");
    helper.setTo(to);
    helper.setSubject(subject != null ? subject : getDefaultSubject(type));
    if (type == EmailType.REGISTRATION || type == EmailType.PASSWORD_RESET
        || type == EmailType.NEWSLETTER) {
      // Use FreeMarker template for HTML emails

      if (type == EmailType.REGISTRATION) {
        templateData.put("activationLink", "https://temposphere.tn/activate?token=abc123");
      }
      if (type == EmailType.PASSWORD_RESET) {
        templateData.put("resetLink", "https://temposphere.tn/reset-password?token=abc123");
      }
      if (type == EmailType.NEWSLETTER) {
        templateData.put("title1", "New Feature Release");
        templateData.put("content1", "We've launched a new dashboard to track your analytics!");
        templateData.put("link1", "https://temporise.tn/new-feature");
        templateData.put("title2", "Upcoming Events");
        templateData.put("content2", "Join our webinar on productivity hacks next week.");
        templateData.put("link2", "https://temporise.tn/events");
      }
      String templateName = getTemplateName(type);
      String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(
          freemarkerConfigurer.getConfiguration().getTemplate(templateName), templateData);
      helper.setText(htmlContent, true);
    } else {
      // Fallback to plain text
      helper.setText(text != null ? text : "");
    }

    // Add attachment if provided
    if (filename != null && !filename.isEmpty()) {
      FileSystemResource file = new FileSystemResource(new File(filename));
      helper.addAttachment(file.getFilename(), file);
    }

    emailSender.send(message);
  }

  @Override
  public void sendVerificationEmail(String to)
      throws MessagingException, IOException, TemplateException, ConflictException {
    try {
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      Map<String, Object> templateData = new HashMap<>();
      // helper.setFrom("noreply@temporise.tn");
      helper.setTo(to);
      // helper.setSubject("Thanks for registration!");
      helper.setSubject(getDefaultSubject(EmailType.REGISTRATION));
      // Use FreeMarker template for HTML emails
      String token = generateActivationToken();

      String activationLink = getLink(EmailType.REGISTRATION) + token;
      UtilisateurEntity user = userJpaRepo.findByEmail(to, UserType.NORMAL)
          .orElseThrow(() -> exceptionFactory.notFound("notfound.user"));
      if (authJpaRepo.findByUser_EmailAndProviderId(user.getEmail(), "0").isPresent())
        if (userJpaRepo.isUserActive(user.getId(), UserType.NORMAL))
          throw exceptionFactory.conflict("conflict.user_already_active");
      templateData.put("userName", to);
      templateData.put("activationLink", activationLink);
      user.setActivation_token(token);
      user.setActivationtokenexpiresat(generateExpirationDate(validateAccountExpiration));
      log.info("user to send email: {}", user);
      userJpaRepo.save(user);
      String templateName = getTemplateName(EmailType.REGISTRATION);
      log.info("template name: {}", templateName);
      String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(
          freemarkerConfigurer.getConfiguration().getTemplate(templateName), templateData);
      // log.info("htmlContent: {}", htmlContent);
      helper.setText(htmlContent, true);
      // Add attachment if provided

      emailSender.send(message);
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Override
  public void sendRestPasswordConfirmationEmail(String to)
      throws MessagingException, IOException, TemplateException, ConflictException {
    UtilisateurEntity user = userJpaRepo.findByEmail(to, UserType.NORMAL)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.user"));
    // authJpaRepo.findByUser_EmailAndProviderId(to, "0")
    // .orElseThrow(() -> exceptionFactory.notFound("notfound.authentication"));
    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    Map<String, Object> templateData = new HashMap<>();
    // helper.setFrom("noreply@temporise.tn");
    helper.setTo(to);
    helper.setSubject(getDefaultSubject(EmailType.PASSWORD_RESET));
    // Use FreeMarker template for HTML emails
    String token = generateActivationToken();

    String activationLink = getLink(EmailType.PASSWORD_RESET) + token;

    templateData.put("userName", to);
    templateData.put("resetLink", activationLink);
    user.setResetpasswordtoken(token);

    user.setResetpasswordexpiresat(generateExpirationDate(passwordResetTokenexpiration));
    log.info("user to send email: {}", user);
    userJpaRepo.save(user);
    String templateName = getTemplateName(EmailType.PASSWORD_RESET);
    log.info("template name: {}", templateName);
    String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(
        freemarkerConfigurer.getConfiguration().getTemplate(templateName), templateData);
    // log.info("htmlContent: {}", htmlContent);
    helper.setText(htmlContent, true);
    // Add attachment if provided

    emailSender.send(message);
  }

  @Override
  public void sendPaymentStatusEmail(WebhookResponsePayment payment)
      throws MessagingException, IOException, TemplateException {
    String email = payment.getPaymentDetails().getEmail();

    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setTo(email);
    helper.setSubject("Statut de votre paiement");

    Map<String, Object> templateData = new HashMap<>();
    templateData.put("name", payment.getPaymentDetails().getName());
    templateData.put("status", payment.getStatus().toString().toUpperCase());
    templateData.put("paymentRef", payment.getToken()); // or id / ref
    templateData.put("amount", payment.getConvertedAmount());
    templateData.put("currency", "TND"); // or dynamically from your system
    templateData.put("type", payment.getType());
    templateData.put("date", DateMapper.INSTANCE.mapOffsetDateTimeToString(payment.getCreatedAt()));

    String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(
        freemarkerConfigurer.getConfiguration().getTemplate("payment-status-notification.ftlh"),
        templateData);

    helper.setText(htmlContent, true);

    emailSender.send(message);
  }

  @Override
  public void sendRefundNotificationEmail(Paiement payment, String adminMessage)
      throws MessagingException, IOException, TemplateException {

    String email = payment.commande().user().email();
    log.info("email: {}", email);
    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setTo(email);
    helper.setSubject("Votre remboursement a été effectué");

    Map<String, Object> templateData = new HashMap<>();
    templateData.put("name", payment.nom());
    templateData.put("status", payment.status().toString().toUpperCase());
    templateData.put("paymentRef", payment.paiementRef());
    templateData.put("amount", payment.amount());
    templateData.put("currency", "TND"); // ou dynamique selon ta logique
    templateData.put("type", payment.type());
    templateData.put("date", DateMapper.INSTANCE.mapUtcDateTimeTimeToString(payment.date()));
    templateData.put("adminMessage", adminMessage);


    String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(
        freemarkerConfigurer.getConfiguration().getTemplate("refund-notif.ftlh"), templateData);

    helper.setText(htmlContent, true);
    emailSender.send(message);
  }

  @Override
  public void sendRefundRejectedNotificationEmail(Paiement payment, String adminMessage)
      throws MessagingException, IOException, TemplateException {

    String email = payment.commande().user().email();
    log.info("email: {}", email);

    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setTo(email);
    helper.setSubject("Votre remboursement a été refusé");

    Map<String, Object> templateData = new HashMap<>();
    templateData.put("name", payment.nom());
    templateData.put("status", payment.status().toString().toUpperCase());
    templateData.put("paymentRef", payment.paiementRef());
    templateData.put("amount", payment.amount());
    templateData.put("currency", "TND");
    templateData.put("type", payment.type());
    templateData.put("date", DateMapper.INSTANCE.mapDateToOffsetDateTime(payment.date()));
    templateData.put("adminMessage", adminMessage);

    String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(
        freemarkerConfigurer.getConfiguration().getTemplate("refund-rejected-notif.ftlh"),
        templateData);

    helper.setText(htmlContent, true);
    emailSender.send(message);
  }

  @Override
  public void sendPromoCodeEmail(String to, String subject, String message, CodePromo codePromo)
      throws MessagingException, IOException, TemplateException {
    UtilisateurModel user = userRepo.findByEmail(to);
    MimeMessage mimeMessage = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

    helper.setTo(to);
    helper.setSubject(subject);

    Map<String, Object> templateData = new HashMap<>();
    templateData.put("message", message);
    templateData.put("codePromo", codePromo.code());
    templateData.put("discount", codePromo.reduction()); // e.g., 15%
    templateData.put("expirationDate",
        DateMapper.INSTANCE.mapUtcDateTimeTimeToString(codePromo.dateExpiration())); // LocalDate
    // or
    // String
    templateData.put("userFullName", user.nom() + " " + user.prenom()); // assuming a relation

    String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(
        freemarkerConfigurer.getConfiguration().getTemplate("promo-code-email.ftlh"), templateData);

    helper.setText(htmlContent, true);
    emailSender.send(mimeMessage);
  }

  @Override
  public void sendOrderNotificationEmail(Commande order, boolean isUpdate)
      throws MessagingException, IOException, TemplateException {

    String email = order.email(); // get the user email
    String subject = isUpdate ? "Your order has been updated" : "Your order has been created";

    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setTo(email);
    helper.setSubject(subject);

    Map<String, Object> templateData = new HashMap<>();
    templateData.put("userName", order.nom() + " " + order.prenom());
    templateData.put("orderRef", order.id());
    templateData.put("orderDate", DateMapper.INSTANCE.mapUtcDateTimeTimeToString(order.date()));
    templateData.put("status", order.statut().toString());
    templateData.put("totalAmount", order.total()); // adjust if you have a total field
    templateData.put("isUpdate", isUpdate);

    // You can create a new FreeMarker template: order-notification.ftlh
    String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(
        freemarkerConfigurer.getConfiguration().getTemplate("order-notification.ftlh"),
        templateData);

    helper.setText(htmlContent, true);
    emailSender.send(message);
  }

  @Override
  public void sendOrderCreatedToAdmins(Commande order)
      throws MessagingException, IOException, TemplateException {

    // Fetch all admin emails from your user repository
    var adminUsers = userJpaRepo.findByRole(Role.ADMIN, UserType.NORMAL); // adjust role field
    if (adminUsers.isEmpty())
      return;

    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    // Convert list of admins to array of emails
    String[] adminEmails =
        adminUsers.stream().map(UtilisateurEntity::getEmail).toArray(String[]::new);

    helper.setTo(adminEmails);
    helper.setSubject("New Order Created - Order #" + order.id());

    Map<String, Object> templateData = new HashMap<>();
    templateData.put("orderRef", order.id());
    templateData.put("orderDate", DateMapper.INSTANCE.mapUtcDateTimeTimeToString(order.date()));
    templateData.put("status", order.statut().toString());
    templateData.put("totalAmount", order.total()+" TND");
    templateData.put("userName", order.nom() + " " + order.prenom());

    // FreeMarker template for admins
    String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(
        freemarkerConfigurer.getConfiguration().getTemplate("order-created-admin.ftlh"),
        templateData);

    helper.setText(htmlContent, true);
    emailSender.send(message);
  }



  @Override
  public void sendContactAideResponseEmail(String to, String userFullName, String contactSubject,
      String contactMessage, String adminResponse)
      throws MessagingException, IOException, TemplateException {

    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setTo(to);
    helper.setSubject("Réponse à votre demande d'aide");

    Map<String, Object> templateData = new HashMap<>();
    templateData.put("userFullName", userFullName);
    templateData.put("contactSubject", contactSubject);
    templateData.put("contactMessage", contactMessage);
    templateData.put("adminMessage", adminResponse);

    String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(
        freemarkerConfigurer.getConfiguration().getTemplate("contact-aide-response.ftlh"),
        templateData);

    helper.setText(htmlContent, true);
    emailSender.send(message);
  }



  private String getDefaultSubject(EmailType type) {
    // Fetch from configuration (you can externalize this)
    return switch (type) {
      case REGISTRATION -> "Thanks for registration!";
      case PASSWORD_RESET -> "Password reset";
      case NEWSLETTER -> "Newsletter";
      default -> "No Subject";
    };
  }

  private String getLink(EmailType type) {
    return switch (type) {
      // http://localhost:4200/reset-password?token=
      case REGISTRATION -> validateAccountUrl + "?token=";
      case PASSWORD_RESET -> resetPasswordUrl + "?token=";
      default -> throw exceptionFactory.conflict("conflict.invalid_link");
    };
  }

  private String getTemplateName(EmailType type) {
    // Fetch from configuration (you can externalize this)
    return switch (type) {
      case REGISTRATION -> "registration.ftlh";
      case PASSWORD_RESET -> "reset.ftlh";
      case NEWSLETTER -> "newsletter.ftlh";
      default -> throw new IllegalArgumentException("No template found for type: " + type);
    };
  }

  private String generateActivationToken() {
    // Generate a random unique token
    return UUID.randomUUID().toString();
  }

  private LocalDateTime generateExpirationDate(Duration expiration) {
    // Generate an expiration date (e.g., 1 hour from now)
    return LocalDateTime.now().plus(expiration);
  }
}
