package com.punch.shop.common.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlobStorageService {

    private final BlobServiceClient blobServiceClient;

    @Value("${azure.storage.container-name}")
    private String containerName;

    public String upload(MultipartFile file) {
        String blobName = UUID.randomUUID() + getExtension(file.getOriginalFilename());

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        try {
            blobClient.upload(file.getInputStream(), file.getSize(), true);
        } catch (IOException e) {
            throw new UncheckedIOException("이미지 업로드에 실패했습니다.", e);
        }

        return blobClient.getBlobUrl();
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
