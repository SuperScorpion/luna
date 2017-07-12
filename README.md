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
<br/>
使用方法<br/>
provider<br/>
在需要暴露的服务 Impl 上添加@LunaService
<luna:registry id="abcde" address="localhost:2181"/><!--注册中心zookeeper地址--><br/>
<luna:sev id="sev" port="3334" /><!--服务端暴露地址--><br/>

consumer<br/>
<luna:registry id="reg" address="localhost:2181"/><!--注册中心zookeeper地址--><br/>
<luna:cli id="xx" service="com.xx.xx.service.BxxService"/><!--service1路径名称--><br/>
<luna:cli id="xxx" service="com.xx.xx.service.BxxxService"/><!--service2路径名称--><br/>
<br/>
如果需要跳过zookeeper注册中心
consumer端直接添加url属性不填写address属性即可.
provider端不填写address属性即可.

监控中心待完善.

遇到问题
1.序列化框架protostuff的问题 Object[null, xxx] -> Object[xxx].<br/>
2.序列化protostuff的问题 还原序列化的时候 Objenesis 会把实体类里的属性默认值加上.<br/>
3.lock的condition await signalall 必须持有资源锁才能有效.<br/>
4.idea的断点会让zookeeper 产生ConnectionLossException.<br/>
5.spring自定义标签id如何去掉.<br/>
6.client端添加了shutdownGracefully channel.writeAndFlush(request) 时会报 netty event executor terminated(exception)<br/>
7.netty连接不了此localhost或者127.0.0.1地址.
