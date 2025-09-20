package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

import static com.clinicalpsychology.app.util.Constant.*;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

//    Not needed because we use @required args constructor
//    public S3Service(S3Client s3Client) {
//        this.s3Client = s3Client;
//    }

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;


    public CommonResponse<String> uploadFile(MultipartFile file, String folderName) throws UnexpectedServerException {
        try {

            String message = RESUME_UPLOADED_SUCCESSFULLY;
            if(folderName.equals("client-images") || folderName.equals("therapist-images")){
                message=IMAGE_UPLOADED_SUCCESSFULLY ;
            }
            // Clean and build the S3 object key with folder path
            String originalFileName = file.getOriginalFilename();
            String fileName = UUID.randomUUID() + "-" + (originalFileName != null ? originalFileName : "file");

            String key = folderName + "/" + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            String fileUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;

            return CommonResponse.<String>builder()
                    .data(fileUrl)
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .message(message)
                    .build();

        } catch (Exception e) {
            throw new UnexpectedServerException(ERROR_UPLOADING_FILE + e.getMessage());
        }
    }

}

