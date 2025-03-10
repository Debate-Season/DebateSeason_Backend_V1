package com.debateseason_backend_v1.domain.repository.entity;


import com.debateseason_backend_v1.domain.chat.model.request.ChatReactionRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "chat_reaction",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_id", "user_id", "reaction_type"}))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ChatReaction {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false)
    private ChatReactionRequest.ReactionType reactionType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
