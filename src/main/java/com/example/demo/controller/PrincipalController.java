package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/principal")
public class PrincipalController {

    @GetMapping("/menu")
    public ModelAndView menu(){
        ModelAndView mv = new ModelAndView("menu/principal");
        return mv;
    }
}
