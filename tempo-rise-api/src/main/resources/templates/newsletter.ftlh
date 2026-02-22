<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Monthly Newsletter</title>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #2196F3; color: white; padding: 10px; text-align: center; }
        .article { margin-bottom: 20px; border-bottom: 1px solid #eee; padding-bottom: 20px; }
        .button {
            background-color: #2196F3;
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
            <h1>This Month at Temporise</h1>
        </div>
        <p>Hello <strong>${subscriberName}</strong>,</p>
        <p>Here's what's new this month:</p>

        <div class="article">
            <h2>${newsletter.title1}</h2>
            <p>${newsletter.content1}</p>
            <a href="${newsletter.link1}" class="button">Read More</a>
        </div>

        <div class="article">
            <h2>${newsletter.title2}</h2>
            <p>${newsletter.content2}</p>
            <a href="${newsletter.link2}" class="button">Read More</a>
        </div>

        <div class="footer">
            <p>Don't want to receive these emails? <a href="${unsubscribeLink}">Unsubscribe</a>.</p>
            <p>&copy; 2024 Temporise. All rights reserved.</p>
        </div>
    </div>
</body>
</html>