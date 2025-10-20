package com.example.datasetapi.service.user;

import com.example.datasetapi.model.UserManager.ConsumerSubscription;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ConsumerService {
    List<ConsumerSubscription> getConsumerSubscriptions(HttpServletRequest request);
}
