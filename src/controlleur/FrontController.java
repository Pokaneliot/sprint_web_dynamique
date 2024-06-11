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
            else{
                out.print("Vous avez entrez cet url :"+url+"\n");
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
        
        
    }
    public static Object executeMethode(Mapping target) throws Exception{
        String className = target.getClassName();
        String methodeName = target.getMethodName();
        Class<?>cl = Class.forName(className);
        Method me = cl.getDeclaredMethod(methodeName);
        Object instance = cl.getDeclaredConstructor().newInstance();
        Object obj = me.invoke(instance);
        return obj;
        
    }
}