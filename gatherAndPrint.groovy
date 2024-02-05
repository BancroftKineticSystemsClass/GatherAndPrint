import org.kohsuke.github.GitHub;

GitHub getGithub() throws IOException {
	File workspace = new File(System.getProperty("user.home") + "/bowler-workspace/");
	if (!workspace.exists()) {
		workspace.mkdir();
	}
	try {
		PasswordManager.loadLoginData(workspace);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	//PasswordManager.login();
	GitHub gh = PasswordManager.getGithub();
	return gh;
}

GitHub github = getGithub();


