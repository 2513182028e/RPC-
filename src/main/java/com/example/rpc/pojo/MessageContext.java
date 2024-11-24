package com.example.rpc.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MessageContext implements Serializable {
    private  int requestId;

    private byte messageType;

    private byte codec;

    private byte compress;



    private Object data;


}
