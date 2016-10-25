package com.pemc.crss.sample.service;

import java.io.IOException;
import java.net.URISyntaxException;

public interface LoginService {

    String login(String username, String password) throws URISyntaxException, IOException;

}
