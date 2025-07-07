package com.debateseason_backend_v1.domain.chat.application.service;

import com.debateseason_backend_v1.domain.chat.application.repository.ChatReactionRepository;
import com.debateseason_backend_v1.domain.chat.application.repository.ChatRepository;
import com.debateseason_backend_v1.domain.chat.application.repository.ReportRepository;
import com.debateseason_backend_v1.domain.chat.infrastructure.chat.ChatEntity;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.chat.validation.ChatValidate;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.fixtures.chat.ChatEntityFixture;
import com.debateseason_backend_v1.fixtures.chat.ChatMessageRequestFixture;
import com.debateseason_backend_v1.fixtures.chatroom.ChatRoomFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.security.Principal;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ChatServiceV1Test {

    @InjectMocks
    ChatServiceV1 chatService;
    @Mock
    ChatRoomServiceV1 chatRoomService;
    @Mock
    ChatValidate chatValidate;
    @Mock
    SimpMessageHeaderAccessor headerAccessor;
    @Mock
    ChatRepository chatRepository;
    @Mock
    ChatReactionRepository chatReactionRepository;
    @Mock
    ReportRepository reportRepository;

    @Nested
    @DisplayName("실시간 채팅 메시지 처리")
    class RealTimeChatMessageProcessing {
        @Nested
        @DisplayName("성공 케이스")
        class Success{
            @Test
            @DisplayName("유효한 메시지이면 메시지가 저장되고 응답이 생성된다")
            void should_save_message_and_create_response_when_valid_message(){
                //given
                Long givenRoomId = 1L;
                Long givenUserId = 2L;
                ChatRoom givenChatroom = ChatRoomFixture.create();
                ChatEntity givenChat = ChatEntityFixture.create();
                ChatMessageRequest givenMessage = ChatMessageRequestFixture.createChatMessageRequest();
                SimpMessageHeaderAccessor givenHeaderAccessor = mock(SimpMessageHeaderAccessor.class);
                Principal givenPrincipal = mock(Principal.class);

                //given(givenHeaderAccessor.getUser()).willReturn(givenPrincipal);
                //given(givenPrincipal.getName()).willReturn(givenUserId.toString());
                //given(chatRoomService.findChatRoomById(givenRoomId)).willReturn(givenChatroom);
                //given(chatRepository.save(any(ChatEntity.class))).willReturn(givenChat);


                //when
                chatService.processChatMessage(givenRoomId,givenMessage,givenHeaderAccessor);

                //then
                //메시지 유효성 검사 확인
                then(chatValidate).should().validateMessageLength(givenMessage);
                //채팅방 존재 여부 확인
                then(chatRoomService).should().findChatRoomById(givenRoomId);
                //인증 정보 에서 사용자 ID가져오기 확인
                then(headerAccessor).should().getUser();
                //메시지 저장 ₩확인
                //응답 생성 확인
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class Fail{

            }

    }

    @Nested
    @DisplayName("채팅 메시지 연속성 처리")
    class ChatMessagePersistence {
        @Nested
        @DisplayName("성공 케이스")
        class Success{

        }

        @Nested
        @DisplayName("실패 케이스")
        class Fail{

        }

    }

    @Nested
    @DisplayName("채팅 메시지 조회")
    class ChatMessageInquiry {
        @Nested
        @DisplayName("성공 케이스")
        class Success{

        }

        @Nested
        @DisplayName("실패 케이스")
        class Fail{

        }

    }

    @Nested
    @DisplayName("채팅방 입장 메시지 처리")
    class ChatRoomJoinMessageProcess {
        @Nested
        @DisplayName("성공 케이스")
        class Success{

        }

        @Nested
        @DisplayName("실패 케이스")
        class Fail{

        }

    }

}
