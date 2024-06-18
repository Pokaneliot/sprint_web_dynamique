package mg.pokaneliot.controlleur;

import java.io.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.PrintWriter;
import java.util.Map;
import mg.pokaneliot.util.ModelAndView;
import mg.pokaneliot.util.Mapping;
import mg.pokaneliot.util.Scanner;

public class FrontController extends HttpServlet {
	Map<String,Mapping> urlMap;
    String message;

    public void init() throws ServletException{
        visited = false;
        String controllPackage = this.getInitParameter("controllPackage");
        try{
            String controllPackage = this.getInitParameter("controllerRepository");
            this.urlMap = Scanner.scanCurrentProjet(controllPackage);
        } catch(Exception e){
            message = e.getMessage();
        }
    }
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	processRequest(request,response);	
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	processRequest(request,response);
    }
    protected void processRequest(HttpServletRequest req,HttpServletResponse rep) throws ServletException,IOException{
        PrintWriter out = rep.getWriter();
        if(message != null){
            out.println(message);
        }
        else{
            String url = req.getRequestURL().toString();
            String[] urlParts = url.split("/");
            int i = 1;
            String urlTarget = "/"+urlParts[urlParts.length - 1];
            boolean ver = false;
            out.print("Vous avez entrez cet url :"+url+"\n");
            this.methodExist(url);
            while(i < urlParts.length){
                if(this.urlMap.containsKey(urlTarget)){
                    Mapping mapping = this.urlMap.get(urlTarget);
                    out.println(String.format("La methode %s de la classe %s a ete appelee\n", mapping.getMethodName(),mapping.getClassName()));
                    try{
                        Object obj = executeMethode(mapping);
                        if(obj instanceof String){
                            out.print((String)obj);
                        }
                        else if(obj instanceof ModelAndView){
                            ModelAndView modelV = (ModelAndView)obj;
                            Map<String,Object>map = modelV.getData();
                            if(!this.urlView.contains(modelV.getViewUrl())){
                                out.print("Erreur 404: la page "+modelV.getViewUrl()+" n'existe pas");
                            }
                            else{
                                RequestDispatcher dispat = req.getRequestDispatcher(modelV.getViewUrl());
                                for(Map.Entry<String,Object> entry : map.entrySet()){
                                    String dataName = (String)entry.getKey();
                                    Object data = entry.getValue();
                                    req.setAttribute(dataName,data);
                                }
                                dispat.forward(req,rep);
                            }
                        } 
                        else{
                            String resultType = obj.getClass().getSimpleName();
                            out.print("Execution impossible pour le type de retour : "+resultType);
                        }

                    } catch(Exception e){
                        out.println(e.getMessage());
                    }
                    ver = true;
                    break;
                }
                else{
                    urlTarget = "/"+urlParts[urlParts.length - (i + 1)]+urlTarget;
                }
                i++;
            }
            if(!ver){
                out.print(String.format("Erreur 404 : Aucune methode n'est associe à cet url : %s\n", urlTarget));
            }         
                
        }   
        
        
    }
    public static Object executeMethode(Mapping target,HttpServletRequest req) throws Exception{
        //recuperation des noms des parametres
        Enumeration<String> paramNames = req.getParameterNames();
        ArrayList<String> parametersNames = new ArrayList<>();
        while(paramNames.hasMoreElements()){
            String paramName = paramNames.nextElement();
            parametersNames.add(paramName); //stockage des noms dans une liste
        } 

        String className = target.getClassName(); //nom de la classe contenu dans le mapping
        String methodeName = target.getMethodName(); //nom de la methode a invoquée
        Class<?>cl = Class.forName(className);
        Method[] mes = cl.getDeclaredMethods();

        //recherche de la methode correspondante
        for(Method m : mes){
            if(target.getMethodName().compareTo(m.getName()) == 0){ //si le nom correspond
                Method me = m;
                Parameter[] parms = me.getParameters(); //recuperation des parametres de la methode
                int countParam = parms.length;
                Object instance = cl.getDeclaredConstructor().newInstance();
                Object obj;
                if(countParam>0){ //si la methode possede des parametres
                    ArrayList<Object> paramO = new ArrayList<>();
                    for(Parameter p : parms){
                        if(p.getAnnotation(Param.class) != null){ //si le parametre possede une annotation
                            String annot = p.getAnnotation(Param.class).name();
                            for(String par : parametersNames){
                                if(par.compareTo(annot) == 0){ // si il y a une correspondance, on stocke la valeur dans une liste
                                    String value = req.getParameter(par);
                                    paramO.add(value);
                                    break;
                                }
                            }
                        }
                        else{ //si le parametre ne possede pas d'annotation
                            for(String par : parametersNames){
                                if(par.compareTo(p.getName()) == 0){ // si il y a une correspondance, on stocke la valeur dans une liste
                                    String value = req.getParameter(par);
                                    paramO.add(value);
                                    break;
                                }
                            }
                        }
                    }
                    Object[] p = paramO.toArray(); //conversion de la liste de valeur en tableau d'objet
                    obj = me.invoke(instance,p); //invocation de la methode avec parametre
                }
                else{ //sinon invocation de la methode sans parametre
                    obj= me.invoke(instance);
                }
                if(obj.getClass().getSimpleName().compareTo("String") != 0 && obj.getClass().getSimpleName().compareTo("ModelView") != 0){
                    throw new Exception("Erreur : la methode "+methodeName+" renvoie un objet de type "+obj.getClass().getSimpleName()+".\n Types attendus : ModelView, String");
                }
                return obj;
            }
        }
        return null;
    }
    public void methodExist(String urlMethod) throws Exception{ 
        Map<String,Mapping> urlList = this.urlMap;
        String[] urlParts = urlMethod.split("/");
        String urlTarget = "/"+urlParts[urlParts.length - 1];
        int i = 1;
        while(i < urlParts.length){
            if(this.urlMap.containsKey(urlTarget)){
                return;
            }
            urlTarget = "/"+urlParts[urlParts.length - (i + 1)]+urlTarget;
            i++;
        }
        throw new Exception("Erreur 404 : L'url "+urlMethod+" n'est associé à aucune méthode du projet");
    }
}