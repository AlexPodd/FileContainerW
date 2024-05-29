package com.DBAuthExample.AuthExample.Controllers;


import com.DBAuthExample.AuthExample.Entity.User;
import com.DBAuthExample.AuthExample.Services.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("admin/")
public class AdminController {

    @Autowired
    private MyUserDetailsService userDetailsService;

    @GetMapping("/panel")
    public String panel(Model model){
        List<User> users= userDetailsService.allUsers();
        model.addAttribute("users",users);
        return "panel";
    }
    @PostMapping("/ChangeAccessLevel")
    public String ChangeLvl(@RequestParam("username") String username,
                            @RequestParam("newAccessLevel") byte newAccessLevel, Model model){
        userDetailsService.ChangeAccessLvl(username, newAccessLevel);


        return panel(model);
    }

}
