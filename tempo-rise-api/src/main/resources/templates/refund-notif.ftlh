<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Produit remboursé</title>
  <style>
    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; }
    .container { max-width: 600px; margin: 0 auto; padding: 20px; background: #fff; border-radius: 8px; }
    .header { background-color: #d9534f; color: white; padding: 15px; text-align: center; border-radius: 8px 8px 0 0; }
    .button {
      background-color: #d9534f;
      color: white;
      padding: 10px 20px;
      text-decoration: none;
      border-radius: 5px;
      display: inline-block;
    }
    .footer { margin-top: 30px; font-size: 12px; text-align: center; color: #999; }
    .details { margin: 20px 0; }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <h1>Produit remboursé</h1>
    </div>

    <p>Bonjour <strong>${name}</strong>,</p>

    <p>Nous vous informons que votre demande de remboursement a été traitée avec succès.</p>

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

    <p>Le montant a été remboursé selon la méthode que vous avez choisie.</p>

    <p>Si vous avez des questions, n'hésitez pas à nous contacter.</p>

    <div class="footer">
      <p>&copy; 2025 Temporise. Tous droits réservés.</p>
    </div>
  </div>
</body>
</html>
