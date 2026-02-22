<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>New Order Created</title>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #4CAF50; color: white; padding: 10px; text-align: center; }
        .button { background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; }
        .footer { margin-top: 20px; font-size: 12px; text-align: center; color: #777; }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>New Order Created</h1>
        </div>

        <p>Hello Admin,</p>
        <p>A new order has been created by <strong>${userName}</strong>.</p>

        <p><strong>Order Details:</strong></p>
        <table>
            <tr><th>Order Ref</th><td>${orderRef}</td></tr>
            <tr><th>Date</th><td>${orderDate}</td></tr>
            <tr><th>Status</th><td>${status}</td></tr>
            <tr><th>Total Amount</th><td>${totalAmount}</td></tr>
        </table>

        <p>Please review the order in the admin dashboard.</p>

        <div class="footer">
            <p>&copy; 2025 Temporise. All rights reserved.</p>
        </div>
    </div>
</body>
</html>
