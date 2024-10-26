<%@ page import="com.example.Controlers.ChiliPeperApplication" %>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <meta charset="utf-8" />
    <title>Debug View</title>
    <link rel="icon" href="static/images/chiliLogo.jpg" type="image/icon">
    <link href="static/css/Chilli.CSS" rel="Stylesheet">
</head>
<body>
    <main>
            ${ChiliPeperApplication.getHTMLTable("Customer")}<hr>
            ${ChiliPeperApplication.getHTMLTable("Terracotta")}<hr>
            ${ChiliPeperApplication.getHTMLTable("plantType")}<hr>
            ${ChiliPeperApplication.getHTMLTable("cron")}<hr>
            ${ChiliPeperApplication.getHTMLTable("schedule")}<hr>
            ${ChiliPeperApplication.getHTMLTable("PLC")}<hr>
    </main>
</body>