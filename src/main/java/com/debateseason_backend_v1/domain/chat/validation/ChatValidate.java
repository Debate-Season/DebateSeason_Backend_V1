package com.debateseason_backend_v1.domain.chat.validation;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.chat.presentation.dto.request.ChatMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatValidate {

    @Value("${chat.message.max-length}")
    private int maxLength;
    @Value("${chat.message.min-length}")
    private int minLength;


    public boolean validateMessageLength(ChatMessageRequest messageRequest){
        log.info("@@ Validating message length. maxLength: {}, minLength: {}", maxLength, minLength);  // 설정값 로그 추가

        String content = messageRequest.getContent();
        log.info("@@ Validating message length. content: {}", content);
        if (content == null) {
            throw new CustomException(
                    ErrorCode.VALUE_OUT_OF_RANGE,
                    "메시지 내용은 필수 입니다"
            );
        }
        int contentLength = content.length();
        log.info("@@ contentLength: {}", contentLength);

        if(contentLength > maxLength || contentLength < minLength || content.isBlank() ){
            throw new CustomException(
                    ErrorCode.VALUE_OUT_OF_RANGE,
                    String.format("메시지는 %d자 이상 %d자 이하여야 합니다", minLength, maxLength)
            );
        }

        return true;
    }

}
