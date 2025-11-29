package com.ktb.community.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.ktb.community.entity.File;
import com.ktb.community.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private FileRepository fileRepository;
    @Mock
    private AmazonS3Client s3Client;

    @InjectMocks
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileStorageService, "bucketName", "test-bucket");
    }

    @Test
    void upload_withValidFile_savesMetadataAndCommits() throws IOException {
        MultipartFile multipartFile = org.mockito.Mockito.mock(MultipartFile.class);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("image.png");
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getSize()).thenReturn(128L);
        byte[] payload = {1, 2, 3};
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(payload));
        when(fileRepository.save(any(File.class))).thenAnswer(invocation -> {
            File file = invocation.getArgument(0);
            ReflectionTestUtils.setField(file, "id", 99L);
            return file;
        });

        File saved = fileStorageService.upload(multipartFile);

        assertThat(saved.getOriginalFileName()).isEqualTo("image.png");
        assertThat(saved.getFileUrl()).contains("test-bucket");
        assertThat(saved.isCommitted()).isFalse();
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
        ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);
        verify(s3Client).putObject(eq("test-bucket"), keyCaptor.capture(), streamCaptor.capture(), metadataCaptor.capture());
        assertThat(keyCaptor.getValue()).startsWith("uploads/");
        assertThat(metadataCaptor.getValue().getContentType()).isEqualTo("image/png");
        verify(fileRepository).save(saved);
    }

    @Test
    void upload_whenFileEmpty_throwsBadRequest() {
        MultipartFile multipartFile = org.mockito.Mockito.mock(MultipartFile.class);
        when(multipartFile.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> fileStorageService.upload(multipartFile))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verify(s3Client, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        verify(fileRepository, never()).save(any());
    }

    @Test
    void upload_whenInputStreamFails_throwsInternalServerError() throws IOException {
        MultipartFile multipartFile = org.mockito.Mockito.mock(MultipartFile.class);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("file.txt");
        when(multipartFile.getInputStream()).thenThrow(new IOException("broken"));

        assertThatThrownBy(() -> fileStorageService.upload(multipartFile))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(fileRepository, never()).save(any());
    }

    @Test
    void getOrThrow_whenExists_returnsFile() {
        File file = File.pending("origin", "key", "url", 1000);
        ReflectionTestUtils.setField(file, "id", 1L);
        when(fileRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(file));

        File result = fileStorageService.getOrThrow(1L);

        assertThat(result).isSameAs(file);
    }

    @Test
    void getOrThrow_whenMissing_throwsNotFound() {
        when(fileRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fileStorageService.getOrThrow(1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
