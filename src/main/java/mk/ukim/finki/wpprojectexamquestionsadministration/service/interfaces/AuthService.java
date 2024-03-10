package mk.ukim.finki.wpprojectexamquestionsadministration.service.interfaces;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.User;

public interface AuthService {
    User login(String username, String password);
}