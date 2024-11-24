package com.example.rpc.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Response {
    private int requestId;

    private Boolean isSuccess;

    private Object context;


    private Class<?> contextType;


}
