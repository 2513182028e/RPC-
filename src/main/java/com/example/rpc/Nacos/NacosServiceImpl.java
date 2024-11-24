package com.example.rpc.Nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.example.rpc.pojo.ServiceMeta;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service("nacosService")
public class NacosServiceImpl implements NacosService{

    private static final String SERVER_ADDR="127.0.0.1:8848";
    private ConfigService configService=null;
    private NamingService namingService=null;
    private Properties properties=new Properties();

    public NacosServiceImpl()
    {
           try {

               properties.put(PropertyKeyConst.SERVER_ADDR,SERVER_ADDR);
               properties.put(PropertyKeyConst.USERNAME,"nacos");
               properties.put(PropertyKeyConst.PASSWORD,"nacos");

                ConfigService configService = NacosFactory.createConfigService(properties);
                namingService=NacosFactory.createNamingService(properties);
           } catch (NacosException e) {
               throw new RuntimeException(e);
           }
    }
    @Override
    public void register(ServiceMeta serviceMeta) {
            try {
            String serviceName = serviceMeta.getServiceName();
            String host = serviceMeta.getIP();
            int  port = serviceMeta.getPort();
            namingService.registerInstance(serviceName, host, port);
        } catch (NacosException e) {
            System.out.println("注册服务时有错误发生:"+e.getMessage());
        }
    }

    @Override
    public List<ServiceMeta> discovery(String serviceName) {
        try {
            List<Instance> allInstances = namingService.getAllInstances(serviceName);
        List<ServiceMeta> lists=new ArrayList<>();
        for (Instance instance:allInstances)
        {
            lists.add(new ServiceMeta(serviceName,instance.getIp(),instance.getPort()));
        }
        return lists;
        } catch (NacosException e) {
            System.out.println("获取服务的时候发送了错误：");
        }
        return null;
    }
}
