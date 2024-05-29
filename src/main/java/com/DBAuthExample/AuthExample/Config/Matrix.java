package com.DBAuthExample.AuthExample.Config;


import com.DBAuthExample.AuthExample.Entity.FileToUpload;
import com.DBAuthExample.AuthExample.Entity.User;
import com.DBAuthExample.AuthExample.handlers.WebSocketHandler;
import lombok.AllArgsConstructor;
import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Matrix {
    private Map<User, List<Vector>> matrix = new HashMap<>();


    @Autowired
    private WebSocketHandler handler;


    public void AddRule(User user, FileToUpload file ,String rule) {
        if(!matrix.containsKey(user)) {
            matrix.put(user, new ArrayList<>());
        }
        matrix.computeIfAbsent(user, k -> new ArrayList<>()).add(new Vector(file, rule));
    }
    public void DeleteRuleUpper(User user, byte lvl) {
        if(!matrix.containsKey(user)) {
            matrix.put(user, new ArrayList<>());
        }
        matrix.get(user).iterator().forEachRemaining(vector -> {
            if (vector.getFile().getLvl() > lvl) {
                handler.CloseSession(user, vector.getFile(), vector.getRule());
            }
        });
        matrix.get(user).removeIf(vector -> vector.getFile().getLvl() > lvl);
    }
    public void DeleteRuleDowner(User user, byte lvl) {
        if(!matrix.containsKey(user)) {
            matrix.put(user, new ArrayList<>());
        }
        matrix.get(user).iterator().forEachRemaining(vector -> {
            if (vector.getFile().getLvl() < lvl) {
                handler.CloseSession(user, vector.getFile(), vector.getRule());
            }
        });
        matrix.get(user).removeIf(vector -> vector.getFile().getLvl() < lvl);
    }
}

@AllArgsConstructor
@Data
class Vector{
     private FileToUpload file;
     private String Rule;

}