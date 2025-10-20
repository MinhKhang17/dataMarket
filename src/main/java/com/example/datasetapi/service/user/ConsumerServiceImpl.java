package com.example.datasetapi.service.user;

import com.example.datasetapi.model.userManager.ConsumerSubscription;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.service.dataset.DatasetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
