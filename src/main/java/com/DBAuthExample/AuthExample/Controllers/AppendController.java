package com.DBAuthExample.AuthExample.Controllers;

import com.DBAuthExample.AuthExample.Config.Matrix;
import com.DBAuthExample.AuthExample.Entity.User;
import com.DBAuthExample.AuthExample.Services.FileSystemStorageService;
import com.DBAuthExample.AuthExample.Services.MyUserDetailsService;
import com.DBAuthExample.AuthExample.repository.FileRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;

@Controller
public class AppendController {

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private FileRepository fileReposiroty;

    @Autowired
    private Matrix matrix;

    @GetMapping("/append")
    public String appendPage(Model model, @RequestParam("filename") String filename){
        model.addAttribute("filename", filename);
        System.out.println(filename);
        return "apend";
    }
    @PostMapping("/append")
    public String append(Principal principal,@RequestParam("filename") String filename, @RequestParam("appendText") String appendText) throws UnsupportedEncodingException {
        String[] parts = filename.split("/");
         filename = URLDecoder.decode(parts[parts.length - 1], "UTF-8");
        if(!fileReposiroty.existsByName(filename)){
            return "redirect:/?error=noSuchFile";
        };
        User user = userDetailsService.UserByUsername(principal.getName());
        Byte AccessLvl = Byte.parseByte(fileReposiroty.findLvlByName(filename));
        if(!userDetailsService.HaveRuleAppend(user,AccessLvl)){
            return "redirect:/access-denied";
        }
        String[] parts2 = filename.split("\\.");

        String path = fileReposiroty.findPathByName(filename);

        switch(parts2[parts2.length-1]){
            case ("txt"):
                AddToTXT(path, appendText);
                break;
            case ("docx"):
                AddToDocx(path, appendText);
                break;
        }


        return "redirect:/";
    }
    public boolean AddToTXT(String filePath, String textToAdd){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.newLine(); // Добавить новую строку перед добавлением текста
            writer.write(textToAdd);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    public boolean AddToDocx(String filePath, String textToAdd) {
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument doc = new XWPFDocument(fis)) {
            XWPFParagraph paragraph = doc.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(textToAdd);

            try (FileOutputStream out = new FileOutputStream(filePath)) {
                doc.write(out);
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
