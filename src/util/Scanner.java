package mg.pokaneliot.util;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import java.text.SimpleDateFormat;

import java.sql.Date;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import mg.pokaneliot.annotation.Controller;
import mg.pokaneliot.annotation.Get;
import mg.pokaneliot.annotation.Post;
import mg.pokaneliot.annotation.Url;
import mg.pokaneliot.util.MultipartFile;
import mg.pokaneliot.annotation.type.*;



public class Scanner {
    public static ArrayList<Class> scanCurrentProjet(String packageName)throws Exception{
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
                    Url url = me.getAnnotation(Url.class);
                    if(url != null){
                        String verb=Scanner.getVerb(me);
                        VerbAction va=new VerbAction(me.getName(),verb);
                        if(res.containsKey(url.url())){
                            Mapping map=(Mapping)res.get(url.url());
                            if (!map.contains(verb)) {
                                map.addVA(va);                                
                            }
                            else{
                                message.add(url.url());
                            }
                        }
                        else {
                            String className = cl.getName();
                            Mapping m = new Mapping(className);
                            m.addVA(va);
                            res.put(url.url(),m);
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

     public static Field takeField(Class<?> model, String name) throws Exception{
        Field target = null;
        try{
            target = model.getDeclaredField(name);
        } catch(Exception e){
            throw e;
        }
        return target;
    }

    //conversion des parametres
    public static Object convertParameterValue(Class<?> targetType, HttpServletRequest req,String argName) throws Exception{
        String parameterValue=req.getParameter(argName);
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
        } else if (targetType== MultipartFile.class) {
            try{
                 // Retrieve the file part from the request
                Part filePart = req.getPart(argName); // argname matches the name attribute in the form

                // Get the file name
                String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

                // Get the content type
                String contentType = filePart.getContentType();

                // Get the input stream of the uploaded file
                InputStream fileContent = filePart.getInputStream();

                // Convert the input stream to bytes
                byte[] fileBytes = fileContent.readAllBytes();
                fileContent.close();

                return new MultipartFile(fileName, contentType, fileBytes);
            } catch(Exception e){
                throw new Exception(erreur);
            } 
        }
        return null;
    }
    //conversion des parametres
    //ne pas directement throw les exception dans les catch mais stocker dans un tableau d'abord et c'est le tableau qu'on throw 
    public static Object convertParameterValueWithAnnot(Class<?> model, HttpServletRequest req,String argName) throws Exception{
        String parameterValue=req.getParameter(argName);
        String targetType = "";
        String erreur = "Une valeur de type %s est attendue pour l'entrée: "+argName+". Valeur trouvée : %s \n";
        String erreurs="";
        Field f = takeField(model, argName);
        if(f.getDeclaredAnnotation(DateSQL.class) != null){ //type date
            String format = f.getDeclaredAnnotation(DateSQL.class).format();
            try{
                return convertDate(parameterValue, format);
            }catch(Exception e){
                erreurs+=String.format(erreur, "DateSql",parameterValue);
            }
        }
        if(f.getDeclaredAnnotation(Numeric.class) != null){ //type numeri
            try{
                return Integer.parseInt(parameterValue);
            }catch(Exception ie){
                try{
                    return Double.parseDouble(parameterValue);
                } catch(Exception de){
                    try{
                        return Float.parseFloat(parameterValue);
                    } catch(Exception fe){
                        try{
                            return Long.parseLong(parameterValue);
                        }catch(Exception e){
                            targetType = "Numeric";
                            erreurs+=String.format(erreur,targetType,parameterValue);
                        }
                    }
                }
            } 
        }
        if(f.getDeclaredAnnotation(Text.class) != null){ //type string
            return parameterValue;
        }
        if(f.getDeclaredAnnotation(Bool.class) != null){ //type boolean
            try{
                return Boolean.parseBoolean(parameterValue);
            }catch(Exception e){
                erreurs+=String.format(erreur, "Boolean",parameterValue);
            }
        }
        if(erreurs==""){ 
            Class<?> targetedType = takeTypeField(model, argName);
            return convertParameterValue(targetedType, req, argName);
        }
        else{
            throw new Exception(erreurs);
        }
    }

    public static Date convertDate(String date,String format) throws Exception{
        Date d = null;
        try{
            SimpleDateFormat dF = new SimpleDateFormat(format);
            java.util.Date dT = dF.parse(date);
            d = new Date(dT.getTime());
        }catch(Exception e){
            throw e;
        }
        return d;
    }
    public static String getVerb(Method m){
        String res="GET";
        if (m.getAnnotation(Post.class)!=null) {
            res="POST";
        }
        return res;
    }



}