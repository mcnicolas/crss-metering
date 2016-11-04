package com.pemc.crss.metering.resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Slf4j
@RestController
@RequestMapping("/bcq")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BCQResource {
}
