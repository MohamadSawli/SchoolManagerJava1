package com.example.schoolmanager;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "students")
public class Student {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String fullName;
    public String studentNumber;
    public String className;
    public String phone;
    public String guardianName;
    public String notes;

    public Student(String fullName, String studentNumber, String className,
                   String phone, String guardianName, String notes) {
        this.fullName = fullName;
        this.studentNumber = studentNumber;
        this.className = className;
        this.phone = phone;
        this.guardianName = guardianName;
        this.notes = notes;
    }
}
