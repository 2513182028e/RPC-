package com.example.rpc.Nacos;

import com.example.rpc.pojo.ServiceMeta;

import java.util.List;

public interface NacosService {



    void  register(ServiceMeta serviceMeta);

    List<ServiceMeta> discovery(String serviceName);


}
