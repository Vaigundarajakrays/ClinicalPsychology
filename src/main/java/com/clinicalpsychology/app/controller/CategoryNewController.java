package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.Category;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.CategoryNewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryNewController {

    private final CategoryNewService categoryNewService;

    @GetMapping
    public CommonResponse<List<Category>> getAllCategories() throws UnexpectedServerException {
        return categoryNewService.getAllCategories();
    }

    //when adding new api make sure to add /api/categories/update/delete
    //because get all categories is not needed authorization
    // /api/categories is in security config
}
