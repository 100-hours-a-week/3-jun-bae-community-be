package com.ktb.community.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.ktb.community.entity.File;
import com.ktb.community.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileStorageService {

    private final FileRepository fileRepository;
    private final AmazonS3Client S3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Transactional
    public File upload(MultipartFile multipartFile) {


        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File payload is empty");
        }
        String originalFilename = multipartFile.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            originalFilename = "anonymous";
        }

        String storageKey = "uploads/" + UUID.randomUUID();
        String fileUrl = "https://" + bucketName + ".s3.amazonaws.com/" + storageKey;
        int fileSize = (int) multipartFile.getSize();
        try {
            //multipartFile.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());
            S3Client.putObject(bucketName, storageKey, multipartFile.getInputStream(), getObjectMetadata(multipartFile));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read file content", ex);
        }

        File file = File.pending(originalFilename, storageKey, fileUrl, fileSize);
        return fileRepository.save(file);
    }

    @Transactional
    public void delete(String storageKey) {
        if (!StringUtils.hasText(storageKey)) {
            return;
        }
        try {
            S3Client.deleteObject(bucketName, storageKey);
        } catch (SdkClientException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file", ex);
        }
    }

    private ObjectMetadata getObjectMetadata(MultipartFile file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());
        return objectMetadata;
    }

    public File getOrThrow(Long fileId) {
        return fileRepository.findByIdAndDeletedAtIsNull(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
    }
}
