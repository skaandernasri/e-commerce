# E-Commerce Application

[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/skaandernasri/e-commerce)](https://github.com/skaandernasri/e-commerce/stargazers)
[![GitHub issues](https://img.shields.io/github/issues/skaandernasri/e-commerce)](https://github.com/skaandernasri/e-commerce/issues)

A modern **full-stack e-commerce application** designed to manage products, users, orders, and payments. Built with a responsive frontend and a robust backend for a seamless shopping experience.

---

## 🛒 Features

- **User Features**
  - Signup/Login with authentication
  - Browse products, categories, and promotions
  - Add to cart and place orders
  - Track order status
  - Receive email/notification updates

- **Admin Features**
  - Manage products, categories, and promotions
  - Monitor orders and sales
  - Manage users
  - Analytics dashboard (optional, via Power BI)

- **Other Features**
  - Product recommendation engine (LightFM)
  - Responsive UI for desktop and mobile
  - Secure payments with multiple gateways
  - Notification system

---

## 💻 Tech Stack

| Layer        | Technology                               |
|-------------|------------------------------------------|
| Backend     | Spring Boot, Java, MySQL                  |
| Frontend    | Angular, Tailwind CSS                     |
| DevOps      | Docker, GitHub Actions                    |
| Security    | JWT, OAuth2 (Google)                      |
| Others      | LightFM (recommendations), Power BI       |

---

## 📂 Project Structure

```text
e-commerce/
├─ tempo-rise-api/          # Spring Boot backend
├─ tempo-rise-web/          # Angular frontend
├─ .github/                 # CI/CD workflows
├─ Dockerfile & docker-compose.yml
├─ README.md
