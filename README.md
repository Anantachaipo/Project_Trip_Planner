# Project_Trip_Planner

**An Application for Trip Planning and Personal Transport Vehicle Booking with Driver Management Features**

---

## 📖 Description

Project_Trip_Planner คือแอปพลิเคชันบน Android ที่ช่วยให้ผู้ใช้สามารถ:

- วางแผนและสร้างทริป (Create Trip)
- จองรถส่วนบุคคล (Book Trip)
- ตรวจสอบสถานะการจองของตนเอง (View My Bookings)
- ให้คะแนนและรีวิวคนขับหลังทริปเสร็จสิ้น (Rate Driver)

นอกจากนี้ยังมีส่วนของผู้จัดการ (Manager) สำหรับ:

- ตรวจสอบและอนุมัติ/ปฏิเสธคำขอจอง (Manage Booking)
- มอบหมายคนขับให้กับการจอง (Assign Driver)

และคนขับ (Driver) สามารถ:

- ดูรายการทริปที่ได้รับมอบหมาย (View Assigned Trips)
- ติดตามรีวิวและคะแนนของตนเอง (View Ratings)

แอปนี้ผสานการทำงานกับ **Firebase** (Authentication, Firestore) และ **Google Maps API** เพื่อรองรับการระบุสถานที่รับ-ส่งอย่างแม่นยำและอ้างอิงแผนที่แบบเรียลไทม์  

---

## ⚙️ Features

1. **Customer**  
   - สมัครสมาชิก / เข้าสู่ระบบ  
   - สร้างทริปพร้อมรายละเอียด (วันเดินทาง, จุดเริ่ม, จุดสิ้นสุด)  
   - เลือกประเภทรถ (VIP/Normal), เช่าพร้อมคนขับหรือไม่  
   - ดูแผนที่และระบุพิกัดรับ-ส่งด้วย Map Integration  
   - จองทริป และดูสถานะ (รอตรวจสอบ, รอชำระเงิน, รอคนขับ, เดินทางเสร็จสิ้น ฯลฯ)  
   - ให้คะแนนและเขียนรีวิวคนขับ  

2. **Manager**  
   - ดูและกรองคำขอจองทั้งหมด  
   - อนุมัติหรือปฏิเสธการจอง  
   - มอบหมายคนขับให้กับแต่ละคำขอ  
   - เพิ่ม/แก้ไขข้อมูลรถและคนขับในระบบ  
   - ดูคะแนนเฉลี่ยและรีวิวของคนขับ  

3. **Driver**  
   - ดูรายละเอียดทริปที่ถูกมอบหมาย (Route, วัน-เวลา)  
   - อัปเดตสถานะงาน (รับงาน, ลงทะเบียนเสร็จสิ้น)  
   - ดูคะแนนเฉลี่ยและความเห็นจากลูกค้า  

---

## 🚀 Tech Stack

- **Android** (Java, Android SDK, Material Components)  
- **Firebase**  
  - Authentication (Email/Password)  
  - Cloud Firestore (Data Storage)  
- **Google Maps SDK** (แผนที่และ Geocoding)  
- **Glide** (Image Loading)  
- **RecyclerView**, **MapView**, **CardView**, **ConstraintLayout**  

---

## 📦 Getting Started

### Prerequisites

- Android Studio (Arctic Fox หรือใหม่กว่า)  
- Java 8+  
- SDK Platform Android 11 (API 30) ขึ้นไป  
- บัญชี Firebase พร้อมเปิด Authentication & Firestore  
- Google Maps API Key  

### Installation

1. โคลน repo:
   ```bash
   git clone https://github.com/Anantachaipo/Project_Trip_Planner.git
   cd Project_Trip_Planner
