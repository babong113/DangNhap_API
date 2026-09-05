package com.bteam.giasu.controller;

import com.bteam.giasu.entity.Schedule;
import com.bteam.giasu.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shedules")
public class ScheduleController {
    @Autowired
    private ScheduleService sheduleService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllShedules()
    {
        List<Schedule> list= sheduleService.getShedule();
        return ResponseEntity.ok(list);
    }
}
