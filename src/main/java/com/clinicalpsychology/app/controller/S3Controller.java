package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.exception.InvalidFieldValueException;
import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping("/upload/{type}")
    public CommonResponse<String> uploadFileToFolder(@PathVariable("type") String type, @RequestParam("file") MultipartFile file) throws UnexpectedServerException {

        if (!List.of("therapist-images", "therapist-resumes", "client-images").contains(type)) {
            throw new InvalidFieldValueException("Invalid upload folder type.");
        }

        return s3Service.uploadFile(file, type);
    }

}

