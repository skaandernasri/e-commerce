<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Order Notification</title>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #4CAF50; color: white; padding: 10px; text-align: center; }
        .button { background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; }
        .footer { margin-top: 20px; font-size: 12px; text-align: center; color: #777; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Order ${isUpdate?string("Updated","Created")}</h1>
        </div>
        <p>Hello <strong>${userName}</strong>,</p>

        <p>Your order <strong>#${orderRef}</strong> has been ${isUpdate?string("updated","created")} successfully.</p>

        <p>Status: <strong>${status}</strong></p>
        <p>Total Amount: <strong>${totalAmount}</strong></p>
        <p>Order Date: <strong>${orderDate}</strong></p>

        <p>Thank you for shopping with us!</p>

        <div class="footer">
            <p>&copy; 2025 Temporise. All rights reserved.</p>
        </div>
    </div>
</body>
</html>
