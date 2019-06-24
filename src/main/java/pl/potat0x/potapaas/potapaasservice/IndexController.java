package pl.potat0x.potapaas.potapaasservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "PotaPaaS";
    }
}
