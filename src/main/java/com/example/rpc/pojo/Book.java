package com.example.rpc.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Book  implements Serializable {

    public Integer id;

    public String name;


}
