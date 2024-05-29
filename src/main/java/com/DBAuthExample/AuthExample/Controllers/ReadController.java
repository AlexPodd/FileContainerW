package com.DBAuthExample.AuthExample.Controllers;

import com.DBAuthExample.AuthExample.Config.Matrix;
import com.DBAuthExample.AuthExample.Entity.User;
import com.DBAuthExample.AuthExample.Services.FileSystemStorageService;
import com.DBAuthExample.AuthExample.Services.MyUserDetailsService;
import com.DBAuthExample.AuthExample.repository.FileRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;

@Controller
public class ReadController {


    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private FileRepository fileReposiroty;

    @Autowired
    private FileSystemStorageService storageService;

    @Autowired
    private Matrix matrix;

    @GetMapping("/read")
    public String read(HttpServletRequest request, Model model, Principal principal) throws IOException {
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

        matrix.DeleteRuleDowner(user, AccessLvl);

        matrix.AddRule(user, fileReposiroty.findByName(filename), "read");

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


        return "read";
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
