package com.team.model; // 確保這裡的 package 名稱與您的資料夾一致

public class MemberRegisterDTO {
    // 這裡的變數名稱，必須跟 login.html 裡面 input 的 name 屬性完全一模一樣！
    private String name;
    private String email;
    private String password;
    private String phone;

    // --- 自動產生 Getter & Setter (或者您可以按右鍵 -> Source -> Generate Getters and Setters) ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // 為了方便印出來看，我們加上 toString 方法
    @Override
    public String toString() {
        return "註冊資料 [姓名=" + name + ", 信箱=" + email + ", 電話=" + phone + "]";
    }
}