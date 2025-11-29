package com.ktb.community.controller;

import com.ktb.community.dto.file.FileUploadResponse;
import com.ktb.community.entity.File;
import com.ktb.community.service.FileStorageService;
import com.ktb.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> upload(@RequestPart("file") MultipartFile file) {
//        ensureAuthenticated(principal);
//        User user = userService.getByEmailOrThrow(principal.getUsername());
//        if (user.isDeleted()) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }
        File stored = fileStorageService.upload(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(FileUploadResponse.from(stored));
    }

    private void ensureAuthenticated(UserDetails principal) {
        if (principal == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
    }
}
