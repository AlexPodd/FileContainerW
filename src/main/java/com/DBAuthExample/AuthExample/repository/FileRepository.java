package com.DBAuthExample.AuthExample.repository;

import com.DBAuthExample.AuthExample.Entity.FileToUpload;
import com.DBAuthExample.AuthExample.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FileRepository extends JpaRepository<FileToUpload, String> {
    @Query("SELECT f.path FROM FileToUpload f WHERE f.name = :name")
    String findPathByName(String name);

    @Query("SELECT f.lvl FROM FileToUpload f WHERE f.name = :name")
    String findLvlByName(String name);

    FileToUpload findByName(String filename);

    boolean existsByName(String name);
}
