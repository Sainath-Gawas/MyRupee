# 💸 MyRupee – Smart Offline Expense Tracker

**MyRupee** is a standalone desktop application developed to simplify personal finance management while ensuring complete data privacy. The system allows users to record expenses, track income, monitor debts, manage monthly budgets and visualize financial behavior without requiring an internet connection or cloud synchronization.
Built entirely using Core Java and SQLite, the application focuses on speed, simplicity and secure local data storage.

---

# 🚀 Core Features

## 📊 Interactive Dashboard

A centralized dashboard that displays:

- Total Income
- Total Expenses
- Pending Debts
- Budget Utilization
  The dashboard also includes graphical financial summaries and weekly spending trend visualization.

---

## 💰 Income & Expense Management

Users can:

- Add and categorize transactions
- Attach descriptions/notes
- Manage financial records efficiently
- Monitor daily spending habits

The system is designed to provide fast and structured transaction logging through a clean desktop interface.

---

## 🎯 Smart Budget Tracking

MyRupee includes a monthly budget management module that:

- Tracks spending against predefined limits
- Displays dynamic progress indicators
- Generates visual warnings when nearing budget thresholds

This helps users maintain better spending discipline and financial awareness.

---

## 🤝 Debt Management System

The application provides a dedicated debt tracking module to manage:

- Borrowed money
- Lent money
- Contact details
- Due amounts and records

The system is intended to simplify peer-to-peer financial tracking for students and working professionals.

---

## 📈 Financial Insights & Analytics

MyRupee offers graphical financial analysis using custom-rendered visualizations such as:

- Category-wise expense distribution
- Spending trend analysis
- Most spent category detection
- Average daily expense calculations

Charts and visual components are rendered using Java Graphics2D.

---

# 🛠️ Technology Stack

| Programming Language | Java |
| GUI Framework | Java Swing |
| Graphics Rendering | Java AWT Graphics2D |
| Database | SQLite |
| Database Connectivity | JDBC (SQLite JDBC Driver) |
| Architecture | DAO-Based Three-Tier Architecture |

---

# 🏗️ Architecture Overview

The application follows a structured three-layer architecture:

### 1. Presentation Layer

Handles all graphical user interface components using Java Swing.

### 2. Business Logic & DAO Layer

Processes application logic and manages database operations through DAO classes.

### 3. Data Persistence Layer

Stores all application data locally using SQLite databases.

This separation improves maintainability, scalability, and code organization.

---

# 🔒 Privacy & Offline Functionality

Unlike many modern finance applications, MyRupee:

- Does not require internet access
- Does not store data on cloud servers
- Does not display advertisements
- Stores all user data locally on the device

This ensures complete ownership and privacy of financial information.
