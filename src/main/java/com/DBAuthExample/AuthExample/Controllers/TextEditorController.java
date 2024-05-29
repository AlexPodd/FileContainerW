package com.DBAuthExample.AuthExample.Controllers;



import com.DBAuthExample.AuthExample.Config.Matrix;
import com.DBAuthExample.AuthExample.Entity.User;
import com.DBAuthExample.AuthExample.Services.FileSystemStorageService;
import com.DBAuthExample.AuthExample.Services.MyUserDetailsService;
import com.DBAuthExample.AuthExample.Storage.StorageService;
import com.DBAuthExample.AuthExample.repository.FileRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.formula.atp.Switch;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class TextEditorController {

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private FileRepository fileReposiroty;

    @Autowired
    private FileSystemStorageService storageService;

    @Autowired
    private Matrix matrix;

    @GetMapping("/editor")
    public String editFile(HttpServletRequest request, Model model, Principal principal) throws IOException {
        String url = request.getParameter("filename");
        String[] parts = url.split("/");
        String filename = URLDecoder.decode(parts[parts.length - 1], "UTF-8");
        if(!fileReposiroty.existsByName(filename)){
            return "redirect:/?error=noSuchFile";
        };

        User user = userDetailsService.UserByUsername(principal.getName());
        Byte AccessLvl = Byte.parseByte(fileReposiroty.findLvlByName(filename));
        if(!userDetailsService.HaveRuleEdit(user,AccessLvl)){
            return "redirect:/access-denied";
        }

        matrix.DeleteRuleUpper(user, AccessLvl);

        matrix.AddRule(user, fileReposiroty.findByName(filename), "write");

        userDetailsService.ChangeTempAccessLvl(user.getName(), AccessLvl);


        String path = fileReposiroty.findPathByName(filename);


        String[] parts2 = filename.split("\\.");
        String content;

        model.addAttribute("filepath", path);
        model.addAttribute("userID", user.getId());
        switch(parts2[parts2.length-1]){
            case ("txt"):
               content = new String(Files.readAllBytes(Paths.get(path)));
               model.addAttribute("fileContent", content);
               break;
            case ("docx"):

                content = docxToString(path);
                model.addAttribute("fileContent", content);
                break;
        }


        return "editor2.0";

    }

    @PostMapping("/save-file")
    public ResponseEntity<String> saveFile(@RequestBody JsonNode fileData) {
        try {

            String content = fileData.get("content").asText();
            String filePath = fileData.get("filePath").asText();

            String[] parts = filePath.split("/");
            String filename = URLDecoder.decode(parts[parts.length - 1], "UTF-8");




            storageService.update(filePath, content, filename);

            return ResponseEntity.ok("Файл успешно сохранен");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка при сохранении файла: " + e.getMessage());
        }
    }

  public String docxToString(String path) throws IOException {
      InputStream fis = new FileInputStream(path);
      XWPFDocument document = new XWPFDocument(fis);
      XWPFWordExtractor extractor = new XWPFWordExtractor(document);
      String fileContent = extractor.getText();
      extractor.close();
      fis.close();
      return fileContent;
  }






}
