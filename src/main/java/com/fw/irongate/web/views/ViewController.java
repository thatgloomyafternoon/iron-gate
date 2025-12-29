package com.fw.irongate.web.views;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ViewController {

  @RequestMapping(value = {"/{path:[^\\.]*}", "/**/{path:[^\\.]*}"})
  public String redirect() {
    return "forward:/index.html";
  }
}
