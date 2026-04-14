package org.example.projektarendehantering.presentation.web;

import io.awspring.cloud.s3.S3Resource;
import lombok.RequiredArgsConstructor;
import org.example.projektarendehantering.application.service.DocumentService;
import org.example.projektarendehantering.common.Actor;
import org.example.projektarendehantering.infrastructure.persistence.DocumentEntity;
import org.example.projektarendehantering.infrastructure.security.SecurityActorAdapter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Controller
@RequestMapping("/ui/cases/{caseId}/documents")
@RequiredArgsConstructor
public class DocumentUiController {

    private final DocumentService documentService;
    private final SecurityActorAdapter securityActorAdapter;

    @PostMapping("/upload")
    public String uploadDocument(@PathVariable UUID caseId, @RequestParam("file") MultipartFile file) throws IOException {
        Actor actor = securityActorAdapter.currentUser();
        documentService.uploadDocument(actor, caseId, file);
        return "redirect:/ui/cases/" + caseId;
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable UUID caseId, @PathVariable UUID documentId) throws IOException {
        Actor actor = securityActorAdapter.currentUser();
        S3Resource s3Resource = documentService.downloadDocument(actor, documentId);
        DocumentEntity entity = documentService.getEntity(actor, documentId);

        String encodedFilename = URLEncoder.encode(entity.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.parseMediaType(entity.getContentType()))
                .contentLength(entity.getFileSize())
                .body(s3Resource);
    }

    @PostMapping("/{documentId}/delete")
    public String deleteDocument(@PathVariable UUID caseId, @PathVariable UUID documentId) {
        Actor actor = securityActorAdapter.currentUser();
        documentService.deleteDocument(actor, documentId);
        return "redirect:/ui/cases/" + caseId;
    }
}
