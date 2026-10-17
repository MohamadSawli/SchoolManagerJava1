package com.example.schoolmanager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StudentDao {
    @Query("SELECT * FROM students ORDER BY fullName COLLATE NOCASE ASC")
    List<Student> getAll();

    @Query("SELECT * FROM students WHERE fullName LIKE '%' || :query || '%' " +
           "OR studentNumber LIKE '%' || :query || '%' " +
           "OR className LIKE '%' || :query || '%' ORDER BY fullName COLLATE NOCASE ASC")
    List<Student> search(String query);

    @Insert
    long insert(Student student);

    @Update
    int update(Student student);

    @Delete
    int delete(Student student);

    @Query("SELECT COUNT(*) FROM students")
    int count();
}
