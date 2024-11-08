package com.debateseason_backend_v1.domain.chat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class E_Message {

    private String to;
    private String from;
    private String message;

}
