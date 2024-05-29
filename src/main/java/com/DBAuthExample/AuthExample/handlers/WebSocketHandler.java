package com.DBAuthExample.AuthExample.handlers;

import com.DBAuthExample.AuthExample.Config.Matrix;
import com.DBAuthExample.AuthExample.Entity.FileToUpload;
import com.DBAuthExample.AuthExample.Entity.User;
import com.DBAuthExample.AuthExample.Services.MyUserDetailsService;
import com.DBAuthExample.AuthExample.repository.UserRepo;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;

@Component
@Data
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    private static final Set<WebSocketSession> sessions = new HashSet<>();

    private static final Map<String, Set<SocketWithRule>> rooms = new HashMap<>();

    private MyUserDetailsService userDetailsService;

    public WebSocketHandler(MyUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session){
        logger.info("Connection established: " + session.getId());
        sessions.add(session);
        URI uri = session.getUri();
        assert uri != null;
        String query = uri.getRawQuery();
        MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
        String filePath = queryParams.getFirst("Room");

        if (!rooms.containsKey(filePath)) {
            rooms.put(filePath, new HashSet<>());
            rooms.get(filePath).add(new SocketWithRule(session,queryParams.getFirst("Rule"),userDetailsService.loadUserByID(Long.parseLong(queryParams.getFirst("userID")))));
        }
        else {
            rooms.get(filePath).add(new SocketWithRule(session,queryParams.getFirst("Rule"),userDetailsService.loadUserByID(Long.parseLong(queryParams.getFirst("userID")))));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Connection closed: " + session.getId());
        sessions.remove(session);

    }

    /*protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        URI uri = session.getUri();
        assert uri != null;
        String RoomName = uri.getQuery();

        Set<SocketWithRule> peopleInRoom = rooms.get(RoomName);
        if (peopleInRoom != null) {
            for (SocketWithRule People : peopleInRoom) {
                if (People.getSession().isOpen()) {
                    People.getSession().sendMessage(message);
                }

            }
        }
    }*/
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        URI uri = session.getUri();
        assert uri != null;
        String RoomName = uri.getQuery();

        Set<SocketWithRule> peopleInRoom = rooms.get(RoomName);
        if (peopleInRoom != null) {
            Iterator<SocketWithRule> iterator = peopleInRoom.iterator();
            while (iterator.hasNext()) {
                SocketWithRule People = iterator.next();
                if (People.getSession().isOpen()) {
                    People.getSession().sendMessage(message);
                } else {
                    iterator.remove();
                }
            }
        }
    }
    public void CloseSession(User user, FileToUpload file, String rule){
        String filePath;
        User UserIn;
        String RuleIn;
        for (WebSocketSession session: sessions) {
            URI uri = session.getUri();
            assert uri != null;
            MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
            filePath = queryParams.getFirst("Room");
            try {
                filePath = URLDecoder.decode(filePath, "UTF-8");
            }catch (UnsupportedEncodingException e) {}

            UserIn = userDetailsService.loadUserByID(Long.parseLong(queryParams.getFirst("userID")));
            RuleIn = queryParams.getFirst("Rule");
            if(rule.equals(RuleIn) && user.equals(UserIn) && file.getPath().equals(filePath)){
                try {
                    session.close();
                } catch (IOException e) {
                }
            }
        }
    }

}
@AllArgsConstructor
@Data
class SocketWithRule{
    private WebSocketSession session;
    private String rule;
    private User user;
}