# luna
rpc
基本上拥有dubbo基本核心功能 序列化使用protostuff 传输层使用netty 注册中心使用zookeeper.
1. 透明化的远程方法调用，就像调用本地方法一样调用远程方法，只需简单配置，没有任何API侵入。(比dubbo更简洁)
2. 软负载均衡及容错机制，可在内网替代F5等硬件负载均衡器，降低成本，减少单点。(负载均衡暂时实现轮训和随机)
3. 服务自动注册与发现，不再需要写死服务提供方地址，注册中心基于接口名查询服务提供者的IP地址，并且能够平滑添加或删除服务提供者。(动态服务注册与发现)

调用过程
1. 服务提供者在启动时，向注册中心注册自己提供的服务。
2. 服务消费者在启动时，向注册中心订阅自己所需的服务。
3. 注册中心返回服务提供者地址列表给消费者，如果有变更，注册中心将基于长连接推送变更数据给消费者。
4. 服务消费者，从提供者地址列表中，基于软负载均衡算法，选一台提供者进行调用，如果调用失败，再选另一台调用。

使用方法<br/>
provider<br/>
<luna:registry id="abcde" address="localhost:2181"/><!--注册中心zookeeper地址--><br/>
<luna:sev id="sev" port="3334" /><!--服务端暴露地址--><br/>

consumer<br/>
<luna:registry id="reg" address="localhost:2181"/><!--注册中心zookeeper地址--><br/>
<luna:cli id="xx" service="com.xx.xx.service.BxxService"/><!--service1路径名称--><br/>
<luna:cli id="xxx" service="com.xx.xx.service.BxxxService"/><!--service2路径名称--><br/>


监控中心待完善.
