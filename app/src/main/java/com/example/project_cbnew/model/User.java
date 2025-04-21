package com.example.project_cbnew.model;

public class User {
    private String email;
    private String password;
    private String name;
    private String tel;
    private String role;
    private String userId;
    private String status; // ✅ เพิ่มสถานะ (ว่าง, ไม่ว่าง, กำลังรับงาน)

    private int completedJobs;
    private float averageRating;

    public User() {}  // ต้องมี constructor เปล่าสำหรับ Firebase

    public User(String email, String password, String name, String tel) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.tel = tel;
        this.role = "customer"; // set default role
        this.status = "ว่าง"; // กำหนด default สำหรับคนขับในอนาคต
    }

    // Getter และ Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }


    // Getter และ Setter
    public int getCompletedJobs() {
        return completedJobs;
    }

    public void setCompletedJobs(int completedJobs) {
        this.completedJobs = completedJobs;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }
}
