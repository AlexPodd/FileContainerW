package com.DBAuthExample.AuthExample.Controllers;

import java.io.IOException;
import java.security.Principal;
import java.util.stream.Collectors;
import java.nio.file.Path;

import com.DBAuthExample.AuthExample.Entity.User;
import com.DBAuthExample.AuthExample.Services.MyUserDetailsService;
import com.DBAuthExample.AuthExample.Storage.StorageFileNotFoundException;
import com.DBAuthExample.AuthExample.Storage.StorageService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.nio.file.*;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    private MyUserDetailsService userDetailsService;
    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model, Principal principal) throws IOException {



        String username = principal.getName();
        User currentUser = userDetailsService.UserByUsername(username);
        model.addAttribute("accessLevel",currentUser.getMaxLvlAccess());

        model.addAttribute("files", storageService.loadAll().map(
                    path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                                "serveFile", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }



    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

      Resource file = storageService.loadAsResource(filename);

        if (file == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachmendt; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes, @RequestParam("LvlAccess") byte lvlAccess) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload.");
            return "redirect:/";
        }

        // Check file type
        String contentType = file.getContentType();
        if (!isAllowedFileType(contentType)) {
            redirectAttributes.addFlashAttribute("message", "Only .docx and .txt files are allowed.");
            return "redirect:/";
        }

        storageService.store(file, lvlAccess);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }
    private boolean isAllowedFileType(String contentType) {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType)
                || "text/plain".equals(contentType);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/access-denied")
    public String accessDenied( RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "У вас нет прав для доступа к этому ресурсу.");
        return "redirect:/";
    }
}
