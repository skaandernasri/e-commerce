<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta charset="UTF-8">
  <title>Réponse à votre demande d'aide</title>
  <style>
    body {
      font-family: Arial, sans-serif;
      background-color: #f4f6f8;
      color: #333333;
      padding: 20px;
    }
    .container {
      background-color: #ffffff;
      padding: 30px;
      border-radius: 8px;
      box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
    }
    .footer {
      font-size: 12px;
      color: #777777;
      margin-top: 20px;
    }
  </style>
</head>
<body>
  <div class="container">
    <h2>Bonjour ${userFullName},</h2>

    <p>Nous avons bien reçu votre demande d'aide concernant :</p>
    <blockquote>
      <strong>Sujet :</strong> ${contactSubject}<br/>
      <strong>Message :</strong> ${contactMessage}
    </blockquote>

    <#if adminMessage?? && adminMessage?has_content>
          <div class="admin-message">
            <p><strong>Message de l'administrateur :</strong></p>
            <p>${adminMessage}</p>
          </div>
        </#if>

    <p>Si vous avez besoin de plus d’assistance, n’hésitez pas à nous contacter à nouveau.</p>

    <p>Bien cordialement,<br/>
    L'équipe support de Temporise</p>

    <div class="footer">
      Cet email vous a été envoyé automatiquement suite à votre demande d'aide sur notre plateforme.
    </div>
  </div>
</body>
</html>
