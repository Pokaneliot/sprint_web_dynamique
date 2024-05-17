package mg.pokaneliot.controlleur;

import java.io.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.PrintWriter;
import java.util.Map;


public class FrontController extends HttpServlet {
	Map<String,Class> urlMap;
    boolean isChecked;
    public void init() throws ServletException{
        isChecked=false;
    }
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    		requestUrl(request,response);	
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    		requestUrl(request,response);
    }
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	if (!isChecked) {
            String controllPackage = this.getInitParameter("controllerRepository");
            this.urlMap = Scanner.scanCurrentProjet(controllPackage);
            isChecked=true;
        }
        PrintWriter out = response.getWriter();
        String url=request.getRequestURL().toString();
    	out.print("vous êtes dans "+url+"\n");
         out.print("Liste des controlleurs du projet : \n");
        for(String key : this.urlMap.keySet()){
            out.print("L'url : "+ key +" est associé à la class "+ this.urlMap.get(key)+"\n");
        }
    }
}