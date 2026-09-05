package com.bteam.giasu.service;


import com.bteam.giasu.entity.Schedule;
import com.bteam.giasu.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {
    @Autowired
    private ScheduleRepository sheduleRepository;

    public List<Schedule> getShedule()
    {
        return sheduleRepository.findAll();
    }
}
