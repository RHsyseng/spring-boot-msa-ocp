package com.redhat.refarch.spring.boot.lambdaair.presentation.service;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping( "/" )
public class WebRequestController
{
	@GetMapping
	public ModelAndView list()
	{
		return new ModelAndView( "index" );
	}
}