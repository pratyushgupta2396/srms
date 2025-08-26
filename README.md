# 🎓 Student Result Management System (SRMS)

A **full-featured Student Result Management System** built with **Spring Boot**, **Thymeleaf**, **PostgreSQL**, and **Bootstrap**.  
This system allows **Admins** to manage students, subjects, and marks, while **Students** can view and download their results as **PDF**.  

---

## ✨ Features

### 🔑 Admin Panel
- 📊 **Dashboard** – Quick overview & navigation  
- 👨‍🎓 **Manage Students** – Add, edit, delete students with roll numbers  
- 📚 **Manage Subjects** – Add, edit, delete subjects  
- 📝 **Manage Marks** – Assign marks per subject (no duplicates)  
- 📖 **View Results** – Paginated results, search by name/email, expand for subject-wise marks  
- ✅ **Pass/Fail** – Auto calculated based on marks  

### 🎓 Student Panel
- 🏠 **Dashboard** – Personalized welcome message  
- 📄 **View Results** – Subject-wise marks, total, percentage, and final result  
- ⬇️ **Download Result as PDF** – With university header and student details  

### 🔒 Security
- 👤 **Role-Based Authentication** (Admin & Student login)  
- 🔑 **Spring Security** – Password encryption & access control  

---

## 🛠 Tech Stack

| Layer       | Technology |
|-------------|------------|
| **Backend** | Java 17, Spring Boot, Spring MVC, Spring Security |
| **Frontend**| Thymeleaf, HTML5, CSS3, Bootstrap 5 |
| **Database**| PostgreSQL |
| **PDF**     | OpenPDF / iText |
| **Build**   | Maven |

---

## 📂 Project Structure

src/
├── main/
│   ├── java/com/example/studentdb/
│   │   ├── controller/        # MVC Controllers (Admin & Student endpoints)
│   │   ├── entity/            # JPA Entities (Student, Subject, Mark, User)
│   │   ├── repository/        # Spring Data JPA Repositories
│   │   ├── dto/               # Data Transfer Objects (Result DTOs, etc.)
│   │   ├── service/           # Business Logic Layer (Student, Result, Mark services)
│   │   └── security/          # Spring Security Configuration (JWT/Session, Roles)
│   │
│   └── resources/
│       ├── templates/         # Thymeleaf HTML Templates (Admin, Student views)
│       ├── static/            # CSS, JS, Images, Bootstrap files
│       └── application.properties  # Spring Boot App Configuration
│
└── test/                      # Unit & Integration Tests

