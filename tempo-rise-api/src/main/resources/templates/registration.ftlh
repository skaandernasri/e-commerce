<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Welcome to Temporise!</title>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #4CAF50; color: white; padding: 10px; text-align: center; }
        .button {
            background-color: #4CAF50;
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
            <h1>Welcome to Temporise!</h1>
        </div>
        <p>Hello <strong>${userName}</strong>,</p>
        <p>Thank you for registering with us. Your account has been successfully created.</p>

        <#if activationLink??>
            <p>Please click the button below to activate your account:</p>
            <p>
                <a href="${activationLink}" class="button">Activate Account</a>
            </p>
            <p><em>This link expires in 24 hours.</em></p>
        </#if>

        <p>If you did not register, please ignore this email.</p>
        <div class="footer">
            <p>&copy; 2025 Temporise. All rights reserved.</p>
        </div>
    </div>
</body>
</html>