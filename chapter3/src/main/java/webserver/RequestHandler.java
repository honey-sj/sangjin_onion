package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.sql.PseudoColumnUsage;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // 요청 라인 추출하기
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String headerMessage = br.readLine();
            //log.debug("HTTP Request Header : {}", headerMessage);

            // 전체 값 추출하기
            if (headerMessage == null) return;

            String tokens[] = headerMessage.split(" ");
            String url = tokens[1];

            // Http Header를 분석한다.
            int contentLen = 0; // pathVariable의 길이
            boolean logined = false;
            while (!"".equals(headerMessage)) {
                headerMessage = br.readLine();
                if (headerMessage.contains("Content-Length")) {
                    contentLen = contentLength(headerMessage);
                } else if (headerMessage.contains("Cookie")) {
                    logined = isLogin(headerMessage);
                }

            }
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            // 회원가입
            if(url.equals("/user/create")) {  // post 방식으로 회원 가입하는 경우
                // br은 요청 본문을 가리키고 있다.
                String pathVariable = IOUtils.readData(br, contentLen);
                // 데이터를 map에 넣어서 받아온다.
                Map<String, String> params = HttpRequestUtils.parseQueryString(pathVariable);
                User user = new User(params.get("userId"), params.get("password"), params.get("name"),
                        params.get("email")); // user 객체를 만든다.
                DataBase.addUser(user); // DB에 user 객체를 넣는다.
                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos, "/index.html");
            }
             else if (url.equals("/user/login")) { // 로그인

                String pathVariable = IOUtils.readData(br, contentLen);
                Map<String, String> params = HttpRequestUtils.parseQueryString(pathVariable);
                User user = DataBase.findUserById(params.get("userId"));

                if(user == null) { // DB에 회원의 정보가 없는 경우
                    responseResource(out, "/user/login_failed.html");
                    return;
                }

                if(user.getPassword().equals(params.get("password"))) { // DB에 저장한 비밀번호와 사용자가 입력한 비밀번호가 동일한 경우
                    DataOutputStream dos = new DataOutputStream(out);
                    response302LoginSuccessHeader(dos); // Header 요청 라인의 상태 코드를 302로 변경하고 Location을 추가한다.
                }else {
                    responseResource(out, "/user/login_failed.html"); // 로그인에 실패한 경우
                }
            } else if (url.equals("/user/list")) { // 사용자의 목록을 조회한다.
                if (!logined) {
                    responseResource(out, "/user/login.html");
                    return;
                }
                Collection<User> userList = DataBase.findAll();
                log.debug("Found {} users", userList.size());
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
                DataOutputStream dos = new DataOutputStream(out);
                response200Header(dos, body.length);
                responseBody(dos, body);
            }else if (url.endsWith(".css")) { // css 적용
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                response200CssHeader(dos, body.length);
                responseBody(dos, body);
            }else{
                // webapp 폴더에서 tokens로 나눈 파일로 경로 이동 시킴
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                DataOutputStream dos = new DataOutputStream(out);
                response200Header(dos, body.length);
                responseBody(dos, body);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    // ● contentLength : content-Length의 길이를 반환하는 메소드
    public int contentLength(String headerMessage) {
        return Integer.parseInt(headerMessage.split(": ")[1]);
    }

    // ● isLogin : Cookie의 상태를 반환하는 메소드
    // Ex) line의 형태 → Cookie: logined=true
    public boolean isLogin(String headerMessage) {
        String state = headerMessage.split(": ")[1].split("=")[1];
        return Boolean.parseBoolean(state);
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: "+url+"\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }



    // ● responseResource : HTTP header와 Body를 생성한다.
    private void responseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    // ● response302LoginSuccessHeader : 로그인을 했으므로 Cookie의 상태를 true로 만들고 index.html로 redirect하는 메소드
    private void response302LoginSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: /index.html \r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ● response200CssHeader : Http Header를 생성하는 메소드
    private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
