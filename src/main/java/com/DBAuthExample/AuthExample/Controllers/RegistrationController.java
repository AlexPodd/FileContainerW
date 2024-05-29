package com.DBAuthExample.AuthExample.Controllers;

import com.DBAuthExample.AuthExample.Entity.User;
import com.DBAuthExample.AuthExample.Certificate.GenerateCertificate;
import com.DBAuthExample.AuthExample.Services.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@Controller
public class RegistrationController {

    @Autowired
    private MyUserDetailsService userservice;
    @PostMapping("/registration")
    public String reg(@ModelAttribute("user") User user, @RequestParam("randomString") String randomString, Model model) {
        if(userservice.HaveUser(user.getName())){
            return "redirect:/registration?error=UserNameAlreadyExists";
        }
        String filename;
        try {
            filename = new GenerateCertificate().Generate(user.getName(),randomString);
        }
        catch (Exception e) {
            e.printStackTrace();
            return "registration";
        }

        // Далее ваш код обработки
        if (!userservice.saveUser(user)) {
            return "registration";
        } else {
            return "redirect:/downloadFile?fileName=" +filename;
        }
    }
   /* @GetMapping("/registration")
    public String regPage(Model model){
        model.addAttribute("user", new User());
        return "registration";
    }*/

    @GetMapping("/registration")
    public String regPage(Model model, @RequestParam(value = "error", required = false) String error){
        if ("UserNameAlreadyExists".equals(error)) {
            model.addAttribute("errorMessage", "Username already exists. Please choose another one.");
        }
        model.addAttribute("user", new User());
        return "registration";
    }


    @GetMapping("/downloadFile")
    public ResponseEntity<FileSystemResource> downloadFile(@RequestParam("fileName") String fileName) {
        File file = new File("src/main/resources/UserCert/"+fileName); // Укажите путь к папке с сертификатами

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file));
    }
}
