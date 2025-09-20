package com.clinicalpsychology.app.service;

import com.clinicalpsychology.app.exception.ResourceAlreadyExistsException;
import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.model.ConnectMethods;
import com.clinicalpsychology.app.repository.ConnectMethodsRepository;
import com.clinicalpsychology.app.response.CommonResponse;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.clinicalpsychology.app.util.Constant.*;

@Service
public class ConnectMethodsService {

    private final ConnectMethodsRepository connectMethodsRepository;

    public ConnectMethodsService(ConnectMethodsRepository connectMethodsRepository){this.connectMethodsRepository=connectMethodsRepository;}

    public CommonResponse<List<ConnectMethods>> getAllConnectMethods() throws UnexpectedServerException {

        try {

            var connectMethods = connectMethodsRepository.findAll();

            if(connectMethods.isEmpty()){

                return CommonResponse.<List<ConnectMethods>>builder()
                        .message(NO_CONNECT_METHODS_AVAILABLE)
                        .status(STATUS_TRUE)
                        .data(connectMethods)
                        .statusCode(SUCCESS_CODE)
                        .build();
            }

            return CommonResponse.<List<ConnectMethods>>builder()
                    .message(LOADED_ALL_CONNECT_METHODS)
                    .status(STATUS_TRUE)
                    .data(connectMethods)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (Exception e) {

            throw new UnexpectedServerException(ERROR_FETCHING_CONNECT_METHODS + e.getMessage());
        }

    }

    public CommonResponse<ConnectMethods> saveConnectMethods(ConnectMethods connectMethods) throws UnexpectedServerException {

        if(connectMethodsRepository.existsByName(connectMethods.getName())){
            throw new ResourceAlreadyExistsException(CONNECT_METHOD_ALREADY_EXISTS);
        }

        try {
            ConnectMethods connectMethod = connectMethodsRepository.save(connectMethods);

            return CommonResponse.<ConnectMethods>builder()
                    .message(SUCCESSFULLY_ADDED)
                    .status(STATUS_TRUE)
                    .data(connectMethod)
                    .statusCode(SUCCESS_CODE)
                    .build();

        } catch (Exception e){
            throw new UnexpectedServerException(ERROR_ADDING_CONNECT_METHOD + e.getMessage());
        }

    }
}
