package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.exceptionHandling.UnexpectedServerException;
import com.clinicalpsychology.app.model.ConnectMethods;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.ConnectMethodsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ConnectMethodsController {

    private final ConnectMethodsService connectMethodsService;

    public ConnectMethodsController(ConnectMethodsService connectMethodsService){this.connectMethodsService=connectMethodsService;}

    @GetMapping("/getAllConnectMethods")
    public CommonResponse<List<ConnectMethods>> getAllConnectMethods() throws UnexpectedServerException {
        return connectMethodsService.getAllConnectMethods();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/saveConnectMethod")
    public CommonResponse<ConnectMethods> saveConnectMethod(@RequestBody ConnectMethods connectMethods) throws UnexpectedServerException {
        return connectMethodsService.saveConnectMethods(connectMethods);
    }
}
