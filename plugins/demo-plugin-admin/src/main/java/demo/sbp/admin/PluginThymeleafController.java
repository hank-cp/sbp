package demo.sbp.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@RequestMapping("/thymeleaf")
public class PluginThymeleafController {

    @GetMapping("/plugin")
    public ModelAndView app() {
        return new ModelAndView("plugin", Map.of(
            "now", System.currentTimeMillis()
        ));
    }

}
