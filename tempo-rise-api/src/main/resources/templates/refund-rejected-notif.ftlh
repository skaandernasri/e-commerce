<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Remboursement refusé</title>
  <style>
    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; }
    .container { max-width: 600px; margin: 0 auto; padding: 20px; background: #fff; border-radius: 8px; }
    .header { background-color: #f0ad4e; color: white; padding: 15px; text-align: center; border-radius: 8px 8px 0 0; }
    .footer { margin-top: 30px; font-size: 12px; text-align: center; color: #999; }
    .details { margin: 20px 0; }
    .admin-message { background-color: #fff3cd; padding: 15px; border-left: 5px solid #ffc107; border-radius: 5px; }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <h1>Remboursement refusé</h1>
    </div>

    <p>Bonjour <strong>${name}</strong>,</p>

    <p>Nous vous informons que votre demande de remboursement n’a pas été approuvée.</p>

    <div class="details">
      <p><strong>Référence du paiement :</strong> ${paymentRef}</p>
      <p><strong>Montant :</strong> ${amount} ${currency}</p>
      <p><strong>Date de traitement :</strong> ${date}</p>
      <p><strong>Type :</strong> ${type}</p>
      <p><strong>Statut :</strong> ${status}</p>
    </div>

    <#if adminMessage?? && adminMessage?has_content>
      <div class="admin-message">
        <p><strong>Message de l'administrateur :</strong></p>
        <p>${adminMessage}</p>
      </div>
    </#if>

    <p>Pour toute question ou clarification, veuillez nous contacter via notre service client.</p>

    <div class="footer">
      <p>&copy; 2025 Temporise. Tous droits réservés.</p>
    </div>
  </div>
</body>
</html>
