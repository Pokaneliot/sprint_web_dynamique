package control;

import java.io.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.PrintWriter;

public class FrontController extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    		requestUrl(request,response);	
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    		requestUrl(request,response);
    }
    public void requestUrl(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	PrintWriter out = response.getWriter();
        String url=request.getRequestURL().toString();
    	out.print("vous êtes dans "+url);
    }
}