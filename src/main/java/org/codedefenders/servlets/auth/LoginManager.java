package org.codedefenders.servlets.auth;

import static org.codedefenders.database.DatabaseAccess.setPasswordResetSecret;
import static org.codedefenders.servlets.admin.AdminUserManagement.DIGITS;
import static org.codedefenders.servlets.admin.AdminUserManagement.LOWER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.codedefenders.database.AdminDAO;
import org.codedefenders.database.DatabaseAccess;
import org.codedefenders.model.User;
import org.codedefenders.servlets.admin.AdminSystemSettings;
import org.codedefenders.util.Constants;
import org.codedefenders.util.EmailUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class LoginManager extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(LoginManager.class);
	private static final int PW_RESET_SECRET_LENGTH = 20;
	private static final String CHANGE_PASSWORD_MSG = "Hello %s!\n\n" +
			"Change your password here: %s\n" +
			"This link is only valid for %d hours.\n\n" +
			"Greetings, your CodeDefenders team";

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		RequestDispatcher dispatcher = request.getRequestDispatcher(Constants.LOGIN_VIEW_JSP);
		dispatcher.forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		ArrayList<String> messages = new ArrayList<String>();
		request.getSession().setAttribute("messages", messages);

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String email = request.getParameter("email");
		String formType = request.getParameter("formType");

		switch (formType) {
			case "create":
				String confirm = request.getParameter("confirm");
				if (!(validUsername(username))) {
					// This check should be performed in the user interface too.
					messages.add("Could not create user. Invalid username.");
					RequestDispatcher dispatcher = request.getRequestDispatcher(Constants.LOGIN_VIEW_JSP);
					dispatcher.forward(request, response);
				} else if (!validPassword(password)) {
					// This check should be performed in the user interface too.
					messages.add("Could not create user. Invalid password.");
					RequestDispatcher dispatcher = request.getRequestDispatcher(Constants.LOGIN_VIEW_JSP);
					dispatcher.forward(request, response);
				} else if (!validEmailAddress(email)) {
					// This check should be performed in the user interface too.
					messages.add("Could not create user. Invalid E-Mail address.");
					RequestDispatcher dispatcher = request.getRequestDispatcher(Constants.LOGIN_VIEW_JSP);
					dispatcher.forward(request, response);
				} else if (!password.equals(confirm)) {
					// This check should be performed in the user interface too.
					messages.add("Could not create user. Password entries did not match.");
					RequestDispatcher dispatcher = request.getRequestDispatcher(Constants.LOGIN_VIEW_JSP);
					dispatcher.forward(request, response);
				} else if (DatabaseAccess.getUserForName(username) != null) {
					messages.add("Could not create user. Username is already taken.");
					RequestDispatcher dispatcher = request.getRequestDispatcher(Constants.LOGIN_VIEW_JSP);
					dispatcher.forward(request, response);
				} else if (DatabaseAccess.getUserForEmail(email) != null) {
					messages.add("Could not create user. Email has already been used. You can reset your password.");
					RequestDispatcher dispatcher = request.getRequestDispatcher(Constants.LOGIN_VIEW_JSP);
					dispatcher.forward(request, response);
				} else {
					User newUser = new User(username, password, email);
					if (newUser.insert()) {
						HttpSession session = request.getSession();
						session.setAttribute("uid", newUser.getId());
						session.setAttribute("username", newUser.getUsername());
						session.setAttribute("messages", messages);
						// Log user activity including the timestamp
						DatabaseAccess.logSession(newUser.getId(), getClientIpAddress(request));
						response.sendRedirect(request.getContextPath() + "/games");
					} else {
						// TODO: How about some error handling?
						messages.add("Could not create a user for you, sorry!");
						RequestDispatcher dispatcher = request.getRequestDispatcher(Constants.LOGIN_VIEW_JSP);
						dispatcher.forward(request, response);
					}
				}
				break;
			case "login":
				User activeUser = DatabaseAccess.getUserForName(username);
				if (activeUser == null) {
					messages.add("Username not found or password incorrect.");
					RequestDispatcher dispatcher = request.getRequestDispatcher(Constants.LOGIN_VIEW_JSP);
					dispatcher.forward(request, response);
				} else {
					String dbPassword = activeUser.getPassword();
					BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
					boolean requireValidation = AdminDAO.getSystemSetting(AdminSystemSettings.SETTING_NAME.REQUIRE_MAIL_VALIDATION).getBoolValue();
					if (requireValidation && !activeUser.isValidated()) {
						messages.add("Account email is not validated.");
						RequestDispatcher dispatcher = request.getRequestDispatcher(Constants.LOGIN_VIEW_JSP);
						dispatcher.forward(request, response);
					} else {
						if (passwordEncoder.matches(password, dbPassword)) {
						    if (activeUser.isActive()) {
								HttpSession session = request.getSession();
								// // Log user activity including the timestamp
								DatabaseAccess.logSession(activeUser.getId(), getClientIpAddress(request));
								session.setAttribute("uid", activeUser.getId());
								session.setAttribute("username", activeUser.getUsername());
								//
								storeApplicationDataInSession(session);

								Object from = session.getAttribute("loginFrom");
								if (from != null && !((String) from).endsWith(".ico")
										&& !((String) from).endsWith(".css")
										&& !((String) from).endsWith(".js")) {
									if (((String) from).startsWith(request.getContextPath())) {
										response.sendRedirect((String) from);
									} else {
										response.sendRedirect(request.getContextPath() + "/" + (String) from);
									}
								} else
									response.sendRedirect(request.getContextPath() + "/games");
							} else {
								messages.add("Your account is inactive, login is only possible with an active account.");
								RequestDispatcher dispatcher = request.getRequestDispatcher(Constants.LOGIN_VIEW_JSP);
								dispatcher.forward(request, response);
							}
						} else {
							messages.add("Username not found or password incorrect.");
							RequestDispatcher dispatcher = request.getRequestDispatcher(Constants.LOGIN_VIEW_JSP);
							dispatcher.forward(request, response);
						}
					}
				}
				break;
			case "resetPassword":
				email = request.getParameter("accountEmail");
				username = request.getParameter("accountUsername");
				User u;
				if ((u = DatabaseAccess.getUserForEmail(email)) != null && u.getUsername().equals(username) &&
						u.getEmail().equals(email)) {
					String resetPwSecret = generatePasswordResetSecret();
					setPasswordResetSecret(u.getId(), resetPwSecret);
					String hostAddr = request.getScheme()+"://"+request.getServerName() + ":" + request.getServerPort()  + request.getContextPath();
					String url =  hostAddr + "/login?resetPW=" + resetPwSecret;
					String msg = String.format(CHANGE_PASSWORD_MSG, u.getUsername(), url,
							AdminDAO.getSystemSetting(AdminSystemSettings.SETTING_NAME.PASSWORD_RESET_SECRET_LIFESPAN).getIntValue());
					if (EmailUtils.sendEmail(u.getEmail(), "Code Defenders Password reset", msg))
						messages.add("A link for changing your password has been sent to " + email);
				} else
					messages.add("No such User found or Email and Username do not match");
				response.sendRedirect(request.getContextPath() + "/login");
				break;

			case "changePassword":
				String resetPwSecret = request.getParameter("resetPwSecret");
				confirm = request.getParameter("inputConfirmPasswordChange");
				password = request.getParameter("inputPasswordChange");

				String responseURL = request.getContextPath() + "/login?resetPW=" + resetPwSecret;
				int userId = DatabaseAccess.getUserIDForPWResetSecret(resetPwSecret);
				if (resetPwSecret != null && userId > -1) {
					if (!(validPassword(password))) {
						messages.add("Password not changed. Make sure it is valid.");
					} else if (password.equals(confirm)) {
						User user = DatabaseAccess.getUser(userId);
						user.setPassword(password);
						if (user.update()) {
							DatabaseAccess.setPasswordResetSecret(user.getId(), null);
							responseURL = request.getContextPath() + "/login";
							messages.add("Successfully changed your Password.");
						}
					} else {
						messages.add("Your Two Password Entries Did Not Match");
					}
				} else {
					messages.add("Your Password reset link is not valid or has expired");
					responseURL = request.getContextPath() + "/login";
				}
				response.sendRedirect(responseURL);
				break;
		}
	}


	private static String generatePasswordResetSecret() {
		StringBuilder sb = new StringBuilder();
		char[] initialSet = LOWER;
		initialSet = ArrayUtils.addAll(initialSet, DIGITS);

		Random random = new Random();
		for (int i = 0; i < PW_RESET_SECRET_LENGTH; i++) {
			sb.append(initialSet[random.nextInt(initialSet.length)]);
		}
		return sb.toString();
	}

	/*
	 * This method collects all the app specific configurations and store them into the current user-session.
	 * This avoids to access Context directly from the JSP code which is a bad practice, since JSP are meant only
	 * for implementing rendering code.
	 */
	private void storeApplicationDataInSession(HttpSession session) {
		// First check the Web abb context
		Boolean isAttackerBlocked = Boolean.FALSE;
		try {
			InitialContext initialContext = new InitialContext();
			Context environmentContext = (Context) initialContext.lookup("java:/comp/env");
			isAttackerBlocked = "enabled".equals((String) environmentContext.lookup(Constants.BLOCK_ATTACKER));
		} catch (NamingException e) {
			logger.warn("Swallow Exception " + e);
			logger.info("Default " + Constants.BLOCK_ATTACKER + " to false");
		}
		session.setAttribute(Constants.BLOCK_ATTACKER, isAttackerBlocked);
	}

	private String getClientIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (invalidIP(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (invalidIP(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (invalidIP(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (invalidIP(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (invalidIP(ip)) {
			ip = request.getRemoteAddr();
		}
		logger.debug("Client IP: " + ip);
		return ip;
	}

	private boolean invalidIP(String ip) {
		return (ip == null) || (ip.length() == 0) ||
				("unknown".equalsIgnoreCase(ip)) || ("0:0:0:0:0:0:0:1".equals(ip));
	}

	/**
	 * Username must contain 3 to 20 alphanumeric characters,
	 * start with an alphabetic character, and have no whitespace or special character.
	 *
	 * @param username
	 * @return true iff valid username
	 */
	// TODO extract that method to a util class
	public static boolean validUsername(String username) {
		String pattern = "^[a-zA-Z][a-zA-Z0-9]{2,19}$";
		return username != null && username.matches(pattern);
	}

	// TODO extract that method to a util class
	public static boolean validEmailAddress(String email) {
		if (email == null) return false;
		boolean result = true;
		try {
			InternetAddress emailAddr = new InternetAddress(email);
			emailAddr.validate();
		} catch (AddressException ex) {
			result = false;
		}
		return result;
	}

	/**
	 * Password must contain MIN_PASSWORD_LENGTH to 20 alphanumeric characters,
	 * with no whitespace or special character.
	 */
	// TODO extract that method to a util class
	public static boolean validPassword(String password) {
		// MIN_PASSWORD_LENGTH-10 alphanumeric characters (a-z, A-Z, 0-9) (no whitespaces)
		int minLength = AdminDAO.getSystemSetting(AdminSystemSettings.SETTING_NAME.MIN_PASSWORD_LENGTH).getIntValue();
		String pattern = "^[a-zA-Z0-9]{" + minLength + ",20}$";
		return password != null && password.matches(pattern);
	}
}