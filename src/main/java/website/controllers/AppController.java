package website.controllers;

import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import website.models.ERole;
import website.models.Role;
import website.models.User;
import website.repository.RoleRepository;
import website.repository.UserRepository;

@Controller
public class AppController {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @GetMapping("/")
  public String viewHomePage(Model model) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (isLoggedIn(auth)) {
      return "redirect:/match";
    }

    model.addAttribute("user", new User());
    return "index";
  }

  private boolean isLoggedIn(Authentication auth) {
    return !(auth instanceof AnonymousAuthenticationToken);
  }

  @PostMapping("/process_register")
  public RedirectView processRegister(User user, RedirectAttributes redirectAttributes) {
    RedirectView redirectView = new RedirectView("/",true);

    if (userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
      redirectAttributes.addFlashAttribute("used", true);
    } else {
      String encodedPassword = encoder.encode(user.getPassword());
      user.setPassword(encodedPassword);
      user.setRoles(getUserRole());
      userRepository.save(user);

      redirectAttributes.addFlashAttribute("signedUp", true);
    }

    return redirectView;
  }

  private Set<Role> getUserRole() {
    Set<Role> roles = new HashSet<>();
    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
    roles.add(userRole);
    return roles;
  }

  @GetMapping("/match")
  public String loadGamePage() {
    return "match";
  }

}
