package mg.pokaneliot.controlleur;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;

import com.google.gson.Gson;

import mg.pokaneliot.util.ModelAndView;
import mg.pokaneliot.util.Mapping;
import mg.pokaneliot.util.Scanner;
import mg.pokaneliot.util.MySession;

import mg.pokaneliot.annotation.Param;
import mg.pokaneliot.annotation.RestApi;
import mg.pokaneliot.annotation.ErrorInput;
import mg.pokaneliot.annotation.AuthMethod;
import mg.pokaneliot.annotation.AuthClass;

public class FrontController extends HttpServlet {
	Map<String, Mapping> urlMap;
    ArrayList<String> urlView;
    String message;
    boolean visited;

    public void init() throws ServletException {
        visited = false;
        String controllPackage = this.getInitParameter("controllPackage");
        try {
            this.urlMap = Scanner.scanMethod(controllPackage);
            this.urlView = Scanner.scanView("../..");
        } catch (Exception e) {
            message = "Erreur au niveau du build du projet. Veuillez consulter votre terminal";
            e.printStackTrace();
        }

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException {
        processRequest(req, rep);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException {
        processRequest(req, rep);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse rep)
            throws ServletException, IOException {

        PrintWriter out = rep.getWriter();
        Map<String, String> paramMap = new HashMap<>();
        String verb=req.getMethod();
        if (req.getAttribute("errorInput")!=null) {
            verb="GET";
        }
        if (message != null) {
            this.displayError(message,"404",rep);
        } else {
            String url = req.getRequestURL().toString();
            if (!visited) {
                out.println("Bienvenue sur la page, veuillez saisir un url!");
                visited = true;
            } else {
                try {
                    Mapping mapping = this.methodExist(url);//note a moi même comme mettre l'urltarget comme type de retour de la fonction methodExist
                    Method m=getMethodTarget(req,mapping,verb);
                    verifAuth(req,m);
                    Object obj = executeMethode(mapping,m, req, rep);
                    if (m.getAnnotation(RestApi.class)!=null) {
                        json(rep,obj);
                    }
                    else{
                        text(req,rep,obj);
                    }

                } catch (Exception e) {
                    this.displayError(e.getMessage(),"404",rep);
                }
            }

        }
        out.flush();
        out.close();

    }
    protected void verifAuth(HttpServletRequest req, Method action) throws Exception{
        HttpSession session = req.getSession();
        String param = this.getInitParameter("sessionUser");
        String usertype=(String)session.getAttribute(param);
        if(action.getAnnotation(AuthMethod.class) != null){ 
            String auth = action.getAnnotation(AuthMethod.class).type();
            if(auth.equals(usertype)==false ){
                throw new Exception("Vous n'avez pas accès à la methode"); 
            }
        }
    }
    protected void verifAuth(HttpServletRequest req, Class<?> cl) throws Exception{
        HttpSession session = req.getSession();
        String param = this.getInitParameter("sessionUser");
        String usertype=(String)session.getAttribute(param);
        if(cl.getAnnotation(AuthClass.class) != null){ 
            String auth = cl.getAnnotation(AuthClass.class).type();
            if(auth.equals(usertype)==false){
                throw new Exception("Vous n'avez pas accès à une class utilisé."); 
            }
        }
    }
    protected Method getMethodTarget(HttpServletRequest req, Mapping target,String verb)throws Exception{
        String className = target.getClassName(); //nom de la classe contenu dans le mapping

        String methodeName =target.getMethodName(verb);  //nom de la methode a invoquée
        if (methodeName==null) {
            throw new Exception("Une requete de type "+verb+" est attendue pour la methode");
        }
        Class<?>cl = Class.forName(className);//Recuperation de la classe qui va invoquer la methode
        verifAuth(req,cl);
        Method[] mes = cl.getDeclaredMethods();//Recuperation de la liste des methodes

        //recherche de la methode correspondante
        for(Method m : mes){
            if(methodeName.compareTo(m.getName()) == 0){
                return m;
            }
        }
        throw new Exception("Erreur: La methode n'existe point");

    }
    protected void text(HttpServletRequest req, HttpServletResponse rep,Object obj)throws ServletException, IOException, Exception 
    {
        PrintWriter out = rep.getWriter();
        if (obj instanceof String) {
            out.print((String) obj);
            out.flush();
            out.close();
        } else if (obj instanceof ModelAndView) {
            ModelAndView modelV = (ModelAndView) obj;
            Map<String, Object> map = modelV.getData();
            this.viewExist(modelV.getUrl());
            RequestDispatcher dispat = req.getRequestDispatcher(modelV.getUrl());
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String dataName = (String) entry.getKey();
                Object data = entry.getValue();
                req.setAttribute(dataName, data);
            }
            dispat.forward(req, rep);
        }
    }
    protected void json(HttpServletResponse rep,Object obj)throws ServletException, IOException, Exception 
    {
        rep.setContentType("application/json");
        rep.setCharacterEncoding("UTF-8");
        PrintWriter out = rep.getWriter();
        Gson gson = new Gson();
        if (obj instanceof ModelAndView) {
            ModelAndView modelV = (ModelAndView) obj;
            Map<String, Object> map = modelV.getData();
            this.viewExist(modelV.getUrl());
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                /*String dataName = (String) entry.getKey();*/
                Object data = entry.getValue();
                String jsonResponse = gson.toJson(data);
                out.print(jsonResponse);
            }
        }
        else{
            String jsonResponse = gson.toJson(obj);
            out.print(jsonResponse);
        }
        out.flush();
        out.close();
    }

    public static Object executeMethode(Mapping map,Method me,HttpServletRequest req,HttpServletResponse rep) throws Exception{
        String className=map.getClassName();
        Class<?>cl = Class.forName(className);
        String methodeName= me.getName();
        //recuperation des noms des parametres 
        Enumeration<String> paramNames = req.getParameterNames();
        ArrayList<String> parametersNames = new ArrayList<>();
        while(paramNames.hasMoreElements()){
            String paramName = paramNames.nextElement();
            parametersNames.add(paramName); //stockage des noms dans une liste
        } 
        Parameter[] parms = me.getParameters(); //recuperation des parametres de la methode
        int countParam = parms.length; //nom d'argument de la methode
        Object instance = cl.getDeclaredConstructor().newInstance(); //instanciation de l'objet qui va executer la methode
        Object obj;
        if(countParam > 1){ //si la methode possede des parametres
            ArrayList<Object> paramO = new ArrayList<>();
            ArrayList<String>passage = new ArrayList<>(); //pour verifier si on est pas deja passer par le parametre
            boolean hasError = false;
            for(Parameter p : parms){
                Class<?> paramType = p.getType(); //recuperation du type du parametre
                String typeName = paramType.getSimpleName(); //type du parametre
                String annot;
                if(paramType.getSimpleName().compareTo("MySession") == 0){
                    HttpSession session = req.getSession();
                    MySession sess = (MySession)(paramType.getDeclaredConstructor(HttpSession.class).newInstance(session));
                    paramO.add(sess);
                }
                if(p.getAnnotation(Param.class) != null){ //si le parametre possede une annotation
                    annot = p.getAnnotation(Param.class).name(); //on prend la valeur de l'annnotation
                }else{
                    annot = p.getName(); // on prend le nom du parametre
                }
                Map<String,String> errorMessage = new HashMap<>(); 
                for(String par : parametersNames){
                    String[] paramParts = par.split("_");//separation du parametre pour savoir si on a besoin d'un objet
                    String argName = "";
                    if(paramParts.length > 1){ //si c'est le cas
                        System.out.println("Objet");
                        String objName = paramParts[0]; //nom de la classe object
                        argName = paramParts[1]; //nom du parametre
                        if(annot.compareTo(objName) == 0){ //validite du parametre
                            if(!passage.contains(annot)){
                                Object instanceParam = paramType.getDeclaredConstructor().newInstance(); //instanciation de l'objet
                                paramO.add(instanceParam);//ajout de l'objet à la liste des parametre
                                passage.add(annot);//marquer comme passer
                            }
                            Class<?> typ = Scanner.takeTypeField(paramType,argName); //recuperation du type de l'argument
                            Object value = null;
                            try{
                                value = Scanner.convertParameterValueWithAnnot(paramType, req, argName); //conversion de la valeur avec des annotations
                                Object inst = paramO.get(paramO.size() - 1); //prise du dernier parametre
                                String methName = "set"+argName.substring(0,1).toUpperCase() + argName.substring(1); //nom du setteur correspondant
                                Method set;
                                if (value instanceof Integer) {
                                    set = paramType.getMethod(methName, int.class);    
                                } else{
                                    set = paramType.getMethod(methName, value.getClass());   
                                }
                                set.invoke(inst, value);
                                
                            } catch(Exception e){
                                //stockage des erreur
                                hasError = true;
                                errorMessage.put(par,e.getMessage());

                            }
                        }
                    } else{
                        argName = paramParts[0];
                        if(argName.compareTo(annot) == 0){ // si il y a une correspondance, on stocke la valeur dans une liste
                            Object value = Scanner.convertParameterValue(paramType, req,argName);//Conversion du parametre
                            paramO.add(value); //ajout du parametre
                            break;
                        }
                    }  
                }
                if(hasError){
                    /*errorMessage.forEach((key, value) -> System.out.println("Erreur de l'attribut "+key+" : "+value));*/
                    /*throw new Exception("validation attribut non valide veuillez voir la ligne de commande");*/
                    displayErrorInput(me,req,rep,errorMessage);
                }

            }
            Object[] p = paramO.toArray(); //conversion de la liste de valeur en tableau d'objet
            obj = me.invoke(instance,p); //invocation de la methode avec parametre
        }
        else{ //sinon invocation de la methode sans parametre
            obj= me.invoke(instance);
        }
        if(obj.getClass().getSimpleName().compareTo("String") != 0 && obj.getClass().getSimpleName().compareTo("ModelAndView") != 0){ //Exception si ce n'est ni un String ni un modelAndView
            throw new Exception("Erreur : la methode "+methodeName+" renvoie un objet de type "+obj.getClass().getSimpleName()+".\n Types attendus : ModelAndView, String");
        }
        return obj;
    }

    //display input error
    public static void displayErrorInput(Method me,HttpServletRequest req,HttpServletResponse rep,Map<String,String> errors )throws Exception{
        String url=null;
        if(me.getAnnotation(ErrorInput.class) != null){
            url = me.getAnnotation(ErrorInput.class).url();
            RequestDispatcher dispat = req.getRequestDispatcher(url);
            req.setAttribute("errorInput",errors);
            dispat.forward(req, rep);
        }
        else{
            throw new Exception("Erreur : validation attribut non valide.\n La methode "+me.getName()+" ne contient pas d'annotation \"ErrorInput\".");
        }
    }
    
    //si la vue exist
    public void viewExist(String viewUrl) throws Exception {
        ArrayList<String> listView = this.urlView;
        if (!listView.contains(viewUrl)) {
            throw new Exception("Erreur : La page " + viewUrl + " n'existe pas!");
        }
    }
    //si la methodExist
    public Mapping methodExist(String urlMethod) throws Exception {
        Map<String, Mapping> urlList = this.urlMap;
        String[] urlParts = urlMethod.split("/");
        String urlTarget = "/" + urlParts[urlParts.length - 1];
        int i = 1;
        while (i < urlParts.length) {
            if (this.urlMap.containsKey(urlTarget)) {
                return this.urlMap.get(urlTarget);
            }
            urlTarget = "/" + urlParts[urlParts.length - (i + 1)] + urlTarget;
            i++;
        }
        throw new Exception("Erreur: L'url " + urlMethod + " n'est associé à aucune méthode du projet");
    }


    
    // recherche de la methode correspondante
    public static Method getMethod(Object obj, String methodName) throws Exception {
        Class<?> cl = obj.getClass();
        Method[] metList = cl.getDeclaredMethods();
        for (Method me : metList) {
            if (me.getName().compareTo(methodName) == 0) {
                return me;
            }
        }
        return null;
    }

    protected void displayError(String error,String code,HttpServletResponse rep) throws IOException,ServletException{
        rep.setContentType("text/html");
        PrintWriter out = rep.getWriter();

        //page 
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Error</title>");

        // CSS intégré dans la balise <style>
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; background-color: #f0f0f0; }");
        out.println("h1 { color: red; }");
        out.println("p { font-size: 16px; }");
        out.println("ul { list-style-type: none; }");
        out.println("li { background-color: #e0e0e0; margin: 5px 0; padding: 10px; border-radius: 5px; }");
        out.println("</style>");
        //fin style 

        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Vous avez rencontre un probleme</h1>");
        out.println("<p><strong>"+code+"</strong></p>");
        out.println("<p><strong>"+error+"</strong></p>");
        out.println("<p>pour plus de details, consulter la console</p>");
        out.println("</ul>");
        out.println("</body>");
        out.println("</html>");
    }

}