package mg.pokaneliot.util;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

import mg.pokaneliot.annotation.Controller;
import mg.pokaneliot.annotation.Get;

public class Scanner {
    public static ArrayList<Class> scanCurrentProjet(String packageName)throws Exception{
        /*ArrayList<Class> res = new ArrayList<>();
        try {
            Thread currentThread = Thread.currentThread();
            ClassLoader classLoader = currentThread.getContextClassLoader();
            String path = packageName.replace(".", "/");

            // Decode URL-encoded spaces
            path = URLDecoder.decode(path, "UTF-8");

            System.out.println("Scanning path: " + path);
            URL resource = classLoader.getResource(path);
            if (resource == null) {
                throw new Exception("The package " + packageName + " does not exist in the project");
            }

            File directory = new File(resource.getFile());
            if (!directory.exists() || !directory.isDirectory()) {
                throw new Exception("The package " + packageName + " does not exist in the project");
            }

            System.out.println("Number of files: " + directory.listFiles().length);
            for (File file : directory.listFiles()) {
                if (file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> cl = Class.forName(className);
                    if (cl.isAnnotationPresent(Controller.class)) {
                        res.add(cl);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = "An error occurred while scanning the package " + packageName;
            throw new Exception(message, e);
        }
        return res;*/
         ArrayList<Class>res = new ArrayList<>();
        try{
            Thread currentThread = Thread.currentThread();
            ClassLoader classLoader = currentThread.getContextClassLoader();
            String path = packageName.replace(".", "/");
            System.out.println(path);
            java.net.URL ressource = classLoader.getResource(path);
            ressource = new java.net.URL(ressource.toString().replace("%20", " "));
            System.out.println(ressource.toString());
            java.io.File directory = new java.io.File(ressource.getFile());
            System.out.println(directory.listFiles().length);
            for(java.io.File file : directory.listFiles()){
                if(file.getName().endsWith(".class")){
                    String className = packageName + "."+ file.getName().substring(0,file.getName().length() - 6);
                    Class<?> cl = Class.forName(className);
                    Controller annot = cl.getAnnotation(Controller.class);
                    if(annot != null){
                        res.add(cl);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            String message = "Le nom de package "+packageName+" n'existe pas dans le projet";
            throw new Exception(message);
        }
        return res;
    }
    public static String buildMessage(String base,ArrayList message,String terminaison){
        String res = base + "\n";
        for(int i = 0; i<message.size(); i++){
            res += message.get(i) + terminaison + "\n";
        }
        return res;
    }
    //recherche de toutes les methodes
    public static Map<String,Mapping> scanMethod(String packageName) throws Exception{
        Map<String,Mapping> res = new HashMap<>();
        ArrayList<String>message = new ArrayList<>();
        try{
            ArrayList<Class>listClass = scanCurrentProjet(packageName);
            for(Object c : listClass){
                Class cl = (Class)c;
                Method[] listMethod = cl.getDeclaredMethods();
                for(Method me : listMethod){
                    Get a = me.getAnnotation(Get.class);
                    if(a != null){
                        if(res.containsKey(a.url())){
                            message.add(a.url());
                        }
                        else {
                            String className = cl.getName();
                            String methodName = me.getName();
                            Mapping m = new Mapping(className,methodName);
                            res.put(a.url(),m);
                        }
                    }
                }
            }
            if(message.size() > 0){
                String mes = buildMessage("Erreur au niveau des urls:", message," est un url d'une autre methode");
                Exception ex =  new Exception(mes);
                throw ex;
            }
        } catch(Exception e){
            throw new Exception(e.getMessage());
        }
        return res;
    }
    
    //recherche de toutes les view
    public static ArrayList<String> scanView(String container) throws Exception{
        ArrayList<String>res = new ArrayList<>();
        String path=container;
        Thread currentThread = Thread.currentThread();
        ClassLoader classLoader = currentThread.getContextClassLoader();
        java.net.URL ressource = classLoader.getResource(path);
        ressource = new java.net.URL(ressource.toString().replace("%20", " "));
        System.out.println(ressource.toString());
        java.io.File directory = new java.io.File(ressource.getFile());
        res.add(directory.getAbsolutePath());
        for(java.io.File file : directory.listFiles()){
            if(file.getName().endsWith(".jsp")){
                String fileName = file.getName();
                res.add(fileName);
            }
        }
        return res; 
    }

    //recuperation du types d'un parametre d'une methode
    public static Class<?> takeTypeField(Class<?> model, String fiel) {
        Class<?> res = null;
        try {
            Field[] fields = model.getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals(fiel)) {
                    res = field.getType();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    //conversion des parametres
    public static Object convertParameterValue(Class<?> targetType, String parameterValue,String argName) throws Exception{
        String erreur = "Une valeur de type "+targetType.getSimpleName()+" est attendue pour l'entrée: "+argName+". Valeur trouvée : "+parameterValue;
        if (targetType == String.class) {
            return parameterValue;
        } else if (targetType == int.class || targetType == Integer.class) {
            try{
                return Integer.parseInt(parameterValue);
            } catch(Exception e){
                throw new Exception(erreur);
            }
        } else if (targetType == long.class || targetType == Long.class) {
            try{
                return Long.parseLong(parameterValue);
            } catch(Exception e){
                throw new Exception(erreur);
            }
        } else if (targetType == float.class || targetType == Float.class) {
            try{
                return Float.parseFloat(parameterValue);
            } catch(Exception e){
                throw new Exception(erreur);
            }
        } else if (targetType == double.class || targetType == Double.class) {
            try{
                return Double.parseDouble(parameterValue);
            } catch(Exception e){
                throw new Exception(erreur);
            }
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            try{
                return Boolean.parseBoolean(parameterValue);
            } catch(Exception e){
                throw new Exception(erreur);
            } 
        }
        return null;
    }



}