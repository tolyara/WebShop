package servlets.admin;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Account;
import models.Basket;
import models.Product;
import service.StorageIdentifier;
import storages.Storage;

import java.io.IOException;

public class ChangeAccountStatusServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String VIEWADMIN_PATH = "/admin/view";
	private static final Storage SHOP_WEB = StorageIdentifier.getStorage();

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {    	
    	SHOP_WEB.changeAccountStatus((req.getParameter("login")), Boolean.valueOf(req.getParameter("currentStatus")));
    	resp.sendRedirect(String.format("%s%s", req.getContextPath(), VIEWADMIN_PATH));
    }

}
