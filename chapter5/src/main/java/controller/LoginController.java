package controller;
import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoginController implements Controller {
    private static final Logger log = LoggerFactory.getLogger (CreateUserController.class);
    @Override
    public void service(HttpRequest request, HttpResponse response) {

        User user = DataBase.findUserById(request.getParameter("userId"));

        if (user == null) {// DB에 회원의 정보가 없는 경우
            response.sendRedirect( "/user/login_failed.html");
            return;
        }

        if (user.getPassword().equals(request.getParameter("password"))) { // DB에 저장한 비밀번호와 사용자가 입력한 비밀번호가 동일한 경우
            response.sendRedirect("/index.html"); // Header 요청 라인의 상태 코드를 302로 변경하고 Location을 추가한다.
        } else {
            response.sendRedirect( "/user/login_failed.html"); // 로그인에 실패한 경우
        }

    }
}

