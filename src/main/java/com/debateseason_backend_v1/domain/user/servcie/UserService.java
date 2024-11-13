package com.debateseason_backend_v1.domain.user.servcie;

import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.user.model.User;
import com.debateseason_backend_v1.domain.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import org.hibernate.query.sql.internal.ParameterRecognizerImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;




@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    // 1. User 등록하기
    public ResponseEntity<?> saveUser(UserDTO userDTO){
        User user = User.builder()
                .name(userDTO.getName())
                .community(userDTO.getCommunity())
                .build()
                ;

        // User 중복성 검사가 필요할까? -> KaKao OAuth 로그인을 하는데, 중복으로 될리가 없을듯.
        userRepository.save(user);
        return ResponseEntity.ok(userDTO.getName()+"님 가입을 축하합니다!");
    }

    // 2.

}
