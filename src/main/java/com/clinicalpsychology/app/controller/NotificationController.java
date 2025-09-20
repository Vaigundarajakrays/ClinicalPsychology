package com.clinicalpsychology.app.controller;

import com.clinicalpsychology.app.exception.ResourceNotFoundException;
import com.clinicalpsychology.app.exception.UnexpectedServerException;
import com.clinicalpsychology.app.model.Notification;
import com.clinicalpsychology.app.response.CommonResponse;
import com.clinicalpsychology.app.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService){this.notificationService=notificationService;}

//    @PostMapping("/saveNotification")
//    public CommonResponse<Notification> saveNotification(@RequestBody NotificationDTO notificationDTO) throws UnexpectedServerException, ResourceNotFoundException {
//        return notificationService.saveNotification(notificationDTO);
//    }

    @GetMapping("/getAllNotificationsByTherapistId/{therapistId}")
    public CommonResponse<List<Notification>> getAllNotificationByTherapistId(@PathVariable Long therapistId) throws UnexpectedServerException {
        return notificationService.getAllNotificationByTherapistId(therapistId, false);
    }

    @GetMapping("/getAllNotificationsByUserId/{userId}")
    public CommonResponse<List<Notification>> getAllNotificationByUserId(@PathVariable Long userId) throws UnexpectedServerException {
        return notificationService.getAllNotificationByUserId(userId, false);
    }

    @GetMapping("/getNotificationById/{id}")
    public  CommonResponse<Notification> getNotificationById(@PathVariable Long id) throws ResourceNotFoundException {
        return notificationService.getNotificationById(id);
    }

    @GetMapping("/getAllNotificationsByIsRead")
    public CommonResponse<List<Notification>> getAllNotificationsByIsRead() throws UnexpectedServerException {
        return notificationService.getAllNotificationsByIsRead(false);
    }

    @PutMapping("/updateNotificationAsRead/{id}")
    public CommonResponse<Notification> updateNotificationAsRead(@PathVariable Long id) throws ResourceNotFoundException, UnexpectedServerException {
        return notificationService.updateNotificationsAsRead(id);
    }

}
