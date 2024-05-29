package com.DBAuthExample.AuthExample.Storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    private String location = "C:/Users/user/IdeaProjects/SpringSecurity-6-Authorization-and-Authentication/src/main/resources/files";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}