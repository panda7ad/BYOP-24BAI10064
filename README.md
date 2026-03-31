# FFCS Attendance Tracker

## 📌 Introduction

FFCS Attendance Tracker is a Java Swing desktop application built to help students manage and track their attendance efficiently. It solves a common problem faced by students — calculating attendance percentage and predicting shortage risk.

The application is designed for flexible timetable systems (like FFCS), where each student has a different schedule.

---

## 🚀 What This Project Does

* Stores your courses and timetable
* Lets you mark daily attendance in seconds
* Saves data permanently using SQLite
* Helps you avoid attendance shortage by tracking records accurately

---

## ⚙️ Technologies Used

* **Java (Core + Swing)** → GUI
* **SQLite** → Database
* **JDBC** → Database connectivity

---

## 📂 Project Structure

```
AttendanceTracker/
├── src/com/attendancetracker/
│   ├── Main.java
│   ├── db/
│   │   ├── DBConnection.java
│   │   └── DatabaseManager.java
│   ├── model/
│   │   ├── Course.java
│   │   └── AttendanceRecord.java
│   ├── dao/
│   │   ├── CourseDAO.java
│   │   └── AttendanceDAO.java
│   ├── ui/
│   │   ├── MainFrame.java
│   │   ├── CoursesPanel.java
│   │   └── MarkAttendancePanel.java
├── data/
│   └── attendance.db
├── lib/
│   └── sqlite-jdbc.jar
└── README.md
```

---

## 🛠️ Setup Instructions (Step-by-Step)

### 1. Clone the Repository

```
git clone https://github.com/your-username/AttendanceTracker.git
cd AttendanceTracker
```

---

### 2. Add SQLite JDBC Driver

* Download SQLite JDBC `.jar` file
* Place it inside the `lib/` folder
* Add it to your project classpath (VS Code or IntelliJ)

---

### 3. Ensure Folder Exists

Create this folder manually if not present:

```
data/
```

This is where the database file will be stored.

---

### 4. Run the Project

* Open project in VS Code / IntelliJ
* Run:

```
Main.java
```

---

### ✅ Expected Output

* Application window opens
* Database (`attendance.db`) is created automatically
* You can start using the app

---

## 🧑‍💻 How to Use

### Step 1: Add Courses

* Go to **Courses tab**
* Enter:

  * Course name
  * Credit
  * Select days (Mon–Fri)
* Click **Add Course**

👉 You can also delete a course if entered incorrectly.

---

### Step 2: Mark Attendance

* Go to **Mark Attendance tab**
* Only today’s courses will appear
* For each course, choose:

  * Present
  * Absent
  * OD (On Duty)
  * Cancelled

---

### Step 3: Cancelled Class Logic

If you select **Cancelled**:

* If substitute/assignment given → counted as **Present**
* If not → class is removed from total count (handled later in logic)

---

### Step 4: Data Persistence

* All data is saved in SQLite
* Close and reopen app → data remains

---

## 🧠 Core Logic

* Attendance is stored per course per day
* OD is treated as Present
* Courses are shown based on timetable (day-wise filtering)

Future logic (planned):

* Attendance percentage calculation
* Safe-to-skip classes
* Required classes to reach 75%
* OD monthly limit (max 2)

---

## ⚠️ Current Limitations

* Dashboard (percentage view) not yet implemented
* Holidays and special Saturdays not added yet
* No edit option for courses (only delete + add)

---

## 🔮 Future Improvements

* Full dashboard with analytics
* Semester setup (start/end dates, exams)
* Holiday & special Saturday handling
* Export attendance report
* Better modern UI design

---

## 💡 Why This Project

This project is built from a real problem faced by students:

> “Am I going to get attendance shortage?”

Instead of manual counting, this system provides a simple and reliable way to track attendance daily.

---

## 🏁 Conclusion

FFCS Attendance Tracker demonstrates how real-world problems can be solved using programming. It combines Java, databases, and GUI design into a practical and useful application.

---

## 📎 Note

If you are running this project for the first time and face issues:

* Check if SQLite `.jar` is added correctly
* Ensure `data/` folder exists
* Make sure Java is installed (`java -version`)

---
