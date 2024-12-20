package com.debateseason_backend_v1.domain.issue.service;

import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.issue.dto.IssueDAO;
import com.debateseason_backend_v1.domain.issue.dto.IssueDTO;
import com.debateseason_backend_v1.domain.repository.entity.Issue;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.IssueRepository;
import com.debateseason_backend_v1.domain.repository.UserIssueRepository;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.repository.entity.UserIssue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@AllArgsConstructor
@Service
public class IssueServiceV1 {

    private final IssueRepository issueRepository;
    private final UserIssueRepository userIssueRepository;
    private final ChatRoomRepository chatRoomRepository;

    private final ObjectMapper objectMapper;

    // 1. save 이슈방
    public ResponseEntity<?> save(IssueDTO issueDTO){

        Issue issue = Issue.builder()
                .title(issueDTO.getTitle())
                .build()
                ;
        issueRepository.save(issue);

        return ResponseEntity.ok(issueDTO.getTitle()+"이 등록되었습니다.");

    }
    
    //2. fetch 단건 이슈방
    @Transactional
    public ResponseEntity<?> fetch(Long issueId){

        // 1. 이슈방 불러오기
        Issue issue = issueRepository.findById(issueId).orElseThrow(
                    ()-> new RuntimeException("There is no "+issueId)
        );

        // 1-1. User 불러오기. 참여 커뮤니티를 보여주기 위함임(내림차순으로)
        List<UserIssue> userIssueList = userIssueRepository.findByIssue(issue);

        //
        List<User> userList = new ArrayList<>();
        for(UserIssue e:userIssueList){
            userList.add(e.getUser());
        }

        // Map
        Map<String,Integer> map = new HashMap<>();
        for (User u:userList){

            String key = u.getCommunity();

            // 1. community에 없는 경우 -> 새로 추가를 한다.
            if(!map.containsKey(key)){
                map.put(key,1);
            }
            // 2. community에 있는 경우 -> value를 찾은 후 +1
            else{
                int count = map.get(key);
                map.put(key,count+1);
            }

        }

        // 1-3. Map을 커뮤니티 count 내림차순으로 정렬

        List<String> keySet = new ArrayList<>(map.keySet());
        keySet.sort((o1, o2) -> map.get(o2).compareTo(map.get(o1)));

        // LinkedHashMap을 써서 순서를 보장한다.
        Map<String,Integer> sortedMap = new LinkedHashMap<>();

        for (String key : keySet) {
            sortedMap.put(key,map.get(key));
        }
        
        // 1-4. 채팅방도 같이 넘기자. null이어도 가능!
        List<ChatRoom> chatRoomList = chatRoomRepository.findByIssue(issue);

        Map<Integer,ChatRoom> chatRoomMap = new LinkedHashMap<>();

        for(int i=1; i<chatRoomList.size()+1 ;i++){
            chatRoomMap.put(i,chatRoomList.get(i-1));
        }

        // 1-5 IssueDAO만들기
        IssueDAO issueDAO = IssueDAO.builder()
                .issue(issue)
                .map(sortedMap)
                .chatRoomMap(chatRoomMap)
                .build()
                ;
        
        
        // json으로 반환을 한다.
        String json;
        try {
            json = objectMapper.writeValueAsString(issueDAO);
        } catch (JsonProcessingException e) {
            log.error("IssueServiceV1.fetch : "+e.getMessage());
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok(json);

    }

    //3. fetch 전체 이슈방(인덱스 페이지용) <- 나중에 수정할듯
    public ResponseEntity<?> fetchAll(){
        List<Issue> issueList = issueRepository.findAll();

        // Gson,JSONArray이 없어서 Map으로 반환을 한다.
        Map<Integer,Issue> issueMap = new LinkedHashMap<>();

        for(int i=0; i<issueList.size(); i++){
            issueMap.put(i,issueList.get(i));
        }

        String json;

        try {
            json = objectMapper.writeValueAsString(issueMap);
        } catch (JsonProcessingException e) {
            log.error("IssueServiceV1.fetchAll : "+e.getMessage());
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok(json);


    }



}
