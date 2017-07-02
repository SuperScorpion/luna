package com.jy.luna;

/**
 * Created by neo on 2017/6/20.
 */
public class Label {
    /**
     * 1. 动态代理
     * 2. netty传输
     * 3. 序列化
     * 4. zookeeper 挂载
     * 5. 自定义标签(忽略)
     */


    public static void main(String[] args) throws Exception {
        /*ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");

        Map<String, Registry> rmp =  context.getBeansOfType(Registry.class);

        for(Registry rgs : rmp.values()) {
            System.out.println(rgs.getAddress());
        }


        Map<String, Cli> c =  context.getBeansOfType(Cli.class);
        System.out.println(c.size());
        for(Cli s : c.values()) {
        }*/

        /*Method[] mcs = BaseServiceImpl.class.getDeclaredMethods();

        for(Method m : mcs) {
            System.out.println(m.getName());
            for(Type t : m.getParameterTypes()) {
            System.out.println(t.getTypeName());

            }
            System.out.println("");
        }*/

//        Object[] a = {1,2,3,4,5,6,7,8,9};
        Object s = "";
        if (s instanceof Object[]) {
            System.out.println("fdsfdsafa");
        }



        /*Long s = System.currentTimeMillis();
        Thread.currentThread().sleep(1000);
        System.out.println(System.currentTimeMillis() - s);*/
    }

}
