package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.enumUtil.Role;
import com.clinicalpsychology.app.exceptionHandling.ResourceAlreadyExistsException;
import com.clinicalpsychology.app.exceptionHandling.ResourceNotFoundException;
import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.Users;
import com.clinicalpsychology.app.repository.UsersRepository;
import com.clinicalpsychology.app.response.CommonResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.clinicalpsychology.app.util.Constant.*;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public UsersService(UsersRepository usersRepository, PasswordEncoder passwordEncoder){
        this.usersRepository=usersRepository;
        this.passwordEncoder=passwordEncoder;
    }

//    public CommonResponse<Users> signUp(Users users) throws UnexpectedServerException {
//
//        if (users.getPhoneNumber() == null || users.getPhoneNumber().trim().isEmpty()) {
//            throw new InvalidFieldValueException("Phone number is required");
//        }
//
//
//        try {
//
//            boolean exists = usersRepository.existsByEmailIdOrPhoneNumber(users.getEmailId(), users.getPhoneNumber());
//
//            if (exists) {
//                throw new ResourceAlreadyExistsException("Email or phone number already exists");
//            }
//
//            String hashedPassword = passwordEncoder.encode(users.getPassword());
//            users.setPassword(hashedPassword);
//            users.setRole(Role.USER);
//
//            Users savedUser = usersRepository.save(users);
//
//            return CommonResponse.<Users>builder()
//                    .message(SUCCESSFULLY_ADDED)
//                    .status(STATUS_TRUE)
//                    .data(savedUser)
//                    .statusCode(SUCCESS_CODE)
//                    .build();
//
//
//        } catch (ResourceAlreadyExistsException e){
//            throw e;
//        } catch (Exception e){
//            throw new UnexpectedServerException(ERROR_DURING_SIGN_UP + e.getMessage());
//        }
//
//
//    }

    public CommonResponse<Users> adminCreate(Users users) throws UnexpectedServerException {

        boolean exists = usersRepository.existsByEmailId(users.getEmailId());

        if (exists) {
            throw new ResourceAlreadyExistsException(EMAIL_ALREADY_EXISTS);
        }

        try {

            String hashedPassword = passwordEncoder.encode(users.getPassword());
            users.setPassword(hashedPassword);
            users.setRole(Role.ADMIN);

            Users savedUser = usersRepository.save(users);

            return CommonResponse.<Users>builder()
                    .message(SUCCESSFULLY_ADDED)
                    .status(STATUS_TRUE)
                    .data(savedUser)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (Exception e){
            throw new UnexpectedServerException(ERROR_DURING_SIGN_UP + e.getMessage());
        }

    }

    public CommonResponse<Users> getUserById(Long id) throws ResourceNotFoundException {

        Users user = usersRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + id));

        return CommonResponse.<Users>builder()
                .message(LOADED_USER_DETAILS)
                .status(STATUS_TRUE)
                .data(user)
                .statusCode(SUCCESS_CODE)
                .build();

    }

    //WANNA CHECK IF EMAIL PHONE EXISTS
//    public CommonResponse<Users> updateUser(Long id, Users updatedUser) throws ResourceNotFoundException, UnexpectedServerException {
//
//        Users existingUser = usersRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + id));
//
//        try {
//
//            existingUser.setName(updatedUser.getName());
//            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
//            existingUser.setEmailId(updatedUser.getEmailId());
//            existingUser.setDescription(updatedUser.getDescription());
//            existingUser.setGoals(updatedUser.getGoals());
//
//            Users updated = usersRepository.save(existingUser);
//
//            return CommonResponse.<Users>builder()
//                    .message(USER_UPDATED)
//                    .status(STATUS_TRUE)
//                    .data(updated)
//                    .statusCode(SUCCESS_CODE)
//                    .build();
//
//        } catch (Exception e) {
//            throw new UnexpectedServerException(ERROR_UPDATING_USER + e.getMessage());
//        }
//
//    }

    public CommonResponse<Users> deleteUserById(Long id) throws ResourceNotFoundException, UnexpectedServerException {

        if(!usersRepository.existsById(id)){
            throw new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + id);
        }

        try {

            usersRepository.deleteById(id);

            return CommonResponse.<Users>builder()
                    .message(USER_DELETED)
                    .statusCode(SUCCESS_CODE)
                    .status(STATUS_TRUE)
                    .build();

        } catch (Exception e){
            throw new UnexpectedServerException(ERROR_DELETING_USER + e.getMessage());
        }
    }
}
