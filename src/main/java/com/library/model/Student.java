package com.library.model;

public class Student {
    private int id;
    private String name;
    private String grade;
    private String email;
    
    public Student() {}
    
    public Student(String name, String grade, String email) {
        this.name = name;
        this.grade = grade;
        this.email = email;
    }
    
    @Override
public String toString() {
    return this.name + " ";  
}
    
    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}