package com.debateseason_backend_v1.domain.notification.application.service;

import com.debateseason_backend_v1.domain.chat.infrastructure.chat.ChatEntity;
import com.debateseason_backend_v1.domain.notification.infrastructure.FcmTokenEntity;
import com.debateseason_backend_v1.domain.repository.UserChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.entity.UserChatRoom;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceV1 {

    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;
    private final FcmTokenServiceV1 tokenService;
    private final NotificationPreferenceServiceV1 preferenceService;
    private final UserChatRoomRepository userChatRoomRepository;

    @Async("notificationExecutor")
    public void sendChatMessageNotification(ChatEntity chat) {
        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null) {
            log.debug("FirebaseMessaging not configured; skipping FCM send.");
            return;
        }

        Long roomId = chat.getChatRoomId().getId();
        Long senderUserId = chat.getUserId();

        List<UserChatRoom> participants = userChatRoomRepository.findByChatRoom(chat.getChatRoomId());

        List<Long> recipientUserIds = participants.stream()
                .map(ucr -> ucr.getUser().getId())
                .filter(uid -> senderUserId == null || !uid.equals(senderUserId))
                .filter(uid -> preferenceService.isUserEligibleForChatRoom(uid, roomId))
                .toList();

        if (recipientUserIds.isEmpty()) {
            log.debug("No eligible recipients for roomId={}", roomId);
            return;
        }

        Map<Long, List<FcmTokenEntity>> tokensByUser = recipientUserIds.stream()
                .collect(Collectors.toMap(uid -> uid, tokenService::findActiveTokens));

        List<String> tokens = tokensByUser.values().stream()
                .flatMap(List::stream)
                .map(FcmTokenEntity::getFcmToken)
                .distinct()
                .toList();

        if (tokens.isEmpty()) {
            log.debug("No FCM tokens for eligible recipients (roomId={})", roomId);
            return;
        }

        String senderName = chat.getSender() != null ? chat.getSender() : "";
        String topicTitle = chat.getChatRoomId() != null && chat.getChatRoomId().getTitle() != null
                ? chat.getChatRoomId().getTitle()
                : "";
        String content = chat.getContent() == null ? "" : chat.getContent();
        String collapseKey = "chat-" + roomId;
        String deeplink = "app://chat/" + roomId;
        java.time.Instant expiry = java.time.Instant.now().plusSeconds(3600);
        String sentAt = (chat.getTimeStamp() != null
                ? chat.getTimeStamp().atZone(java.time.ZoneId.systemDefault()).toInstant()
                : java.time.Instant.now()).toString();

        // Batch by 500 (FCM limit for multicast)
        for (int start = 0; start < tokens.size(); start += 500) {
            int end = Math.min(start + 500, tokens.size());
            List<String> batch = new ArrayList<>(tokens.subList(start, end));

            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(batch)
                    .setNotification(Notification.builder()
                            // iOS 표시 보장을 위한 placeholder — NSE에서 실제 내용 구성
                            .setTitle("placeholder")
                            .setBody("placeholder")
                            .build())
                    // Data payload per team format
                    .putData("type", "chat")
                    .putData("chatId", String.valueOf(roomId))
                    .putData("messageId", chat.getId() == null ? "" : String.valueOf(chat.getId()))
                    .putData("topicTitle", topicTitle)
                    .putData("nickname", senderName)
                    .putData("message", content)
                    .putData("previewLevel", "2")
                    .putData("deeplink", deeplink)
                    .putData("sentAt", sentAt)
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setTtl(3_600_000L)
                            .setCollapseKey(collapseKey)
                            .setNotification(AndroidNotification.builder()
                                    .setChannelId("chat")
                                    .setTag(collapseKey)
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .putHeader("apns-priority", "10")
                            .putHeader("apns-expiration", String.valueOf(expiry.getEpochSecond()))
                            .putHeader("apns-collapse-id", collapseKey)
                            .setAps(Aps.builder()
                                    .setMutableContent(true)
                                    .setThreadId(collapseKey)
                                    .setCategory("CHAT")
                                    .build())
                            .build())
                    .build();

            try {
                var response = firebaseMessaging.sendEachForMulticast(message);
                log.info("FCM multicast sent: success={}, failure={}, roomId={}",
                        response.getSuccessCount(), response.getFailureCount(), roomId);
            } catch (Exception e) {
                log.error("Failed to send FCM notifications for roomId={}: {}", roomId, e.getMessage(), e);
            }
        }
    }
}
