package http;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import util.IOUtils;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(RequestLine.class);

    private HttpMethod method;
    private String path;
    private Map<String, String> params = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();

    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            if(line == null) {
                return;
            }

            processRequestLine(line);

            line = br.readLine();
            while(!line.equals("")) {
                log.debug("header : {}", line);
                String[] tokens = line.split(":");
                headers.put(tokens[0].trim(), tokens[1].trim());
                line = br.readLine();
            }

            if ("POST".equals(method)) {
                String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                params = HttpRequestUtils.parseQueryString(body);
            }
        } catch (IOException e) {
            log.debug(e.getMessage());
        }
    }

    private void processRequestLine(String requestLine) {
        log.debug("request line : {}", requestLine);
        String[] tokens = requestLine.split(" ");
        // method 종류: GET or POST
        method = HttpMethod.valueOf(tokens[0]);

        // POST
        if("POST".equals(method)) {
            path = tokens[1];
            return;
        }

        // GET
        int index = tokens[1].indexOf("?");
        if(index==-1) {
            path = tokens[1];
        }else {
            path = tokens[1].substring(0, index);
            params = HttpRequestUtils.parseQueryString(tokens[1].substring(index+1));
        }
    }

    // 저장한 값에 접근할 수 있도록 하는 get 메소
    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getParameter(String name) {
        return params.get(name);
    }


}

