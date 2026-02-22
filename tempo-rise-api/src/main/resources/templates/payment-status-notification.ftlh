<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Statut de Paiement</title>
  <style>
    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
    .header { background-color: #4caf50; color: white; padding: 10px; text-align: center; }
    .footer { margin-top: 20px; font-size: 12px; text-align: center; color: #777; }
    .status-box {
      background-color: #f0f0f0;
      border-left: 5px solid #4caf50;
      padding: 10px;
      margin: 10px 0;
    }
    .highlight { font-weight: bold; color: #4caf50; }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <h1>Confirmation de Paiement</h1>
    </div>

    <p>Bonjour <strong>${name}</strong>,</p>

    <p>Nous vous informons que votre paiement a été <span class="highlight">${status}</span>.</p>

    <div class="status-box">
      <p><strong>Référence:</strong> ${paymentRef}</p>
      <p><strong>Montant:</strong> ${amount} ${currency}</p>
      <p><strong>Date:</strong> ${date}</p>
      <p><strong>Type de paiement:</strong> ${type}</p>
    </div>

    <p>Merci pour votre confiance.</p>

    <div class="footer">
      <p>&copy; 2024 Temporise. Tous droits réservés.</p>
    </div>
  </div>
</body>
</html>
