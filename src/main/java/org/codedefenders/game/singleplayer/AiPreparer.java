package org.codedefenders.game.singleplayer;

import org.codedefenders.execution.AntRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AiPreparer extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AntRunner.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendRedirect(request.getContextPath()+"/games/upload");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        ArrayList<String> messages = new ArrayList<>();
        session.setAttribute("messages", messages);

        switch (request.getParameter("formType")) {
            case "runPrepareAi":
                int cutId = Integer.parseInt(request.getParameter("cutID"));
                System.out.println("Running PrepareAI on class " + cutId);
                if(!PrepareAI.createTestsAndMutants(cutId)) {
                    messages.add("Preparation of AI for the class failed, please prepare the class again, or try a different class.");
                }
                break;
            default:
                break;
        }

        response.sendRedirect(request.getContextPath()+"/games/upload");
    }
}
