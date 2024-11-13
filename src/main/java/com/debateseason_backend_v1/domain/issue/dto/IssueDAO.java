package com.debateseason_backend_v1.domain.issue.dto;

import com.debateseason_backend_v1.domain.chatroom.model.ChatRoom;
import com.debateseason_backend_v1.domain.issue.model.Issue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class IssueDAO {

    public IssueDAO(){

    }
    private Issue issue;
    private Map<String,Integer> map;
    private Map<Integer,ChatRoom> chatRoomMap;
}
