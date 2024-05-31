package mg.pokaneliot.util;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;


import mg.pokaneliot.annotation.Controller;
import mg.pokaneliot.annotation.Get;

public class Scanner {
    public static Map<String,Mapping> scanCurrentProjet(String packageName){
        Map<String,Mapping> res = new HashMap<>();
        try{
            Thread currentThread = Thread.currentThread();
            ClassLoader classLoader = currentThread.getContextClassLoader();
            String path = packageName.replace(".", "/");
            path = packageName.replace("%20", " ");
            java.net.URL ressource = classLoader.getResource(path);
            java.io.File directory = new java.io.File(ressource.getFile());

            for(java.io.File file : directory.listFiles()){
                if(file.getName().endsWith(".class")){
                    String className = packageName + "."+ file.getName().substring(0,file.getName().length() - 6);
                    Class<?> cl = Class.forName(className);
                    Controller annot = cl.getAnnotation(Controller.class);
                    if(annot != null){
                        setAnnotedMethod(res,cl);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return res;
    }
    public static void setAnnotedMethod(Map<String,Mapping> res,Class cl){
        Method[] meths=cl.getDeclaredMethods();
        for (Method meth:meths ) {
           Get annot_get=meth.getAnnotation(Get.class);
           if (annot_get!=null) {
               Mapping map=new Mapping(cl.getName(),meth.getName());
               res.put(annot_get.url(),map);
           }
        } 
    } 
}
