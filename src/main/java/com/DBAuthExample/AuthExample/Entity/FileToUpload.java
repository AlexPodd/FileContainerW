package com.DBAuthExample.AuthExample.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "files")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileToUpload {
    @Id
    @Column(unique = true)
    private String name;

    private String path;

    private byte lvl;

    private String filetype;
}
