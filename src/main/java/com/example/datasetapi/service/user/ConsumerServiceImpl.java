package com.example.datasetapi.service.user;

import com.example.datasetapi.model.UserManager.ConsumerSubscription;
import com.example.datasetapi.model.UserManager.User;
import com.example.datasetapi.service.Dataset.DatasetService;
import jakarta.servlet.http.HttpServletRequest;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConsumerServiceImpl implements ConsumerService {
  @Autowired
  private UserService userService;
  @Autowired
  private DatasetService datasetService;
  @Autowired
  private TokenService tokenService;

    @Override
    public List<ConsumerSubscription> getConsumerSubscriptions(HttpServletRequest request) {
        User consumer = userService.findUserById(tokenService.getUserIdFromRequest(request));
        return datasetService.findConsumerSub(consumer);
    }
}
