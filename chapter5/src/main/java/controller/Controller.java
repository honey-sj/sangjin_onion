package controller;
import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Controller {
    void service(HttpRequest request, HttpResponse response);
}






