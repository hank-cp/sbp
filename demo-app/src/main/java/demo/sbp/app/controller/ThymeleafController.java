package demo.sbp.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@RequestMapping("/thymeleaf")
public class ThymeleafController {

    @GetMapping("/app")
    public ModelAndView app() {
        return new ModelAndView("app", Map.of(
            "now", System.currentTimeMillis()
        ));
    }

}
