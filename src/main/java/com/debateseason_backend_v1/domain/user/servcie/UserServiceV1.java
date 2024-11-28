package com.debateseason_backend_v1.domain.user.servcie;

import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.user.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceV1 {

    private final UserRepository userRepository;


    // 1. User 등록하기
    public ResponseEntity<?> save(UserDTO userDTO){

        Boolean isExist = userRepository.existsByUsername(userDTO.getUsername());

        if(isExist){
            log.error("There is already username + "+userDTO.getUsername());
            return ResponseEntity.ok("There is already username + "+userDTO.getUsername());
        }

        User user = User.builder()
                .username(userDTO.getUsername())
                .password(userDTO.getPassword())
                .community(userDTO.getCommunity())
                .role("ROLE_USER")
                .build()
                ;

        // User 중복성 검사가 필요할까? -> KaKao OAuth 로그인을 하는데, 중복으로 될리가 없을듯.
        userRepository.save(user);
        return ResponseEntity.ok(userDTO.getUsername()+"님 가입을 축하합니다!");
    }

    // 2.

}
