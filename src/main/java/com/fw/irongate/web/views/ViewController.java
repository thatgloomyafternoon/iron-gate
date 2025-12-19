package com.fw.irongate.web.views;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/{path:[^\\.]*}")
public class ViewController {

  @SuppressWarnings("unused")
  public String redirect() {
    return "forward:/index.html";
  }
}
