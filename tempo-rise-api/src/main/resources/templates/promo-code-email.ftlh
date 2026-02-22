<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta charset="UTF-8">
  <title>Votre code promo Temporise</title>
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
    .promo-code {
      font-size: 24px;
      font-weight: bold;
      color: #2e7d32;
      background-color: #e8f5e9;
      padding: 10px;
      text-align: center;
      margin: 20px 0;
      border-radius: 6px;
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

    <p>${message}</p>

    <div class="promo-code">
      ${codePromo}
    </div>

    <p>Ce code vous donne droit à une réduction de <strong>${discount}%</strong>.</p>

    <#if expirationDate??>
      <p>Il est valable jusqu'au : <strong>${expirationDate}</strong>.</p>
    </#if>

    <p>Utilisez-le dès maintenant sur notre plateforme Temporise !</p>

    <p>Bien cordialement,<br/>
    L’équipe Temporise</p>

    <div class="footer">
      Cet email vous a été envoyé automatiquement. Merci de ne pas y répondre.
    </div>
  </div>
</body>
</html>
