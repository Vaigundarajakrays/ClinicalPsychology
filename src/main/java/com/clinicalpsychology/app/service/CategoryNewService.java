package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.Category;
import com.clinicalpsychology.app.repository.CategoryNewRepository;
import com.clinicalpsychology.app.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.clinicalpsychology.app.util.Constant.*;

@Service
@RequiredArgsConstructor
public class CategoryNewService {

    private final CategoryNewRepository categoryNewRepository;

    public CommonResponse<List<Category>> getAllCategories() throws UnexpectedServerException {

        try {

            List<Category> categories = categoryNewRepository.findAll();

            if (categories.isEmpty()) {
                return CommonResponse.<List<Category>>builder()
                        .status(STATUS_FALSE)
                        .statusCode(SUCCESS_CODE)
                        .data(categories)
                        .message("No categories found")
                        .build();
            }

            return CommonResponse.<List<Category>>builder()
                    .status(STATUS_TRUE)
                    .statusCode(SUCCESS_CODE)
                    .message("Loaded all categories")
                    .data(categories)
                    .build();

        } catch (Exception e){
            throw new UnexpectedServerException("Error while loading categories: " + e.getMessage());
        }
    }
}
