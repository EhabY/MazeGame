package website.controllers;

import java.security.Principal;
import java.util.Objects;
import mazegame.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import website.payload.request.CommandRequest;
import website.payload.response.CommandResponse;
import website.payload.response.MapMessage;
import website.payload.response.ResponseMessage;
import website.services.MatchService;

@Controller
public class MatchController {

  private final Logger logger = LoggerFactory.getLogger(MatchController.class);

  @Autowired
  private MatchService matchService;

  @MessageMapping("/secured/match/join")
  @SendToUser("/queue/match/message")
  public ResponseMessage joinMatch(Principal user) {
    String message = matchService.joinMatch(user.getName());
    return new ResponseMessage(message);
  }

  @MessageMapping("/secured/match/ready")
  @SendToUser("/queue/match/message")
  public ResponseMessage readyToPlay(Principal user) {
    String message = matchService.makePlayerReady(user.getName());
    return new ResponseMessage(message);
  }

  @MessageMapping("/secured/match/command")
  @SendToUser("/queue/match/command")
  public CommandResponse processCommand(@Payload CommandRequest request, Principal user) {
    String command = request.getCommand();
    Response response = matchService.executeCommand(user.getName(), command);
    return new CommandResponse(command, response);
  }

  @MessageMapping("/secured/match/map")
  @SendToUser("/queue/match/map")
  public MapMessage downloadMap(Principal user) {
    String mapJson = matchService.getMazeMapJson(user.getName());
    return new MapMessage(mapJson);
  }

  @EventListener
  public void onConnect(SessionConnectEvent event) {
    String username = getUsernameFromEvent(event);
    logger.info("Session connect: " + username);
  }

  @EventListener
  public void onDisconnect(SessionDisconnectEvent event) {
    String username = getUsernameFromEvent(event);
    matchService.removePlayer(username);
    logger.info("Session disconnect: " + username);
  }

  private String getUsernameFromEvent(AbstractSubProtocolEvent event) {
    SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
    Principal user = Objects.requireNonNull(headers.getUser());
    return user.getName();
  }

}
