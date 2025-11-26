package com.example.datasetapi.service.location;

import com.example.datasetapi.dto.response.CommuneDTO;
import com.example.datasetapi.exception.CustomException;
import com.example.datasetapi.exception.ErrorCode;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.location.Province;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.repository.CommuneRepository;
import com.example.datasetapi.repository.ProvinceRepository;
import com.example.datasetapi.service.user.TokenService;
import com.example.datasetapi.service.user.UserService;
import com.example.datasetapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImp implements LocationService {

    private final ProvinceRepository provinceRepository;
    private final CommuneRepository communeRepository;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;
    private final UserService userService;

    @Override
    public List<Province> getAllProvinces() {
        return provinceRepository.findAll();
    }

    @Override
    public List<CommuneDTO> getCommunesByProvinceId(String provinceId) {
        List<Commune> communes = communeRepository.findByProvince_IdProvince(provinceId);
        return communes.stream()
                .map(c -> new CommuneDTO(
                        c.getIdCommune(),
                        c.getName(),
                        c.getProvince().getIdProvince(),
                        c.getProvince().getName()
                ))
                .toList();
    }

    @Override
    public List<CommuneDTO> getAllCommunes() {
        String token = tokenService.resolveToken(request);
        if (token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.USER_NOT_FOUND);
        }
        User moderator =  userService.findUserById(userId);
        if(!moderator.getRole().getName().equalsIgnoreCase("MODERATOR")){
            throw new CustomException(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
        }

        List<Commune> communes = communeRepository.findAll();
        return communes.stream()
                .map(c -> new CommuneDTO(
                        c.getIdCommune(),
                        c.getName(),
                        c.getProvince().getIdProvince(),
                        c.getProvince().getName()
                ))
                .toList();
    }

}
