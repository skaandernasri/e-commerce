<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Reset Your Password</title>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #f44336; color: white; padding: 10px; text-align: center; }
        .button {
            background-color: #f44336;
            color: white;
            padding: 10px 20px;
            text-decoration: none;
            border-radius: 5px;
            display: inline-block;
        }
        .footer { margin-top: 20px; font-size: 12px; text-align: center; color: #777; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Password Reset Request</h1>
        </div>
        <p>Hello <strong>${userName}</strong>,</p>
        <p>We received a request to reset your password. Click the button below to proceed:</p>

        <p>
            <a href="${resetLink}" class="button">Reset Password</a>
        </p>
        <p><em>This link expires in 1 hour. If you didn't request this, please ignore this email.</em></p>

        <div class="footer">
            <p>&copy; 2024 Temporise. All rights reserved.</p>
        </div>
    </div>
</body>
</html>