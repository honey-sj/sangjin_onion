package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class ListUserController implements Controller {
    private static final Logger log = LoggerFactory.getLogger (CreateUserController.class);
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        Collection<User> userList = DataBase.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'>");
        sb.append("<tr> <td>아이디</td> <td>이름</td> <td>이메일</td> </tr>");
        for (User user : userList) {
            sb.append("<tr>");
            sb.append("<td>" + user.getUserId() + "</td>");
            sb.append("<td>" + user.getName() + "</td>");
            sb.append("<td>" + user.getEmail() + "</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        byte[] body = sb.toString().getBytes();


    }
}

