package com.example.datasetapi.service.user;

import com.example.datasetapi.Mapper.DatasetMapper;
import com.example.datasetapi.dto.response.ConsumerSubResponseDTO;
import com.example.datasetapi.model.UserManager.User;
import com.example.datasetapi.service.Dataset.DatasetService;
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
  @Autowired
  private DatasetMapper datasetMapper;

    @Override
    public List<ConsumerSubResponseDTO> getConsumerSubscriptions(HttpServletRequest request) {
        User consumer = userService.findUserById(tokenService.getUserIdFromRequest(request));
        return datasetMapper.toConsumerSubDTO(datasetService.findConsumerSub(consumer));
    }
}
