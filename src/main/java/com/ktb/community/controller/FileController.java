package com.ktb.community.controller;

import com.ktb.community.dto.file.FileUploadResponse;
import com.ktb.community.entity.File;
import com.ktb.community.entity.User;
import com.ktb.community.service.FileStorageService;
import com.ktb.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> upload(@RequestPart("file") MultipartFile file,
                                                     @RequestAttribute Long userId) {
        User user = userService.getByIdOrThrow(userId);
        if (user.isDeleted()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        File stored = fileStorageService.upload(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(FileUploadResponse.from(stored));
    }
}
