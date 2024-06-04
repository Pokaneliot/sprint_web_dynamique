package mg.pokaneliot.controlleur;

import java.io.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.PrintWriter;
import java.util.Map;
import mg.pokaneliot.util.ModelAndView;
import mg.pokaneliot.util.Mapping;
import mg.pokaneliot.util.Scanner;

public class FrontController extends HttpServlet {
	Map<String,Mapping> urlMap;

    public void init() throws ServletException{
        String controllPackage = this.getInitParameter("controllerRepository");
        this.urlMap = Scanner.scanCurrentProjet(controllPackage);
    }
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	processRequest(request,response);	
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	processRequest(request,response);
    }
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	PrintWriter out = response.getWriter();
        String url=request.getRequestURL().toString();
        int check=0;
    	out.print("vous êtes dans "+url+"\n");
        for(String key : this.urlMap.keySet()){
            int len=key.length()-1;
            int urlLen=url.length()-1;
            String urlkey=url.substring(urlLen-len,urlLen);
            if (key.compare(urlkey)==0) {
                Mapping map= this.urlMap.get(key);
                out.print("L'url : "+ urlkey +" est associé à la methode "+map.getMethodName()+"dans la class "+map.getClassName()+"\n");
                out.print("l'output : "+executeMethode(map,out,request,response));
                check=1;
            }
        }
        if (check==0){
                out.print("L'url : "+ urlkey +" n'est pas associé à une methode dans une classe annotée controller \n");
        }
    }
    public static void executeMethode(Mapping map,PrintWriter out,HttpServletRequest req, HttpServletResponse res) throws Exception{
        String clname=map.getClassName();
        String mthname=map.getMethodName();
        Class<?> cl=Class.forName(clname);
        Method mth=cl.getDeclaredMethod(mthname);
        Object classrep=cl.getDeclaredConstructor().newInstance();
        Object obj=mth.invoke(classrep);
         if(obj instanceof String){
            out.print((String)obj);
        }
        else if(obj instanceof ModelAndView){
            ModelAndView modelV = (ModelAndView)obj;
            Map<String,Object>map = modelV.getData();
            RequestDispatcher dispat = req.getRequestDispatcher(modelV.getViewUrl());
            for(Map.Entry<String,Object> entry : map.entrySet()){
                String dataName = (String)entry.getKey();
                Object data = entry.getValue();
                req.setAttribute(dataName,data);
            }
            dispat.forward(req,res);
        } 
        else{
            String resultType = obj.getClass().getSimpleName();
            throw new Exception("Execution impossible pour le type de retour : "+resultType);
        }

    }
}