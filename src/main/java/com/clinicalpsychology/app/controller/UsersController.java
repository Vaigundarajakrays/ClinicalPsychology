package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.exceptionHandling.ResourceNotFoundException;
import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.Users;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.UsersService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService){this.usersService=usersService;}

    @GetMapping("/getUserById/{id}")
    public CommonResponse<Users> getUserById(@PathVariable Long id) throws ResourceNotFoundException {
        return usersService.getUserById(id);
    }

//    @PutMapping("updateUser/{id}")
//    public CommonResponse<Users> updateUser(@PathVariable Long id ,@RequestBody Users updatedUser) throws ResourceNotFoundException, UnexpectedServerException {
//        return usersService.updateUser(id, updatedUser);
//    }

    @DeleteMapping("deleteUserById/{id}")
    public CommonResponse<Users> deleteUserById(@PathVariable Long id) throws ResourceNotFoundException, UnexpectedServerException {
        return usersService.deleteUserById(id);
    }
}
