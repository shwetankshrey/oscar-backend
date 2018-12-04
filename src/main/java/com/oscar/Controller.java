package com.oscar;

import org.springframework.web.bind.annotation.*;

@RestController
public class Controller {

    Processor _proc = new Processor();

    @CrossOrigin
    @RequestMapping(value="api", method = RequestMethod.GET)
    public String index(@RequestParam("query") String query) {
        return _proc.process(query);
    }
}
